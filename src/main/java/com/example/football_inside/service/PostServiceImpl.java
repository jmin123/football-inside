package com.example.football_inside.service;

import com.example.football_inside.entity.Category;
import com.example.football_inside.entity.Post;
import com.example.football_inside.entity.User;
import com.example.football_inside.repository.CategoryRepository;
import com.example.football_inside.repository.PostRepository;
import com.example.football_inside.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService{
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Post createPost(Post post, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    @Override
    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Override
    public Post updatePost(Long id, Post updatedPost, Long userId) {
        Post existingPost = getPostById(id);

        if (!existingPost.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to update this post");
        }

        existingPost.setTitle(updatedPost.getTitle());
        existingPost.setContent(updatedPost.getContent());
        existingPost.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(existingPost);
    }

    @Override
    public void deletePost(Long id, Long userId) {
        Post post = getPostById(id);

        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this post");
        }

        postRepository.deleteById(id);
    }

    @Override
    public List<Post> getPostsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return postRepository.findByUserId(userId);
    }

    @Override
    public List<Post> getPostsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return postRepository.findByCategoriesId(categoryId);
    }
}
