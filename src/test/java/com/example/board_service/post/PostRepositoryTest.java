package com.example.board_service.post;

import com.example.board_service.domain.Post;
import com.example.board_service.repository.PostRepository;
import com.example.board_service.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class PostRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("게시글을 저장하고 다시 조회할 수 있다")
    void saveAndFind() {
        // given
        Post post = new Post(
                "테스트 제목",
                "테스트 내용",
                "재욱"
        );

        // when
        Post saved = postRepository.save(post);

        // then
        assertThat(saved.getId()).isNotNull();

        Post found = postRepository.findById(saved.getId())
                .orElseThrow(() -> new IllegalStateException("저장한 게시글이 조회되지 않음"));

        assertThat(found.getTitle()).isEqualTo("테스트 제목");
        assertThat(found.getContent()).isEqualTo("테스트 내용");
        assertThat(found.getAuthor()).isEqualTo("재욱");

        // viewCount / likeCount / dislikeCount 기본값도 같이 확인해볼 수 있음
        assertThat(found.getViewCount()).isZero();
        assertThat(found.getLikeCount()).isZero();
        assertThat(found.getDislikeCount()).isZero();
    }
}
