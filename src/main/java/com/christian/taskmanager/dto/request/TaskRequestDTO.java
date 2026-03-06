package com.christian.taskmanager.dto.request;

import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TaskRequestDTO(

        @NotBlank
        String title,

        String description,

        @NotNull
        TaskStatus status,

        @NotNull
        Priority priority,

        LocalDate dueDate
) {
}