package com.christian.taskmanager.security;

import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import com.christian.taskmanager.exception.ExpiredTokenException;
import com.christian.taskmanager.exception.InvalidTokenException;
import com.christian.taskmanager.exception.TokenProcessingException;
import com.christian.taskmanager.exception.UserDisabledException;
import com.christian.taskmanager.util.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
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

        String errorMessage = resolveMessage(authException);
        String errorCode = resolveCode(authException);
        ApiResponseWrapper<Void> errorResponse = ResponseUtils.error(errorMessage, errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String resolveMessage(AuthenticationException ex) {
        return switch (ex) {
            case InvalidTokenException ignored -> "Token is invalid or has been modified.";
            case ExpiredTokenException ignored -> "Your session has expired. Please log in again.";
            case TokenProcessingException ignored -> "Error processing the access token.";
            case UserDisabledException ignored -> "User is disabled.";
            case InsufficientAuthenticationException ignored -> "A valid token access was not provided.";
            default -> "Unauthorized.";
        };
    }

    private String resolveCode(AuthenticationException ex) {
        return switch (ex) {
            case InvalidTokenException ignored -> "INVALID_TOKEN";
            case ExpiredTokenException ignored -> "EXPIRED_TOKEN";
            case TokenProcessingException ignored -> "TOKEN_PROCESSING_ERROR";
            case UserDisabledException ignored -> "USER_DISABLED";
            case InsufficientAuthenticationException ignored -> "NO_TOKEN_PROVIDED";
            default -> "UNAUTHORIZED";
        };
    }
}
