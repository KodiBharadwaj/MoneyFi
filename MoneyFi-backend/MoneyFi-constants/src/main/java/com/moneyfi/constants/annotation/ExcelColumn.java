package com.moneyfi.constants.annotation;

import com.moneyfi.constants.enums.ExcelWidthType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelColumn {
    String header();
    int order() default 0;
    ExcelWidthType widthType() default ExcelWidthType.DYNAMIC;
    int width() default 20;
    String dateFormat() default "yyyy-MM-dd HH:mm:ss";
    boolean ignore() default false;
}
