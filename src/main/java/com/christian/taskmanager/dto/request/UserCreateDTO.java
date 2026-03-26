package com.christian.taskmanager.dto.request;

import com.christian.taskmanager.entity.Role;
import jakarta.validation.constraints.*;

import java.util.List;

public record UserCreateDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        String firstName,

        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        String lastName,

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(max = 30, message = "El nombre de usuario no puede exceder 30 caracteres")
        String nickName,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es válido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 20, message = "La contraseña debe tener entre 8 y 20 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!]+$",
                message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial (@#$%^&+=!)"
        )
        String password,

        @NotEmpty(message = "Debe asignar al menos un rol")
        List<@NotNull(message = "Rol inválido") Role> roles,

        Boolean enabled
) {
}
