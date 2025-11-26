package com.example.board_service.like;

import com.example.board_service.domain.Post;
import com.example.board_service.domain.User;
import com.example.board_service.dto.PostResponse;
import com.example.board_service.repository.PostRepository;
import com.example.board_service.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PostLikeServiceTest {

    @Autowired
    PostLikeService postLikeService;

    @Autowired
    PostLikeRepository postLikeRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * toggleLike 호출 시
     *  - 처음에는 좋아요 추가 (likeCount = 1)
     *  - 한 번 더 누르면 좋아요 해제 (likeCount = 0)
     */
    @Test
    void toggleLike_한번_누르면_좋아요_다시_누르면_취소() {
        // given

        String random = UUID.randomUUID().toString();

        User user = userRepository.save(
                User.builder()
                        .email("like-" + random + "@example.com")
                        .password("encoded-password")
                        .nickname("닉네임-" + random.substring(0, 8)) // 재순재 금지
                        .roles("ROLE_USER")
                        .build()
        );

        Post post = postRepository.save(new Post(
                "좋아요 테스트",
                "내용",
                user.getNickname()
        ));

        // PostLikeService는 principal을 String으로 캐스팅함
        //  → principal 에 email(String) 넣어줘야 함
        setAuthentication(user.getEmail());

        // when
        PostResponse first = postLikeService.toggleLike(post.getId());   // 좋아요 추가
        PostResponse second = postLikeService.toggleLike(post.getId());  // 좋아요 취소

        // then
        assertThat(first.getLikeCount()).isEqualTo(1);
        assertThat(second.getLikeCount()).isEqualTo(0);

        // 실제 DB 상에서도 좋아요가 0건이어야 함
        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(0L);
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
