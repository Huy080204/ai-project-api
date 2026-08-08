package com.ai.api.validation.impl;

import com.ai.api.constant.AIConstant;
import com.ai.api.validation.AssignmentState;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AssignmentStateValidation implements ConstraintValidator<AssignmentState, Integer> {

    private boolean allowNull;

    @Override
    public void initialize(AssignmentState constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer state, ConstraintValidatorContext constraintValidatorContext) {
        if (state == null) {
            return allowNull;
        }
        return AIConstant.ASSIGNMENT_STATE_DRAFT.equals(state)
                || AIConstant.ASSIGNMENT_STATE_PUBLISHED.equals(state)
                || AIConstant.ASSIGNMENT_STATE_CLOSED.equals(state);
    }
}
