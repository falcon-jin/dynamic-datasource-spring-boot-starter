package com.falcon.dynamic.datasource.spring.boot.autoconfigure;

import lombok.Data;
import org.springframework.core.Ordered;

/**
 * 多数据源aop相关配置
 *
 * @author falcon
 */
@Data
public class DynamicDatasourceAopProperties {

    /**
     * 是否启用@DS注解 默认启用
     */
    private Boolean enabled = true;
    /**
     * aop 拦截顺序
     */
    private Integer order = Ordered.HIGHEST_PRECEDENCE;
    /**
     * aop 只允许公共的方法, 默认为true
     */
    private Boolean allowedPublicOnly = true;
}
