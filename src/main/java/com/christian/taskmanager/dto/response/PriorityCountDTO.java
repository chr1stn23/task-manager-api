package com.christian.taskmanager.dto.response;

import com.christian.taskmanager.entity.Priority;

public record PriorityCountDTO(
        Priority priority,
        long count
) {
}
