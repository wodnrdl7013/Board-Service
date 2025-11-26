package com.example.board_service.service;

import com.example.board_service.domain.Post;
import com.example.board_service.domain.User;
import com.example.board_service.domain.ViewHistory;
import com.example.board_service.domain.UploadedFile;
import com.example.board_service.dto.CreatePostRequest;
import com.example.board_service.dto.FileResponse;
import com.example.board_service.dto.PostResponse;
import com.example.board_service.dto.UpdatePostRequest;
import com.example.board_service.exception.NotFoundException;
import com.example.board_service.repository.UserRepository;
import com.example.board_service.repository.PostRepository;
import com.example.board_service.repository.ViewHistoryRepository;
import com.example.board_service.repository.UploadedFileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final UploadedFileRepository uploadedFileRepository;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       ViewHistoryRepository viewHistoryRepository,
                       UploadedFileRepository uploadedFileRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.viewHistoryRepository = viewHistoryRepository;
        this.uploadedFileRepository = uploadedFileRepository;
    }

    /**
     * 🔐 현재 로그인한 사용자의 이메일 가져오기
     * - JwtAuthenticationFilter에서 Authentication.principal 에 email(String)을 넣어놨다고 가정
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        // principal이 String 이면 getName() == principal
        return authentication.getName();
    }

    @Transactional
    public PostResponse create(CreatePostRequest req) {
        // 🔥 JWT 인증 정보에서 email 가져오기 (UserDetails로 캐스팅 X)
        String email = getCurrentUserEmail();

        // email로 User 엔티티 검색 (nickname 사용하기 위해)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        // author = user.getNickname() 자동 설정
        Post saved = postRepository.save(
                new Post(
                        req.getTitle(),
                        req.getContent(),
                        user.getNickname()
                )
        );

        return PostResponse.from(saved);
    }

    /**
     * 게시글 상세 조회 + 조회수 중복 방지
     * - 로그인 사용자 기준
     * - 같은 유저가 같은 글을 같은 날 보면 1번만 증가
     */
    @Transactional
    public PostResponse get(Long id) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));

        // 🔥 현재 로그인 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증된 사용자만 조회수 중복 방지 로직 적용
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            // ✅ principal 캐스팅 안 하고, getName() 으로 username(이메일) 가져오기
            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + email));

            LocalDate today = LocalDate.now();

            boolean alreadyViewed = viewHistoryRepository
                    .existsByUserIdAndPostIdAndViewedAt(user.getId(), id, today);

            if (!alreadyViewed) {
                viewHistoryRepository.save(new ViewHistory(user.getId(), id, today));
                p.increaseViewCount();
            }
        }

        // 📎 첨부 파일 조회
        List<UploadedFile> files = uploadedFileRepository.findByPostIdOrderByUploadedAtAsc(id);
        List<FileResponse> fileResponses = files.stream()
                .map(FileResponse::from)
                .toList();

        return PostResponse.from(p, fileResponses);
    }

    public Page<PostResponse> list(String keyword, Pageable pageable) {
        Page<Post> page = (keyword == null || keyword.isBlank())
                ? postRepository.findAll(pageable)
                : postRepository.findByTitleContainingIgnoreCaseOrContentContainingOrAuthorContainingIgnoreCase(
                keyword, // titleKeyword
                keyword, // contentKeyword
                keyword, // authorKeyword
                pageable
        );

        return page.map(PostResponse::from);
    }



    // ✅ 작성자만 수정 가능
    @Transactional
    public PostResponse update(Long id, UpdatePostRequest req) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));

        // 🔥 JWT에서 현재 로그인 유저 email 가져오기 (UserDetails 캐스팅 제거)
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        // 🔒 작성자 체크 (닉네임 기준)
        if (!p.getAuthor().equals(user.getNickname())) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");
        }

        p.update(req.getTitle(), req.getContent());
        return PostResponse.from(p);
    }

    // ✅ 작성자만 삭제 가능
    @Transactional
    public void delete(Long id) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));

        // 🔥 JWT에서 현재 로그인 유저 email 가져오기 (UserDetails 캐스팅 제거)
        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        // 🔒 작성자 체크
        if (!p.getAuthor().equals(user.getNickname())) {
            throw new AccessDeniedException("작성자만 삭제할 수 있습니다.");
        }

        postRepository.delete(p);
    }
}
