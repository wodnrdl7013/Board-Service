package com.example.board_service.service;

import com.example.board_service.domain.Post;
import com.example.board_service.dto.CreatePostRequest;
import com.example.board_service.dto.PostResponse;
import com.example.board_service.dto.UpdatePostRequest;
import com.example.board_service.exception.NotFoundException;
import com.example.board_service.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional
    public PostResponse create(CreatePostRequest req) {
        Post saved = postRepository.save(new Post(req.getTitle(), req.getContent(), req.getAuthor()));
        return PostResponse.from(saved);
    }

    public PostResponse get(Long id) {
        Post p = postRepository.findById(id).orElseThrow(() -> new NotFoundException("Post not found: " + id));
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
        Post p = postRepository.findById(id).orElseThrow(() -> new NotFoundException("Post not found: " + id));
        p.update(req.getTitle(), req.getContent());
        return PostResponse.from(p);
    }

    @Transactional
    public void delete(Long id) {
        if(!postRepository.existsById(id)) {
            throw new NotFoundException("Post not found: " + id);
        }
        postRepository.deleteById(id);
    }
}
