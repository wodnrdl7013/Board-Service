package com.example.board_service.dislike;

import com.example.board_service.domain.Post;
import com.example.board_service.domain.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "post_dislikes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_dislike_user_post",
                        columnNames = {"user_id", "post_id"}
                )
        }
)
public class PostDislike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👎 싫어요 누른 유저
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 👎 대상 게시글
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PostDislike() {
    }

    public PostDislike(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Post getPost() {
        return post;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // (선택) JPA 프록시 대비용
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostDislike)) return false;
        PostDislike other = (PostDislike) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
