package com.ai.api.validation.impl;

import com.ai.api.constant.AIConstant;
import com.ai.api.validation.SyllabusKind;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class SyllabusKindValidation implements ConstraintValidator<SyllabusKind, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(SyllabusKind constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, AIConstant.SYLLABUS_KIND_CHAPTER)
                || Objects.equals(value, AIConstant.SYLLABUS_KIND_LESSON);
    }
}
