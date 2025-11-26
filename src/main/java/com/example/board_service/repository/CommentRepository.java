package com.example.board_service.repository;

import com.example.board_service.domain.Comment;
import com.example.board_service.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 모든 댓글 + 대댓글 시간순
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // 루트 댓글(부모 없는 것들)만
    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);

    // 대댓글(부모 있는 것들)
    List<Comment> findByPostIdAndParentIsNotNullOrderByCreatedAtAsc(Long postId);
}
