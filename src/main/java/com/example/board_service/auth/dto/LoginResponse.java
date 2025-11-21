package com.example.board_service.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponse {

    private Long userId;
    private String email;
    private String nickname;

    private String accessToken;
    private String tokenType; // 항상 "Bearer"
}
