package com.christian.taskmanager.exception;

import org.springframework.security.core.AuthenticationException;

public class TokenProcessingException extends AuthenticationException {

    public TokenProcessingException() {
        super("processing_error");
    }
}
