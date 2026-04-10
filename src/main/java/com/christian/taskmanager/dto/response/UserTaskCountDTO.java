package com.christian.taskmanager.dto.response;

public record UserTaskCountDTO(
        String nickName,
        long taskCount
) {
}
