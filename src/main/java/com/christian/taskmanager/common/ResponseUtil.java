package com.christian.taskmanager.common;

import java.time.LocalDateTime;

public class ResponseUtil {

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    public static ApiResponse<Void> error(String message, String code) {
        return ApiResponse.<Void>builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .error(
                        ErrorResponse.builder()
                                .message(message)
                                .code(code)
                                .build()
                )
                .build();
    }
}
