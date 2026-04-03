package com.christian.taskmanager.exception.user;

import lombok.Getter;

@Getter
public class UserBusinessException extends RuntimeException{

    private final UserStateErrorCode code;

    public UserBusinessException(UserStateErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

}
