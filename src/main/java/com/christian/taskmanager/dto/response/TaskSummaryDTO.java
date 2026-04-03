package com.christian.taskmanager.dto.response;

public record TaskSummaryDTO(
        long total,
        long todo,
        long inProgress,
        long done,
        long low,
        long medium,
        long high,
        long deleted
) {}