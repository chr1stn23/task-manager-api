package com.christian.taskmanager.exception.admin;


import lombok.Getter;

@Getter
public class AdminBusinessException extends RuntimeException{

    private final AdminErrorCode code;

    public AdminBusinessException(AdminErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }
}
