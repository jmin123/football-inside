package com.example.football_inside.service;

import com.example.football_inside.dto.PostCreateDto;
import com.example.football_inside.dto.PostDto;
import com.example.football_inside.dto.PostUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    PostDto createPost(PostCreateDto postCreateDto, Long userId, Long categoryId);

    PostDto getPostById(Long id);

    Page<PostDto> getPostsByCategoryName(String categoryName, Pageable pageable);

    Page<PostDto> getAllPosts(Pageable pageable);

    PostDto updatePost(Long id, PostUpdateDto postUpdateDto, Long userId);

    void deletePost(Long id, Long userId);

    PostDto recommendPost(Long postId, Long userId);

    PostDto unRecommendPost(Long postId, Long userId);

    Page<PostDto> getPostsByUser(Long userId, org.springframework.data.domain.Pageable pageable);
}
