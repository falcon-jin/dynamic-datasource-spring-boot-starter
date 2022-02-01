package com.falcon.dynamic.datasource.annotation;

import java.lang.annotation.*;

/**
 * @author falcon
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DS("master")
public @interface Master {
}