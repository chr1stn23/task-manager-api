package com.christian.taskmanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class ProfileImageValidator implements ConstraintValidator<ValidProfileImage, MultipartFile> {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/webp"};

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            addConstraintViolation(context, "La imagen de perfil es obligatoria");
            return false;
        }

        String contentType = file.getContentType();
        if (!isAllowedType(contentType)) {
            addConstraintViolation(context, "Solo se permiten archivos JPEG, PNG y WebP");
            return false;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            addConstraintViolation(context, "El tamaño del archivo no puede superar los 5 MB");
            return false;
        }

        return true;
    }

    private boolean isAllowedType(String contentType) {
        if (contentType == null) return false;
        for (String allowed : ALLOWED_TYPES) {
            if (contentType.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
