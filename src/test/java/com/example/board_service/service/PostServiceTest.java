package com.example.board_service.service;

import com.example.board_service.domain.Post;
import com.example.board_service.domain.User;
import com.example.board_service.dto.PostResponse;
import com.example.board_service.domain.ViewHistory;
import com.example.board_service.repository.PostRepository;
import com.example.board_service.repository.UserRepository;
import com.example.board_service.repository.ViewHistoryRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostServiceTest {

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

    @Test
    void list_통합검색_제목_내용_작성자_전부_검색된다() {
        // given
        String keyword = "검색키";

        Post p1 = postRepository.save(new Post(
                "제목에 " + keyword, "내용1", "철수"
        ));
        Post p2 = postRepository.save(new Post(
                "테스트2", "내용에 " + keyword, "영희"
        ));
        Post p3 = postRepository.save(new Post(
                "테스트3", "내용3", "작성자" + keyword
        ));

        postRepository.save(new Post("아무 제목", "아무 내용", "아무나"));

        var pageable = org.springframework.data.domain.PageRequest.of(
                0, 10, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")
        );

        // when
        var result = postService.list(keyword, pageable);

        // then
        Assertions.assertThat(result.getTotalElements()).isEqualTo(3);

        Assertions.assertThat(result.getContent())
                .extracting(PostResponse::getId)
                .containsExactly(p3.getId(), p2.getId(), p1.getId());

        result.getContent().forEach(res -> {
            boolean matched =
                    (res.getTitle() != null && res.getTitle().contains(keyword)) ||
                            (res.getContent() != null && res.getContent().contains(keyword)) ||
                            (res.getAuthor() != null && res.getAuthor().contains(keyword));

            Assertions.assertThat(matched).isTrue();
        });
    }

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
                "조회수 테스트", "내용입니다", user.getNickname()
        ));

        int originalViewCount = post.getViewCount();

        setAuthentication(user.getEmail());

        // when
        PostResponse first = postService.get(post.getId());
        PostResponse second = postService.get(post.getId());

        // then
        Assertions.assertThat(first.getViewCount()).isEqualTo(originalViewCount + 1);
        Assertions.assertThat(second.getViewCount()).isEqualTo(originalViewCount + 1);

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
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
