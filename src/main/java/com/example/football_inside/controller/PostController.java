package com.example.football_inside.controller;

import com.example.football_inside.dto.PostCreateDto;
import com.example.football_inside.dto.PostDto;
import com.example.football_inside.dto.PostUpdateDto;
import com.example.football_inside.entity.Category;
import com.example.football_inside.entity.User;
import com.example.football_inside.exception.ResourceNotFoundException;
import com.example.football_inside.repository.CategoryRepository;
import com.example.football_inside.service.PostServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostServiceImpl postService;
    private final CategoryRepository categoryRepository;

    @PostMapping
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostCreateDto post, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        PostDto createdPost = postService.createPost(post, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<PostDto>> getPostsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getPostsByCategory(categoryId, pageable));
    }

    @GetMapping("/category/name/{categoryName}")
    public ResponseEntity<?> getPostsByCategoryName(@PathVariable String categoryName,
                                                    @RequestParam int page,
                                                    @RequestParam int size) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName));

        Long categoryId = category.getId();
        Page<PostDto> posts = postService.getPostsByCategory(categoryId, PageRequest.of(page, size));
        return ResponseEntity.ok(posts);
    }


    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @Valid @RequestBody PostUpdateDto post, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        PostDto updatedPost = postService.updatePost(id, post, userId);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostDto>> getPostsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getPostsByUser(userId, pageable));
    }


    @PostMapping("/{postId}/recommend")
    public ResponseEntity<PostDto> recommendPost(@PathVariable Long postId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        PostDto updatedPost = postService.recommendPost(postId, user.getId());
        return ResponseEntity.ok(updatedPost);
    }

    @PostMapping("/{postId}/unrecommend")
    public ResponseEntity<?> unrecommendPost(@PathVariable Long postId, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            PostDto updatedPost = postService.unrecommendPost(postId, user.getId());
            return ResponseEntity.ok(updatedPost);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}