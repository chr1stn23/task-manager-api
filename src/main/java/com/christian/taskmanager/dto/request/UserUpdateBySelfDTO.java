package com.christian.taskmanager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateBySelfDTO(
        @NotBlank String name,

        @NotBlank @Email String email,

        String password
) {
}
