package com.example.board_service.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "view_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_view_history_user_post_date",
                columnNames = {"user_id", "post_id", "viewed_at"}
        )
)
public class ViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 봤는지 (User 엔티티의 id 값 저장)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 어떤 게시글을 봤는지 (Post 엔티티의 id 값 저장)
    @Column(name = "post_id", nullable = false)
    private Long postId;

    // 언제 봤는지 (날짜 기준)
    @Column(name = "viewed_at", nullable = false)
    private LocalDate viewedAt;

    protected ViewHistory() {
        // JPA 기본 생성자
    }

    public ViewHistory(Long userId, Long postId, LocalDate viewedAt) {
        this.userId = userId;
        this.postId = postId;
        this.viewedAt = viewedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPostId() {
        return postId;
    }

    public LocalDate getViewedAt() {
        return viewedAt;
    }
}
