package com.example.football_inside.repository;

import com.example.football_inside.entity.Category;
import com.example.football_inside.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByUserId(Long userId, Pageable pageable);
    Page<Post> findByCategoriesContaining(Category category, Pageable pageable);
}
