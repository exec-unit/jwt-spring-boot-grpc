package com.execunit.jwt.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Documented
@Constraint(validatedBy = PublicKeyFileValidator.Validator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicKeyFileValidator {
    String message() default "Public key file must exist and be readable";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<PublicKeyFileValidator, Path> {
        @Override
        public boolean isValid(Path path, ConstraintValidatorContext context) {
            if (path == null) {
                return true;
            }
            return Files.exists(path) && Files.isReadable(path);
        }
    }
}