package com.example.board_service.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserMeResponse {

    private Long id;
    private String email;
    private String nickname;
    private String roles;
}
