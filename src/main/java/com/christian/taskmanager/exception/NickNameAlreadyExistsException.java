package com.christian.taskmanager.exception;

public class NickNameAlreadyExistsException extends RuntimeException{

    public NickNameAlreadyExistsException(String message) {
        super(message);
    }
}
