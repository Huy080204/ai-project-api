package com.ai.api.validation;

import com.ai.api.validation.impl.RatingStarValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RatingStarValidation.class)
@Documented
public @interface RatingStar {
    boolean allowNull() default false;

    String message() default "Rating star must be between 1 and 5";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
