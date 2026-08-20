package com.ai.api.validation.impl;

import com.ai.api.constant.AIConstant;
import com.ai.api.validation.JobPostingState;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class JobPostingStateValidation implements ConstraintValidator<JobPostingState, Integer> {

    private boolean allowNull;

    @Override
    public void initialize(JobPostingState constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer state, ConstraintValidatorContext constraintValidatorContext) {
        if (state == null) {
            return allowNull;
        }
        return AIConstant.JOB_POSTING_STATE_OPEN.equals(state)
                || AIConstant.JOB_POSTING_STATE_CLOSED.equals(state);
    }
}
