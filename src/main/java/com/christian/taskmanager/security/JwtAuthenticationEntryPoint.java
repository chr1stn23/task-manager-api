package com.christian.taskmanager.security;

import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import com.christian.taskmanager.exception.ExpiredTokenException;
import com.christian.taskmanager.exception.InvalidTokenException;
import com.christian.taskmanager.exception.TokenProcessingException;
import com.christian.taskmanager.util.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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
        ApiResponseWrapper<Void> errorResponse = ResponseUtils.error(errorMessage, "UNAUTHORIZED");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String resolveMessage(AuthenticationException ex) {
        return switch (ex) {
            case InvalidTokenException ignored -> "El token es inválido o ha sido manipulado.";
            case ExpiredTokenException ignored -> "Tu sesión ha expirado. Por favor, inicia sesión de nuevo.";
            case TokenProcessingException ignored -> "Error al procesar el token de acceso.";
            case DisabledException ignored -> "La cuenta está deshabilitada.";
            case InsufficientAuthenticationException ignored -> "No se ha proporcionado un token de acceso válido.";
            case BadCredentialsException ignored -> "Credenciales incorrectas.";
            default -> "No autorizado.";
        };
    }
}
