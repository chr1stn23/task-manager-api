package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import com.christian.taskmanager.dto.response.SessionResponseDTO;
import com.christian.taskmanager.security.CurrentUserService;
import com.christian.taskmanager.service.SessionService;
import com.christian.taskmanager.util.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Session management endpoints")
public class SessionController {

    private final SessionService sessionService;
    private final CurrentUserService currentUserService;

    @Operation(summary = "Get sessions from current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions retrieved successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ApiResponseWrapper<List<SessionResponseDTO>> getSessions(
            @CookieValue(value = "refreshToken", required = false) String currentRefreshToken
    ) {
        Long currentUserId = currentUserService.getCurrentUserId();
        return ResponseUtils.success(sessionService.getSessions(currentUserId, currentRefreshToken));
    }

    @Operation(summary = "Revoke all sessions from current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All sessions revoked successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping
    public ApiResponseWrapper<String> revokeAllSessions() {
        Long currentUserId = currentUserService.getCurrentUserId();
        sessionService.revokeAllSessions(currentUserId);
        return ResponseUtils.success("All sessions revoked successfully");
    }

    @Operation(summary = "Revoke session by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions revoked successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @DeleteMapping("/{id}")
    public ApiResponseWrapper<String> revokeSession(@PathVariable Long id) {
        sessionService.revokeSession(id);
        return ResponseUtils.success("Session revoked successfully");
    }
}
