package com.example.board_service.repository;

import com.example.board_service.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long> {

    // 기존 (제목만)
    Page<Post> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    // ✅ 제목 + 내용 + 작성자 통합 검색
    //   - title : ContainingIgnoreCase
    //   - content : Containing (CLOB라서 IgnoreCase 안씀)
    //   - author : ContainingIgnoreCase
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingOrAuthorContainingIgnoreCase(
            String titleKeyword,
            String contentKeyword,
            String authorKeyword,
            Pageable pageable
    );
}
