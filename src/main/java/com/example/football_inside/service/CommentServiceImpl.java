package com.example.football_inside.service;

import com.example.football_inside.dto.CommentDto;
import com.example.football_inside.entity.Comment;
import com.example.football_inside.entity.Post;
import com.example.football_inside.entity.User;
import com.example.football_inside.exception.ResourceNotFoundException;
import com.example.football_inside.repository.CommentRepository;
import com.example.football_inside.repository.PostRepository;
import com.example.football_inside.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        return comments.stream().map(this::convertToDto).toList();
    }

    private CommentDto convertToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setUsername(comment.getUser().getUsername());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}