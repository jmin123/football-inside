package com.example.football_inside.controller;

import com.example.football_inside.dto.PostCreateDto;
import com.example.football_inside.dto.PostDto;
import com.example.football_inside.dto.PostUpdateDto;
import com.example.football_inside.entity.User;
import com.example.football_inside.exception.ResourceNotFoundException;
import com.example.football_inside.exception.UnauthorizedException;
import com.example.football_inside.security.JwtTokenProvider;
import com.example.football_inside.service.PostServiceImpl;
import com.example.football_inside.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final UserService userService;
    private final PostServiceImpl postService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody PostCreateDto post, @RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        String jwtToken = token.substring(7);
        if (!jwtTokenProvider.validateToken(jwtToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        String username = jwtTokenProvider.getUsernameFromJWT(jwtToken);
        User user = userService.getUserByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        Long userId = user.getId();

        // Check if categoryIds is not empty
        if (post.getCategoryIds().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("At least one category must be selected");
        }

        // Get the first category ID from the set
        Long categoryId = post.getCategoryIds().iterator().next();

        PostDto createdPost = postService.createPost(post, userId, categoryId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PostDto>> getPost(@PathVariable Long id) {
        log.info("Received request for post with id: {}", id);
        PostDto post = postService.getPostById(id);

        // category name을 이용하여 category id를 찾아서 추가
        EntityModel<PostDto> resource = EntityModel.of(post);

        resource.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(this.getClass()).getPost(id)
        ).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/category/name/{categoryName}")
    public ResponseEntity<Page<PostDto>> getPostsByCategoryName(
            @PathVariable String categoryName,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Received request for posts in category: {}", categoryName);
        Page<PostDto> posts = postService.getPostsByCategoryName(categoryName, pageable);
        log.info("Returning {} posts for category: {}", posts.getTotalElements(), categoryName);
        return ResponseEntity.ok(posts);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PostDto>>> getAllPosts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostDto> posts = postService.getAllPosts(pageable);

        List<EntityModel<PostDto>> resources = posts.getContent().stream()
                .map(post -> EntityModel.of(post,
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getPost(post.getId())).withSelfRel(),
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getAllPosts(pageable)).withRel("all-posts")
                ))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<PostDto>> result = CollectionModel.of(resources);

        result.add(WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(this.getClass()).getAllPosts(pageable)
        ).withSelfRel());

        return ResponseEntity.ok(result);
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id,
                                              @Valid @RequestBody PostUpdateDto post, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();
        PostDto updatedPost = postService.updatePost(id, post, userId);
        return ResponseEntity.ok(updatedPost);
    }
    
    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            log.error("Authentication is null for delete request on post id: {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = (User) authentication.getPrincipal();
        log.info("Attempting to delete post with id: {} by user: {}", id, user.getUsername());

        try {
            postService.deletePost(id, user.getId());
            log.info("Successfully deleted post with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.warn("Post not found with id: {}. It might have been already deleted.", id);
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedException e) {
            log.warn("User {} is not authorized to delete post with id: {}", user.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error deleting post with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostDto>> getPostsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getPostsByUser(userId, pageable));
    }


    @PostMapping("/{postId}/recommend")
    public ResponseEntity<PostDto> recommendPost(@PathVariable Long postId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) authentication.getPrincipal();
        PostDto updatedPost = postService.recommendPost(postId, user.getId());
        return ResponseEntity.ok(updatedPost);
    }

    @PostMapping("/{postId}/unrecommend")
    public ResponseEntity<?> unrecommendPost(@PathVariable Long postId, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            PostDto updatedPost = postService.unRecommendPost(postId, user.getId());
            return ResponseEntity.ok(updatedPost);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}