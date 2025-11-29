package com.example.board_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    // 👉 지역만 하드코딩
    private static final Region REGION = Region.AP_NORTHEAST_2;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(REGION)
                // 👉 자격증명은 AWS SDK가 알아서
                //    - AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY
                //    - 또는 IAM Role
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
