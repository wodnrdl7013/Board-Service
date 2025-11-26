package com.example.board_service.controller;

import com.example.board_service.dto.CreatePostRequest;
import com.example.board_service.dto.PageResponse;
import com.example.board_service.dto.PostResponse;
import com.example.board_service.dto.UpdatePostRequest;
import com.example.board_service.like.PostLikeService;
import com.example.board_service.dislike.PostDislikeService;
import com.example.board_service.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;
    private final PostDislikeService postDislikeService;

    public PostController(PostService postService,
                          PostLikeService postLikeService,
                          PostDislikeService postDislikeService) {
        this.postService = postService;
        this.postLikeService = postLikeService;
        this.postDislikeService = postDislikeService;
    }

    @PostMapping
    public PostResponse create(@Valid @RequestBody CreatePostRequest req) {
        return postService.create(req);
    }

    @GetMapping("/{id}")
    public PostResponse get(@PathVariable Long id) {
        return postService.get(id);
    }

    @GetMapping
    public PageResponse<PostResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sort // 🔥 정렬 옵션 추가
    ){
        // 🔥 sort 파라미터에 따라 정렬 기준 결정
        Sort sortOption;
        switch (sort) {
            case "views":
                // 조회수 내림차순
                sortOption = Sort.by(Sort.Direction.DESC, "viewCount");
                break;
            case "likes":
                // 좋아요 내림차순
                sortOption = Sort.by(Sort.Direction.DESC, "likeCount");
                break;
            case "latest":
            default:
                // 최신순 (id DESC - auto increment 가정)
                sortOption = Sort.by(Sort.Direction.DESC, "id");
                break;
        }

        Pageable pageable = PageRequest.of(page, size, sortOption);
        Page<PostResponse> res = postService.list(keyword, pageable);
        return PageResponse.of(res);
    }


    @PutMapping("/{id}")
    public PostResponse update(@PathVariable Long id,
                               @Valid @RequestBody UpdatePostRequest req) {
        return postService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        postService.delete(id);
    }

    // 👍 좋아요 토글
    @PostMapping("/{id}/like")
    public PostResponse toggleLike(@PathVariable Long id) {
        return postLikeService.toggleLike(id);
    }

    // 👎 싫어요 토글
    @PostMapping("/{id}/dislike")
    public PostResponse toggleDislike(@PathVariable Long id) {
        return postDislikeService.toggleDislike(id);
    }
}
