package com.christian.taskmanager.dto.response;

public record UserListResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String nickName,
        String email,
        String profileImageUrl,
        boolean enabled
) {
}