package com.christian.taskmanager.mapper;

import com.christian.taskmanager.dto.request.TaskRequestDTO;
import com.christian.taskmanager.dto.response.TaskResponseDTO;
import com.christian.taskmanager.entity.Task;

public class TaskMapper {

    public static Task toEntity(TaskRequestDTO dto) {
        return Task.builder()
                .title(dto.title())
                .description(dto.description())
                .status(dto.status())
                .priority(dto.priority())
                .dueDate(dto.dueDate())
                .build();
    }

    public static TaskResponseDTO toDTO(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate()
        );
    }
}
