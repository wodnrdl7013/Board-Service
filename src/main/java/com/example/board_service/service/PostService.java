package com.example.board_service.service;

import com.example.board_service.domain.Post;
import com.example.board_service.dto.CreatePostRequest;
import com.example.board_service.dto.PostResponse;
import com.example.board_service.dto.UpdatePostRequest;
import com.example.board_service.exception.NotFoundException;
import com.example.board_service.repository.PostRepository;
import com.example.board_service.user.User;
import com.example.board_service.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // JWT에서 현재 로그인 유저 조회
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        // JwtAuthenticationFilter에서 principal로 email(String)을 넣었으므로
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }

    @Transactional
    public PostResponse create(CreatePostRequest req) {
        User user = getCurrentUser();

        Post saved = postRepository.save(
                new Post(
                        req.getTitle(),
                        req.getContent(),
                        user.getNickname()
                )
        );

        return PostResponse.from(saved);
    }

    // 👀 상세 조회 시 조회수 증가
    @Transactional   // ★★★ 이거 중요: readOnly=false로 오버라이드
    public PostResponse get(Long id) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));

        p.increaseViewCount();   // 변경 감지 -> UPDATE 쿼리 나감

        return PostResponse.from(p);
    }

    public Page<PostResponse> list(String keyword, Pageable pageable) {
        Page<Post> page = (keyword == null || keyword.isBlank())
                ? postRepository.findAll(pageable)
                : postRepository.findByTitleContainingIgnoreCase(keyword, pageable);

        return page.map(PostResponse::from);
    }

    @Transactional
    public PostResponse update(Long id, UpdatePostRequest req) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));

        User user = getCurrentUser();

        if (!p.getAuthor().equals(user.getNickname())) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");
        }

        p.update(req.getTitle(), req.getContent());
        return PostResponse.from(p);
    }

    @Transactional
    public void delete(Long id) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));

        User user = getCurrentUser();

        if (!p.getAuthor().equals(user.getNickname())) {
            throw new AccessDeniedException("작성자만 삭제할 수 있습니다.");
        }

        postRepository.delete(p);
    }

    // 👍 좋아요 기능
    @Transactional
    public PostResponse like(Long id) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));

        p.increaseLikeCount();
        return PostResponse.from(p);
    }

    // 필요하면 👎 싫어요도
    @Transactional
    public PostResponse dislike(Long id) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));

        p.increaseDislikeCount();
        return PostResponse.from(p);
    }
}
