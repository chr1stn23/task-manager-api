package com.christian.taskmanager.dto.response;

import java.time.LocalDateTime;

public record SessionResponseDTO(
        Long id,
        String deviceName,
        String ipAddress,
        String agentName,
        LocalDateTime createdAt,
        boolean current
) {
}
