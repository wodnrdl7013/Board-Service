package com.example.board_service.service;

import com.example.board_service.domain.Post;
import com.example.board_service.user.User;
import com.example.board_service.dto.CreatePostRequest;
import com.example.board_service.dto.PostResponse;
import com.example.board_service.dto.UpdatePostRequest;
import com.example.board_service.exception.NotFoundException;
import com.example.board_service.repository.PostRepository;
import com.example.board_service.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Transactional
    public PostResponse create(CreatePostRequest req) {

        // 🔥 JWT 인증 정보 꺼내기
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        // 🔥 principal.getUsername() = email
        String email = principal.getUsername();

        // 🔥 email로 User 엔티티 검색 (nickname 사용하기 위해)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException("User not found: " + email));

        // 🔥 author = user.getNickname() 자동 설정
        Post saved = postRepository.save(
                new Post(
                        req.getTitle(),
                        req.getContent(),
                        user.getNickname()
                )
        );

        return PostResponse.from(saved);
    }

    public PostResponse get(Long id) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));
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
        p.update(req.getTitle(), req.getContent());
        return PostResponse.from(p);
    }

    @Transactional
    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new NotFoundException("Post not found: " + id);
        }
        postRepository.deleteById(id);
    }
}
