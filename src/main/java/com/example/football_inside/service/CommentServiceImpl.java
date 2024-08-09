package com.example.football_inside.service;

import com.example.football_inside.dto.CommentDto;
import com.example.football_inside.entity.Comment;
import com.example.football_inside.entity.Post;
import com.example.football_inside.entity.User;
import com.example.football_inside.exception.ResourceNotFoundException;
import com.example.football_inside.exception.UnauthorizedException;
import com.example.football_inside.repository.CommentRepository;
import com.example.football_inside.repository.PostRepository;
import com.example.football_inside.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    private static final int MAX_COMMENT_LENGTH = 500;

    @Override
    public CommentDto createComment(Long postId, Long userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        if (content.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("Comment is too long. Maximum length is " +
                    MAX_COMMENT_LENGTH + " characters.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content.trim());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return convertToDto(savedComment);
    }

    @Override
    public List<CommentDto> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
        return comments.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Cacheable(value = "comments", key = "#postId + #pageable.pageNumber")
    @Override
    public Page<CommentDto> getCommentsByPostId(Long postId, Pageable pageable) {
        return commentRepository.findCommentDtosByPostId(postId, pageable);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long postId, Long commentId, String content, Long userId) throws UnauthorizedException {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        if (content.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("Comment is too long. Maximum length is " +
                    MAX_COMMENT_LENGTH + " characters.");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new ResourceNotFoundException("Comment does not belong to the specified post");
        }

        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to update this comment");
        }

        comment.setContent(content.trim());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        return convertToDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) throws UnauthorizedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new ResourceNotFoundException("Comment does not belong to the specified post");
        }

        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }


    private CommentDto convertToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setUsername(comment.getUser().getUsername());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
}