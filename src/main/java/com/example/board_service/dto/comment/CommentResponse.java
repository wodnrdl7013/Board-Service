package com.example.board_service.dto.comment;

import com.example.board_service.domain.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class CommentResponse {

    private Long id;
    private Long postId;
    private Long parentId;
    private String author;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 대댓글 리스트
    @Builder.Default
    private List<CommentResponse> children = new ArrayList<>();

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .author(comment.getAuthor())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .children(new ArrayList<>())
                .build();
    }
}
