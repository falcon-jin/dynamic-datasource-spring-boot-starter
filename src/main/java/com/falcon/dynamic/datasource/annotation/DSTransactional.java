package com.falcon.dynamic.datasource.annotation;

import java.lang.annotation.*;

/**
 * 多数据源事物注解
 * 使用此注解 seata 要配置为 false 不然不会生效
 * 如果是分布式项目而且已经整合了seata 推荐开启设置seata=true 然后使用spring自带的事务注解就行
 * 此注解的拦截处理器是 DynamicLocalTransactionAdvisor
 * @author qy
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DSTransactional {
}
