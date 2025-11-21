package com.example.board_service.config;

import com.example.board_service.exception.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        ApiErrorResponse body = new ApiErrorResponse(
                "인증이 필요합니다.",
                HttpStatus.UNAUTHORIZED.value(),
                request.getRequestURI()
        );

        // 혹시 앞에서 뭐라도 써졌으면 싹 지움
        if (response.isCommitted()) {
            // 이미 커밋됐으면 더 못 건드리니까 그냥 로그만 남기고 리턴
            return;
        }

        response.resetBuffer(); // 버퍼 초기화

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(response.getWriter(), body);

        response.flushBuffer(); // 바로 전송
    }
}
