package com.ai.api.validation;

import com.ai.api.validation.impl.DateRangeValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidation.class)
@Documented
public @interface DateRange {
    String startField();

    String endField();

    String message() default "End date must not be before start date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
