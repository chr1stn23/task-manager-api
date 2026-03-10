package com.christian.taskmanager.dto.request;

import com.christian.taskmanager.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UserUpdateByAdminDTO(
        @NotBlank String name,

        @NotBlank @Email String email,

        List<Role> roles,

        Boolean enabled
) {
}
