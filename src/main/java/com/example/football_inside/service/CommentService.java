package com.example.football_inside.service;

import com.example.football_inside.dto.CommentDto;
import com.example.football_inside.exception.UnauthorizedException;

import java.util.List;

public interface CommentService {
    List<CommentDto> getCommentsByPostId(Long postId);
    CommentDto createComment(Long postId, Long userId, String content);
    CommentDto updateComment(Long postId, Long commentId, String content, Long userId) throws UnauthorizedException;
    void deleteComment(Long postId, Long commentId, Long userId) throws UnauthorizedException;
}