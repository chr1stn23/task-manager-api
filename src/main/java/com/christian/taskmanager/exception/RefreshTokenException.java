package com.christian.taskmanager.exception;

import lombok.Getter;

@Getter
public class RefreshTokenException extends RuntimeException {

    private final String errorCode;

    public RefreshTokenException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public static class RefreshTokenExpiredException extends RefreshTokenException {
        public RefreshTokenExpiredException(String token) {
            super("The token [" + token + "] is expired.", "REFRESH_TOKEN_EXPIRED");
        }
    }

    public static class RefreshTokenRevokedException extends RefreshTokenException {
        public RefreshTokenRevokedException(String token) {
            super("The token [" + token + "] is revoked or invalid.", "REFRESH_TOKEN_REVOKED");
        }
    }
}
