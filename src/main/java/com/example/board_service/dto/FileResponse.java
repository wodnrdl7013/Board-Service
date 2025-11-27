package com.example.board_service.dto;

import com.example.board_service.domain.UploadedFile;

import java.time.LocalDateTime;

public class FileResponse {

    private Long id;
    private String originalName;
    private String url;
    private String contentType;
    private Long size;
    private LocalDateTime uploadedAt;

    public FileResponse(Long id,
                        String originalName,
                        String url,
                        String contentType,
                        Long size,
                        LocalDateTime uploadedAt) {
        this.id = id;
        this.originalName = originalName;
        this.url = url;
        this.contentType = contentType;
        this.size = size;
        this.uploadedAt = uploadedAt;
    }

    public static FileResponse from(UploadedFile uf) {
        return new FileResponse(
                uf.getId(),
                uf.getOriginalName(),
                uf.getUrl(),
                uf.getContentType(),
                uf.getSize(),
                uf.getUploadedAt()
        );
    }

    public Long getId() {
        return id;
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
