package com.falcon.dynamic.datasource.strategy;

import java.util.List;

/**
 * 动态数据源切换策略接口
 *
 * @author falcon 
 * @see RandomDynamicDataSourceStrategy
 * @see LoadBalanceDynamicDataSourceStrategy
 * @since 1.0.0
 */
public interface DynamicDataSourceStrategy {

    /**
     * 根据给定的数据源确定数据库
     *
     * @param dsNames given dataSources
     * @return final dataSource
     */
    String determineKey(List<String> dsNames);
}
