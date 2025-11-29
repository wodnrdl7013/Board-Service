package com.example.board_service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Component
public class S3FileStorage implements FileStorage {

    private final S3Client s3Client;

    // 👉 여기서 완전 하드코딩
    private static final String BUCKET = "board-service-jaewook-files";
    private static final String BASE_URL = "https://board-service-jaewook-files.s3.ap-northeast-2.amazonaws.com";

    public S3FileStorage(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String upload(String directory, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // S3 object key (경로 + 랜덤 파일명)
        String key = directory + "/" + UUID.randomUUID() + ext;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(key)
                    .contentType(file.getContentType())
                    // .acl(ObjectCannedACL.PUBLIC_READ)  // 버킷이 ACL 허용이면 사용 (지금은 생략)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        // 버킷이 public 아니면 이 URL로 직접 열리진 않을 수 있지만, 경로 확인 용도로 쓴다.
        return BASE_URL + "/" + key;
    }
}
