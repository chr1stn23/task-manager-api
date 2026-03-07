package com.christian.taskmanager.common;

import java.time.LocalDateTime;

public class ResponseUtil {

    public static <T> ApiResponseWrapper<T> success(T data) {
        return ApiResponseWrapper.<T>builder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    public static ApiResponseWrapper<Void> error(String message, String code) {
        return ApiResponseWrapper.<Void>builder()
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
