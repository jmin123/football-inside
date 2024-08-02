package com.example.football_inside.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDto {
    private Long id;
    private String content;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}