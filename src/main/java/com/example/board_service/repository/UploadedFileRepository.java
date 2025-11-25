package com.example.board_service.repository;

import com.example.board_service.domain.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    List<UploadedFile> findByPostIdOrderByUploadedAtAsc(Long postId);
}
