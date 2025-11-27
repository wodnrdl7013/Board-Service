package com.example.board_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    private final JsonAccessDeniedHandler jsonAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF 비활성화 (JWT 사용)
        http.csrf(AbstractHttpConfigurer::disable);

        // 세션 사용 X
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 인가 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/auth/signup",
                        "/auth/login",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/**"
                ).permitAll()
                // ★ 관리자 전용 API
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 그 외는 로그인만 되어 있으면 접근 가능
                .anyRequest().authenticated()
        );

        // 401 / 403 JSON 응답
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(jsonAuthenticationEntryPoint)
                .accessDeniedHandler(jsonAccessDeniedHandler)
        );

        // 익명 사용자 비활성화
        http.anonymous(anonymous -> anonymous.disable());

        // 기본 로그인 방식 비활성화
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);

        // JWT 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
