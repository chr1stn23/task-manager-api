package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.request.AuthRequestDTO;
import com.christian.taskmanager.dto.request.RegisterRequestDTO;
import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import com.christian.taskmanager.dto.response.AuthResponseDTO;
import com.christian.taskmanager.service.AuthService;
import com.christian.taskmanager.service.model.TokenPair;
import com.christian.taskmanager.util.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseWrapper<AuthResponseDTO>> register(
            @RequestBody @Valid RegisterRequestDTO request) {

        TokenPair tokens = authService.register(request);

        return buildAuthResponse(tokens);
    }

    @Operation(summary = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseWrapper<AuthResponseDTO>> login(@RequestBody @Valid AuthRequestDTO request) {
        TokenPair tokens = authService.login(request);
        return buildAuthResponse(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseWrapper<AuthResponseDTO>> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        TokenPair tokens = authService.refresh(refreshToken);
        return buildAuthResponse(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseWrapper<String>> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        authService.logout(refreshToken);
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(ResponseUtils.success("Logout successful"));
    }

    @NonNull
    private ResponseEntity<ApiResponseWrapper<AuthResponseDTO>> buildAuthResponse(TokenPair tokens) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        AuthResponseDTO response = new AuthResponseDTO(tokens.accessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ResponseUtils.success(response));
    }
}
