-- 1. users
CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(100) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(20)  NOT NULL,
    roles       VARCHAR(100) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    CONSTRAINT uk_users_email    UNIQUE (email),
    CONSTRAINT uk_users_nickname UNIQUE (nickname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. posts
CREATE TABLE posts (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    content       TINYTEXT     NOT NULL,
    author        VARCHAR(50)  NOT NULL,
    view_count    INT          NOT NULL,
    like_count    INT          NOT NULL,
    dislike_count INT          NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. comments
CREATE TABLE comments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id    BIGINT       NOT NULL,
    parent_id  BIGINT       NULL,
    author     VARCHAR(50)  NOT NULL,
    content    TEXT         NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    CONSTRAINT fk_comments_post
        FOREIGN KEY (post_id) REFERENCES posts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent
        FOREIGN KEY (parent_id) REFERENCES comments(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_comments_post_id ON comments (post_id);
CREATE INDEX idx_comments_parent_id ON comments (parent_id);

-- 4. post_likes
CREATE TABLE post_likes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    post_id    BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_post_likes_user_post UNIQUE (user_id, post_id),
    CONSTRAINT fk_post_likes_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_post
        FOREIGN KEY (post_id) REFERENCES posts(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_post_likes_user_id ON post_likes (user_id);
CREATE INDEX idx_post_likes_post_id ON post_likes (post_id);

-- 5. post_dislikes
CREATE TABLE post_dislikes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    post_id    BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_post_dislikes_user_post UNIQUE (user_id, post_id),
    CONSTRAINT fk_post_dislikes_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_post_dislikes_post
        FOREIGN KEY (post_id) REFERENCES posts(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_post_dislikes_user_id ON post_dislikes (user_id);
CREATE INDEX idx_post_dislikes_post_id ON post_dislikes (post_id);

-- 6. view_history
CREATE TABLE view_history (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id   BIGINT NOT NULL,
    post_id   BIGINT NOT NULL,
    viewed_at DATE   NOT NULL,
    CONSTRAINT uk_view_history_user_post_date
        UNIQUE (user_id, post_id, viewed_at),
    CONSTRAINT fk_view_history_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_view_history_post
        FOREIGN KEY (post_id) REFERENCES posts(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_view_history_user_id ON view_history (user_id);
CREATE INDEX idx_view_history_post_id ON view_history (post_id);

-- 7. uploaded_file
CREATE TABLE uploaded_file (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id       BIGINT        NOT NULL,
    original_name VARCHAR(255)  NOT NULL,
    url           VARCHAR(1000) NOT NULL,
    content_type  VARCHAR(255)  NULL,
    size          BIGINT        NULL,
    uploaded_at   DATETIME(6)   NOT NULL,
    CONSTRAINT fk_uploaded_file_post
        FOREIGN KEY (post_id) REFERENCES posts(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_uploaded_file_post_id ON uploaded_file (post_id);
