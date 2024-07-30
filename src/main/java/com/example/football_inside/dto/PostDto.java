package com.example.football_inside.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private String username;
    private int recommendationCount;
    private Set<Long> categoryIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
