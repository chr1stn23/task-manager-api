package com.christian.taskmanager.dto.response;

import java.util.List;

public record AdminDashboardDTO(
        long activeUsers,
        long totalTasks,
        long liveSessions,
        List<StatusCountDTO> tasksByStatus,
        List<PriorityCountDTO> tasksByPriority,
        List<UserTaskCountDTO> topUsers
) {
}
