package com.ai.api.validation.impl;

import com.ai.api.validation.DateRange;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Date;

@Slf4j
public class DateRangeValidation implements ConstraintValidator<DateRange, Object> {
    private String startField;
    private String endField;

    @Override
    public void initialize(DateRange constraintAnnotation) {
        startField = constraintAnnotation.startField();
        endField = constraintAnnotation.endField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            Field startDateField = value.getClass().getDeclaredField(startField);
            Field endDateField = value.getClass().getDeclaredField(endField);
            startDateField.setAccessible(true);
            endDateField.setAccessible(true);
            Date startDate = (Date) startDateField.get(value);
            Date endDate = (Date) endDateField.get(value);
            if (startDate == null || endDate == null) {
                return true;
            }
            return endDate.after(startDate);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.warn("Failed to validate date range for fields '{}' / '{}'", startField, endField, e);
            return false;
        }
    }
}
