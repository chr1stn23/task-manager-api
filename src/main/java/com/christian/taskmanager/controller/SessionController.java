package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import com.christian.taskmanager.dto.response.SessionResponseDTO;
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

    @Operation(summary = "Get sessions for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions retrieved successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ApiResponseWrapper<List<SessionResponseDTO>> getSessions(
            @RequestParam Long userId,
            @CookieValue(value = "refreshToken", required = false) String currentRefreshToken
    ) {
        return ResponseUtils.success(sessionService.getSessions(userId, currentRefreshToken));
    }

    @Operation(summary = "Revoke session by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions revoked successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @DeleteMapping("/{sessionId}")
    public ApiResponseWrapper<String> revokeSession(@PathVariable Long sessionId) {
        sessionService.revokeSession(sessionId);
        return ResponseUtils.success("Session revoked successfully");
    }

    @Operation(summary = "Revoke all sessions from current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All sessions revoked successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping
    public ApiResponseWrapper<String> revokeAllSessions() {
        sessionService.revokeAllSessions();
        return ResponseUtils.success("All sessions revoked successfully");
    }
}
