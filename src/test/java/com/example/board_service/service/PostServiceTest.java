package com.example.board_service.service;

import com.example.board_service.domain.Post;
import com.example.board_service.domain.User;
import com.example.board_service.dto.PostResponse;
import com.example.board_service.domain.ViewHistory;
import com.example.board_service.repository.PostRepository;
import com.example.board_service.repository.UserRepository;
import com.example.board_service.repository.ViewHistoryRepository;
import com.example.board_service.support.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Transactional
class PostServiceTest extends IntegrationTestSupport {

    @Autowired
    PostService postService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ViewHistoryRepository viewHistoryRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 제목 / 내용 / 작성자 통합 검색이 잘 되는지 검증
     * - 다른 데이터랑 안 섞이도록 특수 키워드 사용
     */
    @Test
    void list_통합검색_제목_내용_작성자_전부_검색된다() {
        // given
        // 너무 길면 author 컬럼 길이를 초과하니까 짧은 키워드 사용
        String keyword = "검색키";

        // 제목에 keyword 포함
        Post p1 = postRepository.save(new Post(
                "제목에 " + keyword,
                "내용1",
                "철수"
        ));

        // 내용에 keyword 포함
        Post p2 = postRepository.save(new Post(
                "테스트2",
                "내용에 " + keyword,
                "영희"
        ));

        // 작성자에 keyword 포함 (author 길이도 짧게)
        Post p3 = postRepository.save(new Post(
                "테스트3",
                "내용3",
                "작성자" + keyword  // 예: "작성자검색키"
        ));

        // 이건 매칭되면 안 되는 글
        postRepository.save(new Post(
                "아무 제목",
                "아무 내용",
                "아무나"
        ));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));

        // when
        Page<PostResponse> result = postService.list(keyword, pageable);

        // then
        // 우리가 방금 넣은 3개만 나와야 한다.
        Assertions.assertThat(result.getTotalElements()).isEqualTo(3);

        // 정렬이 id DESC 라고 가정했으니 p3, p2, p1 순서
        Assertions.assertThat(result.getContent())
                .extracting(PostResponse::getId)
                .containsExactly(p3.getId(), p2.getId(), p1.getId());

        // 진짜로 keyword를 포함하는지 최종 확인
        result.getContent().forEach(res -> {
            boolean matched =
                    (res.getTitle() != null && res.getTitle().contains(keyword)) ||
                            (res.getContent() != null && res.getContent().contains(keyword)) ||
                            (res.getAuthor() != null && res.getAuthor().contains(keyword));

            Assertions.assertThat(matched).isTrue();
        });
    }

    /**
     * 같은 유저가 같은 날 같은 글을 여러 번 조회해도
     * 조회수는 1만 증가해야 한다.
     */
    @Test
    void get_같은유저_같은날_여러번_조회해도_조회수는_1만_증가() {
        // given
        String randomEmail = "test-" + UUID.randomUUID() + "@example.com";

        User user = userRepository.save(
                User.builder()
                        .email(randomEmail)
                        .password("encoded-password")
                        .nickname("조회수유저")
                        .roles("ROLE_USER")
                        .build()
        );

        Post post = postRepository.save(new Post(
                "조회수 테스트",
                "내용입니다",
                user.getNickname()
        ));

        int originalViewCount = post.getViewCount(); // 보통 0

        // SecurityContext 에 인증 정보 세팅 (principal = email)
        setAuthentication(user.getEmail());

        // when
        PostResponse first = postService.get(post.getId());   // 첫 조회
        PostResponse second = postService.get(post.getId());  // 같은 날 두 번째 조회

        // then
        // 첫 호출에서만 +1, 두 번째 호출에서 추가 증가 X
        Assertions.assertThat(first.getViewCount()).isEqualTo(originalViewCount + 1);
        Assertions.assertThat(second.getViewCount()).isEqualTo(originalViewCount + 1);

        // ViewHistory: 해당 (user, post, today) 조합은 1건이어야 한다.
        LocalDate today = LocalDate.now();
        List<ViewHistory> histories = viewHistoryRepository.findAll();

        long count = histories.stream()
                .filter(v -> v.getUserId().equals(user.getId()))
                .filter(v -> v.getPostId().equals(post.getId()))
                .filter(v -> v.getViewedAt().equals(today))
                .count();

        Assertions.assertThat(count).isEqualTo(1L);
    }

    private void setAuthentication(String email) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, // principal
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
