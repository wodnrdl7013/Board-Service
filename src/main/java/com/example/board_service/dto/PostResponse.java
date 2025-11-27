package com.example.board_service.dto;

import com.example.board_service.domain.Post;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String author;
    private int viewCount;
    private int likeCount;
    private int dislikeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 첨부 파일 목록 (상세 조회 시 사용)
    private List<FileResponse> files;

    public PostResponse(Long id,
                        String title,
                        String content,
                        String author,
                        int viewCount,
                        int likeCount,
                        int dislikeCount,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt,
                        List<FileResponse> files) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.files = files;
    }

    /**
     * 파일 정보를 포함하지 않는 기본 변환
     * - 게시글 목록 조회 등에서 사용
     */
    public static PostResponse from(Post p) {
        return new PostResponse(
                p.getId(),
                p.getTitle(),
                p.getContent(),
                p.getAuthor(),
                p.getViewCount(),
                p.getLikeCount(),
                p.getDislikeCount(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                null
        );
    }

    /**
     * 첨부 파일까지 함께 내려주는 변환
     * - 게시글 상세 조회에서 사용
     */
    public static PostResponse from(Post p, List<FileResponse> files) {
        return new PostResponse(
                p.getId(),
                p.getTitle(),
                p.getContent(),
                p.getAuthor(),
                p.getViewCount(),
                p.getLikeCount(),
                p.getDislikeCount(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                files
        );
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

    public List<FileResponse> getFiles() {
        return files;
    }
}
