package com.example.football_inside.repository;

import com.example.football_inside.entity.Post;
import com.example.football_inside.entity.Recommendation;
import com.example.football_inside.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    Optional<Recommendation> findByPostAndUser(Post post, User user);
}