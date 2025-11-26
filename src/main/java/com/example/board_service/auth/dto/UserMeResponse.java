package com.example.board_service.auth.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMeResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String roles;
}
