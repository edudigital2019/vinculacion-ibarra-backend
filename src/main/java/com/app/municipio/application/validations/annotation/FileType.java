package com.app.municipio.application.validations.annotation;

import com.app.municipio.application.validations.validator.FileTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileTypeValidator.class)
@Documented
public @interface FileType {
    String message() default "Tipo de archivo no permitido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] value(); // extensiones permitidas, ej: {"jpg", "pdf"}
}
