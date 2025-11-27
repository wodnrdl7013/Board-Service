package com.example.board_service.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

    @NotBlank
    private String content;
}
