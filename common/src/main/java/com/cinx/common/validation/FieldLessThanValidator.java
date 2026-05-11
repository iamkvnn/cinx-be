package com.cinx.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class FieldLessThanValidator implements ConstraintValidator<FieldLessThan, Object> {

    private String smallerField;
    private String largerField;

    @Override
    public void initialize(FieldLessThan constraintAnnotation) {
        this.smallerField = constraintAnnotation.smallerField();
        this.largerField = constraintAnnotation.largerField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            Field smallField = value.getClass().getDeclaredField(smallerField);
            smallField.setAccessible(true);
            Number smaller = (Number) smallField.get(value);

            Field largeField = value.getClass().getDeclaredField(largerField);
            largeField.setAccessible(true);
            Number larger = (Number) largeField.get(value);

            if (smaller != null && larger != null) {
                return smaller.doubleValue() < larger.doubleValue();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
