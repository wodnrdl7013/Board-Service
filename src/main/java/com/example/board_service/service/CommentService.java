package com.example.board_service.service;

import com.example.board_service.domain.Comment;
import com.example.board_service.domain.Post;
import com.example.board_service.dto.comment.CommentResponse;
import com.example.board_service.dto.comment.CreateCommentRequest;
import com.example.board_service.repository.CommentRepository;
import com.example.board_service.repository.PostRepository;
import com.example.board_service.user.User;
import com.example.board_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    /**
     * 루트 댓글 생성
     */
    @Transactional
    public CommentResponse createComment(Long postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + postId));

        // 🔥 JWT 에서 현재 로그인 유저 꺼내기
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        String email = principal.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        Comment comment = Comment.builder()
                .post(post)
                .parent(null)
                .author(user.getNickname())          // 🔥 author = JWT 유저 닉네임
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);
        return CommentResponse.from(saved);
    }

    /**
     * 대댓글 생성
     */
    @Transactional
    public CommentResponse createReply(Long postId, Long parentId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + postId));

        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다. id=" + parentId));

        if (!Objects.equals(parent.getPost().getId(), postId)) {
            throw new IllegalArgumentException("부모 댓글이 해당 게시글의 댓글이 아닙니다.");
        }

        // 🔥 JWT 에서 현재 로그인 유저 꺼내기
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        String email = principal.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        Comment reply = Comment.builder()
                .post(post)
                .parent(parent)
                .author(user.getNickname())          // 🔥 author = JWT 유저 닉네임
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(reply);
        return CommentResponse.from(saved);
    }

    /**
     * 게시글 기준 전체 댓글 + 대댓글 계층 구조로 조회
     */
    public List<CommentResponse> getCommentsByPost(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        // 1. 엔티티 → DTO 변환 + Map에 담기
        Map<Long, CommentResponse> dtoMap = comments.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toMap(CommentResponse::getId, dto -> dto));

        // 2. 트리 구조 만들기
        List<CommentResponse> roots = new ArrayList<>();

        for (Comment comment : comments) {
            CommentResponse dto = dtoMap.get(comment.getId());

            if (comment.getParent() == null) {
                // 루트 댓글
                roots.add(dto);
            } else {
                // 부모 댓글의 children에 추가
                CommentResponse parentDto = dtoMap.get(comment.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            }
        }

        return roots;
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId) {
        Comment target = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + commentId));

        List<Comment> allComments = commentRepository.findByPostIdOrderByCreatedAtAsc(target.getPost().getId());

        List<Comment> children = allComments.stream()
                .filter(c -> c.getParent() != null && Objects.equals(c.getParent().getId(), commentId))
                .collect(Collectors.toList());

        commentRepository.deleteAll(children);
        commentRepository.delete(target);
    }
}
