package com.example.football_inside.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PostSummaryDto {
    private Long id;
    private String title;
    private String username;
    private LocalDateTime createdAt;
    private int recommendationCount;
    private int commentCount;

    public PostSummaryDto(Long id, String title, String username, LocalDateTime createdAt, int recommendationCount, long commentCount) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.createdAt = createdAt;
        this.recommendationCount = recommendationCount;
        this.commentCount = (int) commentCount; // Cast long to int
    }
}
