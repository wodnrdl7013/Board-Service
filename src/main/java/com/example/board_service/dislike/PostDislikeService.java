package com.example.board_service.dislike;

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
public class PostDislikeService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostDislikeRepository postDislikeRepository;

    public PostDislikeService(PostRepository postRepository,
                              UserRepository userRepository,
                              PostDislikeRepository postDislikeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postDislikeRepository = postDislikeRepository;
    }

    // 👎 싫어요 토글 + Post.dislikeCount 업데이트
    public PostResponse toggleDislike(Long postId) {
        // 1) JWT에서 현재 로그인 유저 이메일 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        // 2) 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));

        // 3) 이미 싫어요 했는지 확인
        Optional<PostDislike> existing =
                postDislikeRepository.findByUserIdAndPostId(user.getId(), post.getId());

        if (existing.isPresent()) {
            // 이미 싫어요 → 취소
            postDislikeRepository.delete(existing.get());
            post.decreaseDislikeCount();
        } else {
            // 아직 싫어요 안 눌렀으면 → 싫어요 추가
            PostDislike postDislike = new PostDislike(user, post);
            postDislikeRepository.save(postDislike);
            post.increaseDislikeCount();
        }

        // 변경된 게시글 상태 반환
        return PostResponse.from(post);
    }
}
