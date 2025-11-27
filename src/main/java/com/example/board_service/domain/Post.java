package com.example.board_service.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "Posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false, length = 50)
    private String author;

    // 👀 조회수
    @Column(nullable = false)
    private int viewCount = 0;

    // 👍 좋아요 수
    @Column(nullable = false)
    private int likeCount = 0;

    // 👎 싫어요 수 (원하면 사용)
    @Column(nullable = false)
    private int dislikeCount = 0;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Post() {
    }

    public Post(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getDislikeCount() {
        return dislikeCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // ====== 조회수 / 좋아요 / 싫어요 증가/감소 메서드 ======
    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseDislikeCount() {
        this.dislikeCount++;
    }

    public void decreaseDislikeCount() {
        if (this.dislikeCount > 0) {
            this.dislikeCount--;
        }
    }

    // ====== equals / hashCode (JPA 프록시 대비용) ======
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        Post other = (Post) o;
        // 영속화 안 된 엔티티(id == null)는 동일 객체로만 같다고 본다
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        // Hibernate 권장 패턴
        return getClass().hashCode();
    }
}
