package com.example.football_inside.service;

import com.example.football_inside.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long postId, Long userId, String content);
    List<CommentDto> getCommentsByPostId(Long postId);
}