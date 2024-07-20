package com.example.football_inside.service;

import com.example.football_inside.entity.Post;

import java.util.List;

public interface PostService {
    Post createPost(Post post, Long userId);
    Post getPostById(Long id);
    List<Post> getAllPosts();
    Post updatePost(Long id, Post updatedPost, Long userId);
    void deletePost(Long id, Long userId);
    List<Post> getPostsByUser(Long userId);
    List<Post> getPostsByCategory(Long categoryId);
}
