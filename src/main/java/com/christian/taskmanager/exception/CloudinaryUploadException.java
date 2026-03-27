package com.christian.taskmanager.exception;

import lombok.Getter;

public class CloudinaryUploadException extends RuntimeException {

    @Getter
    private final String errorCode;

    public CloudinaryUploadException(String message) {
        super(message);
        this.errorCode = "CLOUDINARY_UPLOAD_ERROR";
    }

    public CloudinaryUploadException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CLOUDINARY_UPLOAD_ERROR";
    }
}
