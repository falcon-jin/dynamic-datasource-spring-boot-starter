package com.falcon.dynamic.datasource.strategy;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 切换数据库的随机策略
 *
 * @author falcon 
 * @since 1.0.0
 */
public class RandomDynamicDataSourceStrategy implements DynamicDataSourceStrategy {

    @Override
    public String determineKey(List<String> dsNames) {
        return dsNames.get(ThreadLocalRandom.current().nextInt(dsNames.size()));
    }
}
