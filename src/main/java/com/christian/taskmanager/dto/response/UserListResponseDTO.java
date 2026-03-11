package com.christian.taskmanager.dto.response;

public record UserListResponseDTO(
        Long id,
        String name,
        String email,
        boolean enabled
) {
}