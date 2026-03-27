package com.christian.taskmanager.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileImageValidatorTest {

    private final ProfileImageValidator validator = new ProfileImageValidator();

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @Test
    @DisplayName("Should return false when file is null")
    void shouldReturnFalseWhenFileIsNull() {
        // Arrange
        when(context.buildConstraintViolationWithTemplate(any()))
                .thenReturn(builder);

        // Act
        boolean result = validator.isValid(null, context);

        // Assert
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("La imagen de perfil es obligatoria");
    }

    @Test
    @DisplayName("Should return false when file is empty")
    void shouldReturnFalseWhenFileIsEmpty() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        when(context.buildConstraintViolationWithTemplate(any()))
                .thenReturn(builder);

        // Act
        boolean result = validator.isValid(file, context);

        // Assert
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("La imagen de perfil es obligatoria");
    }

    @Test
    @DisplayName("Should return false when type is invalid")
    void shouldReturnFalseWhenTypeIsInvalid() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");

        when(context.buildConstraintViolationWithTemplate(any()))
                .thenReturn(builder);

        // Act
        boolean result = validator.isValid(file, context);

        // Assert
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Solo se permiten archivos JPEG, PNG y WebP");
    }

    @Test
    @DisplayName("Should return false when content type is null")
    void shouldReturnFalseWhenContentTypeIsNull() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(null);

        when(context.buildConstraintViolationWithTemplate(any()))
                .thenReturn(builder);

        // Act
        boolean result = validator.isValid(file, context);

        // Assert
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Solo se permiten archivos JPEG, PNG y WebP");
    }

    @Test
    @DisplayName("Should return false when size exceeds limit")
    void shouldReturnFalseWhenSizeExceedsLimit() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(3 * 1024 * 1024L);

        when(context.buildConstraintViolationWithTemplate(any()))
                .thenReturn(builder);

        // Act
        boolean result = validator.isValid(file, context);

        // Assert
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("El tamaño del archivo no puede superar los 5 MB");
    }

    @Test
    @DisplayName("Should return true when file is valid")
    void shouldReturnTrueWhenFileIsValid() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(1024 * 1024L);

        // Act
        boolean result = validator.isValid(file, context);

        // Assert
        assertTrue(result);
        verify(context, never()).buildConstraintViolationWithTemplate(any());
    }
}