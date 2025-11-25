package com.example.board_service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    /**
     * 파일을 업로드하고, 접근 가능한 URL(또는 key)을 반환한다.
     *
     * @param directory 업로드 경로 prefix (예: "posts/1")
     * @param file      업로드할 파일
     * @return 업로드된 파일 URL (또는 key)
     */
    String upload(String directory, MultipartFile file);
}
