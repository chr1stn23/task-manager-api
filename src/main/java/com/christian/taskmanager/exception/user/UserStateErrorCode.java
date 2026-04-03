package com.christian.taskmanager.exception.user;

import lombok.Getter;

@Getter
public enum UserStateErrorCode {
    USER_ALREADY_DISABLED("User already disabled"),
    USER_ALREADY_ENABLED("User already enabled");

    private final String message;

    UserStateErrorCode(String message) {
        this.message = message;
    }
}
