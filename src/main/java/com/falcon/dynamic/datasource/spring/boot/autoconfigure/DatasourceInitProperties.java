package com.falcon.dynamic.datasource.spring.boot.autoconfigure;

import lombok.Data;

/**
 * 动态数据源初始化脚本配置
 *
 * @author falcon
 * @since 3.5.0
 */
@Data
public class DatasourceInitProperties {

    /**
     * 自动运行的建表脚本
     */
    private String schema;
    /**
     * 自动运行的数据脚本
     */
    private String data;

    /**
     * 错误是否继续 默认 true
     */
    private boolean continueOnError = true;
    /**
     * 分隔符 默认 ;
     */
    private String separator = ";";
}
