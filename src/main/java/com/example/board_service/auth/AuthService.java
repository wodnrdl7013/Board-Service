package com.example.board_service.auth;

import com.example.board_service.auth.dto.LoginRequest;
import com.example.board_service.auth.dto.LoginResponse;
import com.example.board_service.auth.dto.SignupRequest;
import com.example.board_service.auth.dto.UserMeResponse;
import com.example.board_service.config.JwtProvider;
import com.example.board_service.exception.NotFoundException;
import com.example.board_service.user.User;
import com.example.board_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(SignupRequest request) {

        // ✅ 방법 A: 중복 이메일/닉네임 체크를 여기서 안 한다.
        // DB의 UNIQUE 제약 조건에 맡긴다.
        // 중복이면 save 시점에 DataIntegrityViolationException 발생 → GlobalExceptionHandler에서 409로 처리.

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .roles("ROLE_USER")
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.")
                );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRoles());

        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    @Transactional(readOnly = true)
    public UserMeResponse me() {

        // 현재 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("인증이 필요합니다.");
        }

        String email = authentication.getName(); // JwtAuthenticationFilter에서 email을 Principal로 넣었음

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        return UserMeResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .roles(user.getRoles())
                .build();
    }

}
