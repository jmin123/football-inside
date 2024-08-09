package com.example.football_inside.controller;

import com.example.football_inside.dto.CommentDto;
import com.example.football_inside.entity.User;
import com.example.football_inside.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<Page<CommentDto>> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentDto> comments = commentService.getCommentsByPostId(postId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestBody CommentDto commentDto, Authentication authentication) {
        log.info("Received request to add comment to post: {}", postId);

        if (authentication == null) {
            log.error("Authentication is null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        log.info("Authentication principal: {}", authentication.getPrincipal());

        if (!(authentication.getPrincipal() instanceof User)) {
            log.error("Unexpected authentication principal type: {}", authentication.getPrincipal().getClass());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected authentication principal type");
        }

        User user = (User) authentication.getPrincipal();
        log.info("User ID: {}, Username: {}", user.getId(), user.getUsername());

        try {
            CommentDto createdComment = commentService.createComment(postId, user.getId(), commentDto.getContent());
            log.info("Comment created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
        } catch (Exception e) {
            log.error("Error creating comment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating comment: " + e.getMessage());
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long postId,
                                           @PathVariable Long commentId,
                                           @RequestBody CommentDto commentDto,
                                           Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        try {
            CommentDto updatedComment = commentService.updateComment(postId, commentId, commentDto.getContent(), user.getId());
            return ResponseEntity.ok(updatedComment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to update this comment");
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId,
                                           @PathVariable Long commentId,
                                           Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        try {
            commentService.deleteComment(postId, commentId, user.getId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to delete this comment");
        }
    }
}