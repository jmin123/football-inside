package com.example.football_inside.service;

import com.example.football_inside.dto.PostCreateDto;
import com.example.football_inside.dto.PostDto;
import com.example.football_inside.dto.PostSummaryDto;
import com.example.football_inside.dto.PostUpdateDto;
import com.example.football_inside.entity.Category;
import com.example.football_inside.entity.Post;
import com.example.football_inside.entity.Recommendation;
import com.example.football_inside.entity.User;
import com.example.football_inside.exception.ResourceNotFoundException;
import com.example.football_inside.exception.UnauthorizedException;
import com.example.football_inside.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RecommendationRepository recommendationRepository;
    private final CommentRepository commentRepository;

    @Value("${recommendation.unrecommend.time-limit:1}")
    private long unrecommendTimeLimitMinutes;

    @Override
    public PostDto createPost(PostCreateDto postCreateDto, Long userId, Long categoryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        // ID로 카테고리를 찾아서 Set에 저장(Set에 저장하는 이유는 중복을 피하기 위해서)
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Post post = new Post();
        post.setTitle(postCreateDto.getTitle());
        post.setContent(postCreateDto.getContent());
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.getCategories().add(category);

        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    @Override
    public PostDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        PostDto dto = convertToDTO(post);

        Set<String> categoryName = post.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toSet());

        dto.setCategoryName(categoryName);

        return dto;
    }

    @Override
    public Page<PostDto> getPostsByCategoryName(String categoryName, Pageable pageable) {
        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName));

        Page<Post> posts = postRepository.findByCategoriesContaining(category, pageable);

        return posts.map(this::convertToDTO);
    }

    @Cacheable(value = "postSummaries", key = "#categoryName + #pageable.pageNumber")
    @Override
    public Page<PostSummaryDto> getPostSummariesByCategoryName(String categoryName, Pageable pageable) {
        return postRepository.findPostSummariesByCategoryName(categoryName, pageable);
    }

    @Override
    public Page<PostDto> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional
    @Override
    public PostDto recommendPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (post.getUser().getId().equals(userId)) {
            throw new IllegalStateException("자신의 게시글은 추천할 수 없습니다.");
        }

        Optional<Recommendation> existingRecommendation = recommendationRepository.findByPostAndUser(post, user);

        if (existingRecommendation.isPresent()) {
            throw new IllegalStateException("이미 추천한 게시글입니다.");
        }

        Recommendation recommendation = new Recommendation();
        recommendation.setPost(post);
        recommendation.setUser(user);
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendationRepository.save(recommendation);

        post.setRecommendationCount(post.getRecommendationCount() + 1);
        postRepository.save(post);

        return convertToDTO(post);
    }

    @Transactional
    @Override
    public PostDto unRecommendPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Recommendation> recommendation = recommendationRepository.findByPostAndUser(post, user);

        if (recommendation.isEmpty()) {
            throw new IllegalStateException("아직 추천하지 않았습니다.");
        }

        LocalDateTime unrecommendDeadline = recommendation.get().getCreatedAt().plusMinutes(unrecommendTimeLimitMinutes);

        if (LocalDateTime.now().isAfter(unrecommendDeadline)) {
            throw new IllegalStateException("추천 취소 시간이 지났습니다.");
        }

        recommendationRepository.delete(recommendation.get());
        post.setRecommendationCount(post.getRecommendationCount() - 1);
        postRepository.save(post);

        return convertToDTO(post);
    }

    @Override
    public List<String> getRecommendationUsernames(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글이 존재하지 않음"));
        return recommendationRepository.findByPostId(postId).stream()
                .map(recommendation -> recommendation.getUser().getUsername())
                .collect(Collectors.toList());
    }


    // 게시글 수정
    @Override
    public PostDto updatePost(Long id, PostUpdateDto updatedPost, Long userId) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시글이 존재하지 않음"));
        if (!existingPost.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("해당 게시글을 수정할 권한 없음");
        }

        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        existingPost.setUpdatedAt(LocalDateTime.now());

        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(updatedPost.getCategoryIds()));
        existingPost.setCategories(categories);

        Post savedPost = postRepository.save(existingPost);
        return convertToDTO(savedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to delete this post");
        }

        recommendationRepository.deleteByPostId(id);

        postRepository.delete(post);
    }

    @Override
    public Page<PostDto> getPostsByUser(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return postRepository.findByUserId(userId, pageable).map(this::convertToDTO);
    }

    private PostDto convertToDTO(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setUsername(post.getUser().getUsername());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setRecommendationCount(post.getRecommendationCount());
        dto.setCategoryName(post.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toSet()));
        return dto;
    }
}