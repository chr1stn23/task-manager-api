package com.christian.taskmanager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProfileImageValidator.class)
public @interface ValidProfileImage {
    String message() default "Invalid profile image";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
