package com.example.football_inside.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class PostCategoryId implements Serializable {
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "category_id")
    private Long categoryId;
}
