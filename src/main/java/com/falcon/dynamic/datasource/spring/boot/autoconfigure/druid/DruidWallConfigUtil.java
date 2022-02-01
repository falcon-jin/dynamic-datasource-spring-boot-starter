package com.falcon.dynamic.datasource.spring.boot.autoconfigure.druid;

import com.alibaba.druid.wall.WallConfig;
import com.falcon.dynamic.datasource.toolkit.DsConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 防火墙配置工具类
 *
 * @author falcon
 * @since 3.5.0
 */
@Slf4j
public final class DruidWallConfigUtil {

    private static final Map<String, Method> METHODS = DsConfigUtil.getSetterMethods(WallConfig.class);

    /**
     * 根据当前的配置和全局的配置生成druid防火墙配置
     *
     * @param c 当前配置
     * @param g 全局配置
     * @return 防火墙配置
     */
    public static WallConfig toWallConfig(Map<String, Object> c, Map<String, Object> g) {
        WallConfig wallConfig = new WallConfig();
        Map<String, Object> map = DsConfigUtil.mergeConfig(c, g);
        Object dir = map.get("dir");
        if (dir != null) {
            wallConfig.loadConfig(String.valueOf(dir));
        }
        for (Map.Entry<String, Object> item : map.entrySet()) {
            String key = DsConfigUtil.lineToUpper(item.getKey());
            Method method = METHODS.get(key);
            if (method != null) {
                try {
                    method.invoke(wallConfig, item.getValue());
                } catch (Exception e) {
                    log.warn("druid wall set param {} error", key, e);
                }
            } else {
                log.warn("druid wall does not have param {}", key);
            }
        }
        return wallConfig;
    }
}
