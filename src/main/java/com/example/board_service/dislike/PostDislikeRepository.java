package com.example.board_service.dislike;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostDislikeRepository extends JpaRepository<PostDislike, Long> {

    // 유저 + 게시글 기준으로 싫어요 여부 확인
    Optional<PostDislike> findByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);
}
