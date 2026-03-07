package com.christian.taskmanager.security;

import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import com.christian.taskmanager.util.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ApiResponseWrapper<Void> errorResponse = ResponseUtils.error("Authentication required", "UNAUTHORIZED");

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
