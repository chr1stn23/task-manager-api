package com.christian.taskmanager.exception;

import com.christian.taskmanager.dto.response.ApiResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should return 404 when NotFoundException is thrown")
    void shouldHandleNotFoundException() {
        // Arrange
        NotFoundException ex = new NotFoundException("Task not found");

        // Act
        ResponseEntity<ApiResponseWrapper<Void>> response = handler.handleNotFound(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Task not found", response.getBody().getError().getMessage());
        assertEquals("NOT_FOUND", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should return 403 when UnauthorizedException is thrown")
    void shouldHandleUnauthorizedException() {
        // Arrange
        UnauthorizedException ex = new UnauthorizedException("Not authorized");

        // Act
        ResponseEntity<ApiResponseWrapper<Void>> response = handler.handleUnauthorized(ex);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not authorized", response.getBody().getError().getMessage());
        assertEquals("ACCESS_DENIED", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should return 403 when AccessDeniedException is thrown")
    void shouldHandleAccessDeniedException() {
        // Act
        ResponseEntity<ApiResponseWrapper<Void>> response = handler.handleAccessDenied();

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCESS_DENIED", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should return 409 when EmailAlreadyExistsException is thrown")
    void shouldHandleEmailAlreadyExistsException() {
        // Arrange
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email already exists");

        // Act
        ResponseEntity<ApiResponseWrapper<Void>> response = handler.handleEmailExists(ex);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("EMAIL_ALREADY_EXISTS", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should return 401 when InvalidCredentialsException is thrown")
    void shouldHandleInvalidCredentialsException() {
        // Arrange
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ApiResponseWrapper<Void>> response = handler.handleInvalidCredentials(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_CREDENTIALS", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should return 401 when UserDisabledException is thrown")
    void shouldHandleUserDisabledException() {
        // Arrange
        UserDisabledException ex = new UserDisabledException("User disabled");

        // Act
        ResponseEntity<ApiResponseWrapper<Void>> response = handler.handleUserDisabled(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("USER_DISABLED", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should return 400 with field error when MethodArgumentNotValidException is thrown")
    void shouldHandleMethodArgumentNotValidException() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "email", "must not be blank");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<ApiResponseWrapper<Void>> response = handler.handleValidationErrors(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getError().getCode());
        assertTrue(response.getBody().getError().getMessage().contains("email"));
    }

    @Test
    @DisplayName("Should return 400 with fallback message when no field errors")
    void shouldHandleMethodArgumentNotValidExceptionWithNoFieldErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // Act
        ResponseEntity<ApiResponseWrapper<Void>> response = handler.handleValidationErrors(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation error", response.getBody().getError().getMessage());
    }

    @Test
    @DisplayName("Should return 400 with enum values when type mismatch on enum")
    void shouldHandleTypeMismatchWithEnum() {
        // Arrange
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getRequiredType()).thenAnswer(i -> TestEnum.class);
        when(ex.getName()).thenReturn("status");

        // Act
        ResponseEntity<ApiResponseWrapper<Void>> response = handler.handleTypeMismatch(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_PARAMETER", response.getBody().getError().getCode());
        assertTrue(response.getBody().getError().getMessage().contains("status"));
    }

    @Test
    @DisplayName("Should return 400 with parameter name when type mismatch on non-enum")
    void shouldHandleTypeMismatchWithNonEnum() {
        // Arrange
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getRequiredType()).thenReturn(null);
        when(ex.getName()).thenReturn("id");

        // Act
        ResponseEntity<ApiResponseWrapper<Void>> response = handler.handleTypeMismatch(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_PARAMETER", response.getBody().getError().getCode());
        assertTrue(response.getBody().getError().getMessage().contains("id"));
    }

    @Test
    @DisplayName("Should return 500 when generic Exception is thrown")
    void shouldHandleGenericException() {
        // Arrange
        Exception ex = new Exception("Unexpected error");

        // Act
        ResponseEntity<ApiResponseWrapper<Void>> response = handler.handleGeneric(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getError().getCode());
    }

    enum TestEnum {ACTIVE, INACTIVE}
}