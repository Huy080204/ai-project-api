package com.ai.api.validation;

import com.ai.api.validation.impl.VoucherStateValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VoucherStateValidation.class)
@Documented
public @interface VoucherState {
    boolean allowNull() default false;

    String message() default "Voucher state is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
