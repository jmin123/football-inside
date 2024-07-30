package com.example.football_inside.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PostCreateDto {
    private String title;
    private String content;
    private Set<Long> categoryIds;
}
