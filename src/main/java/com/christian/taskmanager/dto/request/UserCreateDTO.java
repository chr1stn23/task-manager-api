package com.christian.taskmanager.dto.request;

import com.christian.taskmanager.entity.Role;
import jakarta.validation.constraints.*;

import java.util.List;

public record UserCreateDTO(
        @NotBlank String name,

        @NotBlank @Email String email,

        @NotBlank
        @Size(min = 8, max = 20)
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!]+$",
                message = "must contain at least one uppercase, one lowercase, one digit, and one special character " +
                        "(@#$%^&+=!)"
        )
        String password,

        @NotEmpty List<Role> roles,

        Boolean enabled
) {
}
