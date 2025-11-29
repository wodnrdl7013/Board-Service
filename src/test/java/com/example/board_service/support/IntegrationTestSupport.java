package com.example.board_service.support;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MySQLContainer;

/**
 * 통합 테스트 공통 베이스 클래스.
 * - MySQL Testcontainers 띄우고
 * - spring.datasource.* 를 컨테이너로 덮어쓴다.
 * - Flyway도 이 DB에 자동으로 마이그레이션 수행.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
@ActiveProfiles("test") // 필요하면 application-test.yml 따로 둘 수도 있음
public abstract class IntegrationTestSupport {

    // MySQL 8.0 컨테이너 하나만 전체 테스트에서 공유
    @Container
    static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("board")
            .withUsername("test")
            .withPassword("test");

    // Spring Boot가 사용할 DB 설정을 컨테이너 값으로 덮어쓰기
    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);

        // ddl-auto는 이미 validate로 되어 있으니 그대로 두면 됨
        // Flyway도 동일하게 이 DB에 대해 동작
    }
}
