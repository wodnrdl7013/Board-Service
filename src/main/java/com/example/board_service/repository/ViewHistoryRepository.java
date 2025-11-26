package com.example.board_service.repository;

import com.example.board_service.domain.ViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    boolean existsByUserIdAndPostIdAndViewedAt(Long userId, Long postId, LocalDate viewedAt);
}
