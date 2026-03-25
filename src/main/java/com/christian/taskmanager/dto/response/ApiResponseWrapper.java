package com.christian.taskmanager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ApiResponseWrapper<T> {
    private boolean success;
    private Instant timestamp;
    private T data;
    private ErrorResponse error;
}
