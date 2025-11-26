package com.example.board_service.like;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // ✅ ID 기반 조회 → 프록시/equals 문제 원천 차단
    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);
}
