package com.ai.api.validation;

import com.ai.api.validation.impl.ClassroomStateValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ClassroomStateValidation.class)
@Documented
public @interface ClassroomState {
    boolean allowNull() default false;

    String message() default "Classroom state is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
