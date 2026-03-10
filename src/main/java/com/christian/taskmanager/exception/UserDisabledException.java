package com.christian.taskmanager.exception;

import org.springframework.security.core.AuthenticationException;

public class UserDisabledException extends AuthenticationException {

    public UserDisabledException() {
        super("disabled");
    }

    public UserDisabledException(String message) {
        super(message);
    }
}
