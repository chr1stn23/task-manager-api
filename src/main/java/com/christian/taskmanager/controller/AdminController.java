package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.request.UserCreateDTO;
import com.christian.taskmanager.dto.request.UserUpdateByAdminDTO;
import com.christian.taskmanager.dto.response.*;
import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.TaskStatus;
import com.christian.taskmanager.service.SessionService;
import com.christian.taskmanager.service.TaskService;
import com.christian.taskmanager.service.UserService;
import com.christian.taskmanager.util.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin management endpoints")
public class AdminController {

    private final UserService userService;
    private final TaskService taskService;
    private final SessionService sessionService;

    // ===================================================
    //                       USERS
    // ===================================================

    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/users")
    public ApiResponseWrapper<UserResponseDTO> createUser(@RequestBody @Valid UserCreateDTO request) {
        return ResponseUtils.success(userService.create(request));
    }

    @Operation(summary = "Get users with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/users")
    public ApiResponseWrapper<Page<UserListResponseDTO>> getUsers(
            @Parameter(description = "Filter by name")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filter by email")
            @RequestParam(required = false) String email,
            @Parameter(description = "Filter by enabled status")
            @RequestParam(required = false) Boolean enabled,
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseUtils.success(userService.getUsers(name, email, enabled, pageable));
    }

    @Operation(summary = "Get user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/users/{id}")
    public ApiResponseWrapper<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseUtils.success(userService.getUserById(id));
    }

    @Operation(summary = "Update user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/users/{id}")
    public ApiResponseWrapper<UserResponseDTO> updateUserById(@PathVariable Long id,
            @RequestBody @Valid UserUpdateByAdminDTO request) {
        return ResponseUtils.success(userService.updateByAdmin(id, request));
    }

    @Operation(summary = "Disable user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User disabled successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/users/{id}/disable")
    public ApiResponseWrapper<String> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return ResponseUtils.success("User disabled successfully");
    }

    @Operation(summary = "Enable user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User enabled successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/users/{id}/enable")
    public ApiResponseWrapper<String> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return ResponseUtils.success("User enabled successfully");
    }

    // ===================================================
    //                       TASKS
    // ===================================================

    @Operation(summary = "Get tasks with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/tasks")
    public ApiResponseWrapper<Page<TaskResponseDTO>> getTasks(
            @Parameter(description = "Filter by deleted status")
            @RequestParam(required = false) Boolean deleted,
            @Parameter(description = "Filter by task status")
            @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Filter by task priority")
            @RequestParam(required = false) Priority priority,
            @Parameter(description = "Filter by user ID")
            @RequestParam(required = false) Long userId,
            @PageableDefault(sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseUtils.success(taskService.getTasks(deleted, status, priority, userId, pageable));
    }

    // ===================================================
    //                       SESSIONS
    // ===================================================

    @Operation(summary = "Get sessions from a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions retrieved successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/users/{userId}/sessions")
    public ApiResponseWrapper<List<SessionResponseDTO>> getSessions(
            @PathVariable Long userId,
            @CookieValue(value = "refreshToken", required = false) String currentRefreshToken
    ) {
        return ResponseUtils.success(sessionService.getSessions(userId, currentRefreshToken));
    }

    @Operation(summary = "Revoke all sessions from a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All sessions revoked successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/users/{userId}/sessions")
    public ApiResponseWrapper<String> revokeAllSessions(@PathVariable Long userId) {
        sessionService.revokeAllSessions(userId);
        return ResponseUtils.success("All sessions revoked successfully");
    }

    @Operation(summary = "Revoke session by ID and userID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions revoked successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @DeleteMapping("/users/{userId}/sessions/{sessionId}")
    public ApiResponseWrapper<String> revokeSession(@PathVariable Long userId, @PathVariable Long sessionId) {
        sessionService.adminRevokeSession(userId, sessionId);
        return ResponseUtils.success("Session revoked successfully");
    }
}
