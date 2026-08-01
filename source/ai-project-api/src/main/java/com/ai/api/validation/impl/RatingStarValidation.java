package com.ai.api.validation.impl;

import com.ai.api.constant.AIConstant;
import com.ai.api.validation.RatingStar;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RatingStarValidation implements ConstraintValidator<RatingStar, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(RatingStar constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return value != null
                && value >= AIConstant.RATING_STAR_MIN
                && value <= AIConstant.RATING_STAR_MAX;
    }
}
