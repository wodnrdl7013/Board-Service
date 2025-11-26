package com.example.board_service.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_file")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시글에 속한 파일인지 (Post와 직접 연관관계 안 걸고 postId만 저장)
    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size")
    private Long size;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    protected UploadedFile() {
    }

    public UploadedFile(Long postId,
                        String originalName,
                        String url,
                        String contentType,
                        Long size) {
        this.postId = postId;
        this.originalName = originalName;
        this.url = url;
        this.contentType = contentType;
        this.size = size;
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getPostId() {
        return postId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSize() {
        return size;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
