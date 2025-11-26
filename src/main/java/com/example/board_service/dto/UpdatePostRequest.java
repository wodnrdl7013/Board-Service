package com.example.board_service.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdatePostRequest {
    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String content;

    public UpdatePostRequest() {}

    public UpdatePostRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
