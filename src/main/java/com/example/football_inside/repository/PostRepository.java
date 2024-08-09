package com.example.football_inside.repository;

import com.example.football_inside.dto.PostSummaryDto;
import com.example.football_inside.entity.Category;
import com.example.football_inside.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByUserId(Long userId, Pageable pageable);
    Page<Post> findByCategoriesContaining(Category category, Pageable pageable);
    @Query("SELECT new com.example.football_inside.dto.PostSummaryDto(p.id, p.title, p.user.username, p.createdAt, p.recommendationCount, SIZE(p.comments)) " +
            "FROM Post p JOIN p.categories c " +
            "WHERE LOWER(c.name) = LOWER(:categoryName)")
    Page<PostSummaryDto> findPostSummariesByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);
}
