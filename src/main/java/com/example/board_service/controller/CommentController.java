package com.example.board_service.controller;

import com.example.board_service.dto.comment.CommentResponse;
import com.example.board_service.dto.comment.CreateCommentRequest;
import com.example.board_service.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 / 대댓글 생성
     *
     * - parentId 없으면: 루트 댓글
     * - parentId 있으면: 해당 댓글의 대댓글
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestBody @Valid CreateCommentRequest request
    ) {
        CommentResponse response;

        if (parentId == null) {
            response = commentService.createComment(postId, request);
        } else {
            response = commentService.createReply(postId, parentId, request);
        }

        return ResponseEntity
                .created(URI.create("/api/posts/" + postId + "/comments"))
                .body(response);
    }

    /**
     * 게시글별 전체 댓글 + 대댓글 트리 조회
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long postId
    ) {
        List<CommentResponse> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
