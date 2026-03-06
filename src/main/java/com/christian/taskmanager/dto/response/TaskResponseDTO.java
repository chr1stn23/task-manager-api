package com.christian.taskmanager.dto.response;

import com.christian.taskmanager.entity.Priority;
import com.christian.taskmanager.entity.TaskStatus;

import java.time.LocalDate;

public record TaskResponseDTO(
        Long id,
        String title,
        String description,
        TaskStatus status,
        Priority priority,
        LocalDate dueDate
) {
}
