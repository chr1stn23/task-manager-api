package com.christian.taskmanager.dto.response;

import java.util.List;

public record UserResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String nickName,
        String profileImageUrl,
        String email,
        List<String> roles,
        boolean enabled
) {
}
