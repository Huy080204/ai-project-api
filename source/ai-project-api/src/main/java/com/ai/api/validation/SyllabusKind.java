package com.ai.api.validation;

import com.ai.api.validation.impl.SyllabusKindValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SyllabusKindValidation.class)
@Documented
public @interface SyllabusKind {
    boolean allowNull() default false;

    String message() default "Syllabus kind is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
