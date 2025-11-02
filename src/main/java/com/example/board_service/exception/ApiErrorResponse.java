package com.example.board_service.exception;

import java.time.LocalDateTime;

public class ApiErrorResponse {
    private LocalDateTime timestamp = LocalDateTime.now();
    private String message;
    private int status;
    public String path;

    public ApiErrorResponse(String message, int status, String path) {
        this.message = message;
        this.status = status;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }
}
