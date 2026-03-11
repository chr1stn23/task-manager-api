package com.christian.taskmanager.service.model;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
