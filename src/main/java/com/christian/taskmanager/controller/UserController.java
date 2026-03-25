package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.request.PasswordChangeRequestDTO;
import com.christian.taskmanager.dto.request.UserUpdateBySelfDTO;
import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import com.christian.taskmanager.dto.response.UserResponseDTO;
import com.christian.taskmanager.security.CurrentUserService;
import com.christian.taskmanager.service.UserService;
import com.christian.taskmanager.util.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    @Operation(summary = "Get current user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ApiResponseWrapper<UserResponseDTO> getCurrentUser() {
        Long currentUserId = currentUserService.getCurrentUserId();
        return ResponseUtils.success(userService.getUserById(currentUserId));
    }

    @Operation(summary = "Update current user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/me")
    public ApiResponseWrapper<UserResponseDTO> updateCurrentUser(@RequestBody @Valid UserUpdateBySelfDTO request) {
        return ResponseUtils.success(userService.updateBySelf(request));
    }

    @Operation(summary = "Change current user password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid current password or request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/me/change-password")
    public ApiResponseWrapper<String> changePassword(@RequestBody @Valid PasswordChangeRequestDTO request) {
        userService.changePassword(request);
        return ResponseUtils.success("Password updated successfully");
    }

    @Operation(summary = "Disable current user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User disabled successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/me/disable")
    public ApiResponseWrapper<String> disableUser() {
        Long currentUserId = currentUserService.getCurrentUserId();
        userService.disableUser(currentUserId);
        return ResponseUtils.success("User disabled successfully");
    }
}
