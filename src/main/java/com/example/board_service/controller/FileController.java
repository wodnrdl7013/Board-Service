package com.example.board_service.controller;

import com.example.board_service.domain.UploadedFile;
import com.example.board_service.dto.FileResponse;
import com.example.board_service.exception.NotFoundException;
import com.example.board_service.repository.PostRepository;
import com.example.board_service.repository.UploadedFileRepository;
import com.example.board_service.storage.FileStorage;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/files")
public class FileController {

    private final FileStorage fileStorage;
    private final UploadedFileRepository uploadedFileRepository;
    private final PostRepository postRepository;

    public FileController(FileStorage fileStorage,
                          UploadedFileRepository uploadedFileRepository,
                          PostRepository postRepository) {
        this.fileStorage = fileStorage;
        this.uploadedFileRepository = uploadedFileRepository;
        this.postRepository = postRepository;
    }

    /**
     * 파일 업로드 (JWT 인증 필요)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public FileResponse upload(@PathVariable Long postId,
                               @RequestPart("file") MultipartFile file) {
        // 게시글 존재 확인
        postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));

        // S3에 업로드
        String directory = "posts/" + postId;
        String url = fileStorage.upload(directory, file);

        UploadedFile saved = uploadedFileRepository.save(
                new UploadedFile(
                        postId,
                        file.getOriginalFilename(),
                        url,
                        file.getContentType(),
                        file.getSize()
                )
        );

        return FileResponse.from(saved);
    }

    /**
     * 해당 게시글의 파일 목록 조회
     */
    @GetMapping
    public List<FileResponse> list(@PathVariable Long postId) {
        List<UploadedFile> files = uploadedFileRepository.findByPostIdOrderByUploadedAtAsc(postId);
        return files.stream()
                .map(FileResponse::from)
                .toList();
    }
}
