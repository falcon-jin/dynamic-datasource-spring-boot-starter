package com.falcon.dynamic.datasource.event;

import com.falcon.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;

import javax.sql.DataSource;

/**
 * 多数据源连接池创建事件
 *
 * @author falcon
 * @since 3.5.0
 */
public interface DataSourceInitEvent {

    /**
     * 连接池创建前执行（可用于参数解密）
     *
     * @param dataSourceProperty 数据源基础信息
     */
    void beforeCreate(DataSourceProperty dataSourceProperty);

    /**
     * 连接池创建后执行
     *
     * @param dataSource 连接池
     */
    void afterCreate(DataSource dataSource);
}
