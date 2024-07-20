package com.example.football_inside.repository;

import com.example.football_inside.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByPostId(Long postId);
    List<Like> findByUserId(Long userId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
}