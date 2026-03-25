package com.christian.taskmanager.dto.response;

import java.time.Instant;

public record SessionResponseDTO(
        Long id,
        String deviceName,
        String ipAddress,
        String agentName,
        Instant createdAt,
        boolean current
) {
}
