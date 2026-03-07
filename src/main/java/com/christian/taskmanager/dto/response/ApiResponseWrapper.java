package com.christian.taskmanager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiResponseWrapper<T> {
    private boolean success;
    private LocalDateTime timestamp;
    private T data;
    private ErrorResponse error;
}
