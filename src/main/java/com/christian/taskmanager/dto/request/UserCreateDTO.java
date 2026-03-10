package com.christian.taskmanager.dto.request;

import com.christian.taskmanager.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserCreateDTO(
        @NotBlank String name,

        @NotBlank @Email String email,

        @NotBlank @Size(min = 8) String password,

        @NotEmpty List<Role> roles,

        boolean enabled
) {
}
