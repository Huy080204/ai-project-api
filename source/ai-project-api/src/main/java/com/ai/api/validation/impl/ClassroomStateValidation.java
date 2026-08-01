package com.ai.api.validation.impl;

import com.ai.api.constant.AIConstant;
import com.ai.api.validation.ClassroomState;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class ClassroomStateValidation implements ConstraintValidator<ClassroomState, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(ClassroomState constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, AIConstant.CLASSROOM_STATE_PENDING)
                || Objects.equals(value, AIConstant.CLASSROOM_STATE_ACTIVE)
                || Objects.equals(value, AIConstant.CLASSROOM_STATE_DONE)
                || Objects.equals(value, AIConstant.CLASSROOM_STATE_CANCEL);
    }
}
