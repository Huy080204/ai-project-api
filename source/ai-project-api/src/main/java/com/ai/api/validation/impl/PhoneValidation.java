package com.ai.api.validation.impl;

import com.ai.api.constant.AIConstant;
import com.ai.api.validation.PhoneConstraint;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneValidation implements ConstraintValidator<PhoneConstraint, String> {
    private boolean allowNull;

    @Override
    public void initialize(PhoneConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return StringUtils.isBlank(value) ? allowNull : StringUtils.isNotBlank(value) && value.matches(AIConstant.PHONE_PATTERN);
    }
}
