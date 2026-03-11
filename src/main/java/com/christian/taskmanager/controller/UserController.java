package com.christian.taskmanager.controller;

import com.christian.taskmanager.dto.request.UserCreateDTO;
import com.christian.taskmanager.dto.request.UserUpdateByAdminDTO;
import com.christian.taskmanager.dto.request.UserUpdateBySelfDTO;
import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import com.christian.taskmanager.dto.response.UserListResponseDTO;
import com.christian.taskmanager.dto.response.UserResponseDTO;
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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponseWrapper<UserResponseDTO> createUser(@RequestBody @Valid UserCreateDTO request) {
        return ResponseUtils.success(userService.create(request));
    }

    @Operation(summary = "Get users with optional filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
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
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public ApiResponseWrapper<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseUtils.success(userService.getUserById(id));
    }

    @Operation(summary = "Update user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ApiResponseWrapper<UserResponseDTO> updateUserById(@PathVariable Long id,
            @RequestBody @Valid UserUpdateByAdminDTO request) {
        return ResponseUtils.success(userService.updateByAdmin(id, request));
    }

    @Operation(summary = "Update current user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PatchMapping("/me")
    public ApiResponseWrapper<UserResponseDTO> updateCurrentUser(@RequestBody @Valid UserUpdateBySelfDTO request) {
        return ResponseUtils.success(userService.updateBySelf(request));
    }

    @Operation(summary = "Disable user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User disabled successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PatchMapping("/{id}/disable")
    public ApiResponseWrapper<String> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return ResponseUtils.success("User disabled successfully");
    }

    @Operation(summary = "Enable user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User enabled successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/enable")
    public ApiResponseWrapper<String> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return ResponseUtils.success("User enabled successfully");
    }
}
