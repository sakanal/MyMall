package com.sakanal.common.validator.constraint;


import com.sakanal.common.validator.ListValue;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private final Set<Integer> set = new HashSet<>();

    //初始化方法
    @Override
    public void initialize(ListValue constraintAnnotation) {
        // values：注解上传入的值
        int[] values = constraintAnnotation.values();
        for (int value: values) {
            set.add(value);
        }
    }

    /**
     * @param value   需要校验的值
     * @param context
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {

        return set.contains(value);
    }
}

