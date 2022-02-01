package com.falcon.dynamic.datasource.spring.boot.autoconfigure.druid;

import com.alibaba.druid.filter.stat.StatFilter;
import com.falcon.dynamic.datasource.toolkit.DsConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Druid监控配置工具类
 *
 * @author falcon
 * @since 3.5.0
 */
@Slf4j
public final class DruidStatConfigUtil {

    private static final Map<String, Method> METHODS = DsConfigUtil.getSetterMethods(StatFilter.class);

    static {
        try {
            METHODS.put("dbType", StatFilter.class.getDeclaredMethod("setDbType", String.class));
        } catch (Exception ignore) {
        }
    }

    /**
     * 根据当前的配置和全局的配置生成druid防火墙配置
     *
     * @param c 当前配置
     * @param g 全局配置
     * @return StatFilter
     */
    public static StatFilter toStatFilter(Map<String, Object> c, Map<String, Object> g) {
        StatFilter filter = new StatFilter();
        Map<String, Object> map = DsConfigUtil.mergeConfig(c, g);
        for (Map.Entry<String, Object> item : map.entrySet()) {
            String key = DsConfigUtil.lineToUpper(item.getKey());
            Method method = METHODS.get(key);
            if (method != null) {
                try {
                    method.invoke(filter, item.getValue());
                } catch (Exception e) {
                    log.warn("druid stat set param {} error", key, e);
                }
            } else {
                log.warn("druid stat does not have param {}", key);
            }
        }
        return filter;
    }
}
