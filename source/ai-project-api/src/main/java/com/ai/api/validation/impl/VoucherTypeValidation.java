package com.ai.api.validation.impl;

import com.ai.api.constant.AIConstant;
import com.ai.api.validation.VoucherType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class VoucherTypeValidation implements ConstraintValidator<VoucherType, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(VoucherType constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, AIConstant.VOUCHER_TYPE_PERCENT)
                || Objects.equals(value, AIConstant.VOUCHER_TYPE_FIXED_AMOUNT);
    }
}
