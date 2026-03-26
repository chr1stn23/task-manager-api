package com.christian.taskmanager.exception;

import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import com.christian.taskmanager.util.ResponseUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleNotFound(NotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseUtils.error(ex.getMessage(), "NOT_FOUND"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseUtils.error(ex.getMessage(), "ACCESS_DENIED"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleAccessDenied() {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseUtils.error("You do not have permission to access this resource", "ACCESS_DENIED"));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleEmailExists(EmailAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ResponseUtils.error(ex.getMessage(), "EMAIL_ALREADY_EXISTS"));
    }

    @ExceptionHandler(NickNameAlreadyExistsException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleNickNameExists(NickNameAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ResponseUtils.error(ex.getMessage(), "NICKNAME_ALREADY_EXISTS"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ResponseUtils.error(ex.getMessage(), "INVALID_CREDENTIALS"));
    }

    @ExceptionHandler(UserDisabledException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleUserDisabled(UserDisabledException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ResponseUtils.error(ex.getMessage(), "USER_DISABLED"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation error");

        return ResponseEntity
                .badRequest()
                .body(ResponseUtils.error(message, "VALIDATION_ERROR"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        String message;

        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            Object[] enumValues = ex.getRequiredType().getEnumConstants();
            message = "Invalid value for '" + ex.getName() + "'. Allowed values: " + Arrays.toString(enumValues);
        } else {
            message = "Invalid value for parameter: " + ex.getName();
        }

        return ResponseEntity
                .badRequest()
                .body(ResponseUtils.error(message, "INVALID_PARAMETER"));
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleRefreshTokenException(RefreshTokenException ex) {
       return ResponseEntity
               .status(HttpStatus.UNAUTHORIZED)
               .body(ResponseUtils.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseUtils.error(ex.getMessage(), "INTERNAL_ERROR"));
    }
}
