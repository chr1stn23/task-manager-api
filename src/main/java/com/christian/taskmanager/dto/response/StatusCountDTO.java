package com.christian.taskmanager.dto.response;

import com.christian.taskmanager.entity.TaskStatus;

public record StatusCountDTO(
        TaskStatus status,
        long count
) {
}
