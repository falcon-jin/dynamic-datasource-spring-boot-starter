package com.falcon.dynamic.datasource.annotation;


import java.lang.annotation.*;

/**
 * 切换数据源的核心注释。
 * 注解加在service类上或方法上 (保证一个service下通过ioc注入的mapper属于同一个数据源)
 * 加在mapper上会导致数据源切换失败
 * @author falcon
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DS {

    /**
     * groupName 或特定的数据库名称或 spring SPEL 名称。
     *
     * @return the database you want to switch
     */
    String value();
}