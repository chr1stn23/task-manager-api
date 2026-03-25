package com.christian.taskmanager.util;

import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import com.christian.taskmanager.dto.response.ErrorResponse;

import java.time.Instant;

public class ResponseUtils {

    private ResponseUtils() {
    }

    public static <T> ApiResponseWrapper<T> success(T data) {
        return ApiResponseWrapper.<T>builder()
                .success(true)
                .timestamp(Instant.now())
                .data(data)
                .build();
    }

    public static ApiResponseWrapper<Void> error(String message, String code) {
        return ApiResponseWrapper.<Void>builder()
                .success(false)
                .timestamp(Instant.now())
                .error(
                        ErrorResponse.builder()
                                .message(message)
                                .code(code)
                                .build()
                )
                .build();
    }
}
