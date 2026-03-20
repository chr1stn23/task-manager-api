package com.christian.taskmanager.dto.request;

import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequestDTO(

        @NotBlank(message = "El título es obligatorio")
        @Size(min = 3, max = 150, message = "El título debe tener entre 3 y 150 caracteres")
        String title,

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        String description,

        @NotNull(message = "El estado es obligatorio")
        TaskStatus status,

        @NotNull(message = "La prioridad es obligatoria")
        Priority priority,

        LocalDate dueDate
) {
}