package com.example.football_inside.service;

import com.example.football_inside.dto.CommentDto;
import com.example.football_inside.exception.UnauthorizedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    List<CommentDto> getCommentsByPostId(Long postId);
    Page<CommentDto> getCommentsByPostId(Long postId, Pageable pageable);
    CommentDto createComment(Long postId, Long userId, String content);
    CommentDto updateComment(Long postId, Long commentId, String content, Long userId) throws UnauthorizedException;
    void deleteComment(Long postId, Long commentId, Long userId) throws UnauthorizedException;
}