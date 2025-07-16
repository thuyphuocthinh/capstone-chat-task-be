package com.tpt.chat_task.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {
    private Enum<?>[] enumValues;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        enumValues = constraintAnnotation.enumClass().getEnumConstants();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false; // Hoặc cho phép null tùy logic của bạn
        return Arrays.stream(enumValues).anyMatch(e -> e.name().equals(value));
    }
}