package com.falcon.dynamic.datasource.spring.boot.autoconfigure;

/**
 * 自定义配置属性类
 * @author falcon
 * @since 3.4.0
 */
public interface DynamicDataSourcePropertiesCustomizer {

    /**
     * 自定义给定的DynamicDataSourceProperties对象。{@link DynamicDataSourceProperties} object.
     *
     * @param properties 自定义的 DynamicDataSourceProperties 对象
     */
    void customize(DynamicDataSourceProperties properties);
}
