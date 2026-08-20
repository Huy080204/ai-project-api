package com.ai.api.validation;

import com.ai.api.validation.impl.ReactionTypeValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReactionTypeValidation.class)
@Documented
public @interface ReactionTypeConstraint {
    boolean allowNull() default false;

    String message() default "Reaction type is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
