package com.example.football_inside.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PostUpdateDto {
    private String title;
    private String content;
    private Set<Long> categoryIds;
}
