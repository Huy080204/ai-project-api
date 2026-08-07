package com.ai.api.validation.impl;

import com.ai.api.constant.AIConstant;
import com.ai.api.validation.VoucherState;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class VoucherStateValidation implements ConstraintValidator<VoucherState, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(VoucherState constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, AIConstant.VOUCHER_STATE_PENDING)
                || Objects.equals(value, AIConstant.VOUCHER_STATE_ACTIVE)
                || Objects.equals(value, AIConstant.VOUCHER_STATE_DONE);
    }
}
