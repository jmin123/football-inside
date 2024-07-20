package com.example.football_inside.repository;

import com.example.football_inside.entity.PostCategory;
import com.example.football_inside.entity.PostCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostCategoryRepository extends JpaRepository<PostCategory, PostCategoryId> {
    List<PostCategory> findByPostId(Long postId);
    List<PostCategory> findByCategoryId(Long categoryId);
}