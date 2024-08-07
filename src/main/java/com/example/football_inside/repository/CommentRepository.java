package com.example.football_inside.repository;

import com.example.football_inside.dto.CommentDto;
import com.example.football_inside.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post.id = :postId")
    int deleteByPostId(Long postId);
    @Query("SELECT new com.example.football_inside.dto.CommentDto(c.id, c.content, c.user.username, c.createdAt, c.updatedAt) " +
            "FROM Comment c WHERE c.post.id = :postId")
    Page<CommentDto> findCommentDtosByPostId(@Param("postId") Long postId, Pageable pageable);
}