package com.falcon.dynamic.datasource.strategy;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 切换数据库的负载均衡策略
 *
 * @author falcon 
 * @since 1.0.0
 */
public class LoadBalanceDynamicDataSourceStrategy implements DynamicDataSourceStrategy {

    /**
     * 负载均衡计数器
     */
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public String determineKey(List<String> dsNames) {
        return dsNames.get(Math.abs(index.getAndAdd(1) % dsNames.size()));
    }
}
