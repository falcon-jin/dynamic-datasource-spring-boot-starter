package com.falcon.dynamic.datasource.spring.boot.autoconfigure.druid;

import com.alibaba.druid.filter.logging.LogFilter;
import com.falcon.dynamic.datasource.toolkit.DsConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Druid日志配置工具类
 *
 * @author falcon
 * @since 3.5.0
 */
@Slf4j
public final class DruidLogConfigUtil {

    private static final Map<String, Method> METHODS = DsConfigUtil.getSetterMethods(LogFilter.class);

    /**
     * 根据当前的配置和全局的配置生成druid的日志filter
     *
     * @param c 当前配置
     * @param g 全局配置
     * @return 日志filter
     */
    public static LogFilter initFilter(Class<? extends LogFilter> clazz, Map<String, Object> c, Map<String, Object> g) {
        try {
            LogFilter filter = clazz.newInstance();
            Map<String, Object> params = DsConfigUtil.mergeConfig(c, g);
            for (Map.Entry<String, Object> item : params.entrySet()) {
                String key = DsConfigUtil.lineToUpper(item.getKey());
                Method method = METHODS.get(key);
                if (method != null) {
                    try {
                        method.invoke(filter, item.getValue());
                    } catch (Exception e) {
                        log.warn("druid {} set param {} error", clazz.getName(), key, e);
                    }
                } else {
                    log.warn("druid {} does not have param {}", clazz.getName(), key);
                }
            }
            return filter;
        } catch (Exception e) {
            return null;
        }
    }
}
