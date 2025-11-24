package com.example.board_service.like;

import com.example.board_service.domain.Post;
import com.example.board_service.dto.PostResponse;
import com.example.board_service.exception.NotFoundException;
import com.example.board_service.repository.PostRepository;
import com.example.board_service.user.User;
import com.example.board_service.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PostLikeService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;

    public PostLikeService(PostRepository postRepository,
                           UserRepository userRepository,
                           PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
    }

    // ✅ 좋아요 토글 + Post.likeCount 업데이트
    public PostResponse toggleLike(Long postId) {
        // 1) 현재 로그인 유저 이메일 꺼내기 (JWT에서 principal = email 로 세팅돼 있음)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        // 디버깅용 (원하면 잠깐 켜서 확인)
        // System.out.println("toggleLike userId=" + user.getId() + ", email=" + email);

        // 2) 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));

        // 3) 이미 좋아요 했는지 확인 (ID 기준으로 조회)
        Optional<PostLike> existing =
                postLikeRepository.findByUserIdAndPostId(user.getId(), post.getId());

        if (existing.isPresent()) {
            // 이미 좋아요 → 좋아요 해제
            postLikeRepository.delete(existing.get());
            post.decreaseLikeCount();
        } else {
            // 아직 안 눌렀으면 → 좋아요 추가
            PostLike postLike = new PostLike(user, post);
            postLikeRepository.save(postLike);
            post.increaseLikeCount();
        }

        // 4) 변경사항은 JPA가 flush하면서 Post에도 반영됨
        //    (필요하면 postRepository.save(post); 한 번 더 호출해도 됨)
        return PostResponse.from(post);
    }
}
