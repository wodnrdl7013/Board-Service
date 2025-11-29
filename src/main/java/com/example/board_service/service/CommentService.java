package com.example.board_service.service;

import com.example.board_service.domain.Comment;
import com.example.board_service.domain.Post;
import com.example.board_service.dto.comment.CommentResponse;
import com.example.board_service.dto.comment.CreateCommentRequest;
import com.example.board_service.repository.CommentRepository;
import com.example.board_service.repository.PostRepository;
import com.example.board_service.repository.UserRepository;
import com.example.board_service.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 댓글 생성 (루트 댓글)
     */
    @Transactional
    public CommentResponse createComment(Long postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + postId));

        // 🔥 JWT 에서 현재 로그인 유저 꺼내기
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = (String) authentication.getPrincipal();

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
    public CommentResponse createReply(Long postId, Long parentCommentId, CreateCommentRequest request) {
        // 게시글 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + postId));

        // 부모 댓글 확인
        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다. id=" + parentCommentId));

        // 부모 댓글이 해당 게시글에 속하는지 검증
        if (!Objects.equals(parent.getPost().getId(), postId)) {
            throw new IllegalArgumentException("부모 댓글이 해당 게시글의 댓글이 아닙니다.");
        }

        // 🔥 JWT 에서 현재 로그인 유저 꺼내기
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = (String) authentication.getPrincipal();

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
                // 자식 댓글 → 부모 DTO의 children에 추가
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

        // 🔥 JWT에서 현재 로그인 유저 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        // 🔒 작성자 체크
        if (!target.getAuthor().equals(user.getNickname())) {
            throw new AccessDeniedException("작성자만 댓글을 삭제할 수 있습니다.");
        }

        // 자식 댓글들 먼저 삭제 (네가 원래 쓰던 로직 유지)
        List<Comment> allComments =
                commentRepository.findByPostIdOrderByCreatedAtAsc(target.getPost().getId());

        List<Comment> children = allComments.stream()
                .filter(c -> c.getParent() != null
                        && Objects.equals(c.getParent().getId(), commentId))
                .collect(Collectors.toList());

        commentRepository.deleteAll(children);
        commentRepository.delete(target);
    }

}
