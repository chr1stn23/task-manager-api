package com.christian.taskmanager.common;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiResponse<T> {
    private boolean success;
    private LocalDateTime timestamp;
    private T data;
    private ErrorResponse error;
}
