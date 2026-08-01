package com.ai.api.validation.impl;

import com.ai.api.constant.AIConstant;
import com.ai.api.validation.ClassroomStudentState;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class ClassroomStudentStateValidation implements ConstraintValidator<ClassroomStudentState, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(ClassroomStudentState constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, AIConstant.CLASSROOM_STUDENT_STATE_PENDING)
                || Objects.equals(value, AIConstant.CLASSROOM_STUDENT_STATE_ACCEPT)
                || Objects.equals(value, AIConstant.CLASSROOM_STUDENT_STATE_REJECT);
    }
}
