package com.example.board_service.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApiErrorResponse {

    private final String timestamp;
    private final String message;
    private final int status;
    private final String path;

    public ApiErrorResponse(String message, int status, String path) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.message = message;
        this.status = status;
        this.path = path;
    }

    public String getTimestamp() {
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
