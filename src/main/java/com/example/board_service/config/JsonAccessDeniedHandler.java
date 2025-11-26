package com.example.board_service.config;

import com.example.board_service.exception.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        ApiErrorResponse body = new ApiErrorResponse(
                "접근 권한이 없습니다.",
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI()
        );

        if (response.isCommitted()) {
            return;
        }

        response.resetBuffer();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(response.getWriter(), body);

        response.flushBuffer();
    }
}
