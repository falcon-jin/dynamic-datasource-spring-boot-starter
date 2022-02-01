package com.falcon.dynamic.datasource.aop;


import com.falcon.dynamic.datasource.processor.DsProcessor;
import com.falcon.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.ClassUtils;
import org.springframework.util.PatternMatchUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 动态数据源的命名拦截器
 *
 * @author falcon
 * @since 3.4.0
 */
@Slf4j
public class DynamicDatasourceNamedInterceptor implements MethodInterceptor {

    private static final String DYNAMIC_PREFIX = "#";
    private final Map<String, String> nameMap = new HashMap<>();
    private final DsProcessor dsProcessor;

    public DynamicDatasourceNamedInterceptor(DsProcessor dsProcessor) {
        this.dsProcessor = dsProcessor;
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        String dsKey = determineDatasourceKey(invocation);
        DynamicDataSourceContextHolder.push(dsKey);
        try {
            return invocation.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    /**
     * 添加方法使用的数据源
     *
     * @param methodName 方法名称 例如 select*
     * @param dsKey 数据源名称     例如 master or slave
     */
    public void addPattern(@Nonnull String methodName, @Nonnull String dsKey) {
        log.debug("dynamic-datasource adding ds method [" + methodName + "] with attribute [" + dsKey + "]");
        nameMap.put(methodName, dsKey);
    }

    /**
     * 添加 PatternMap
     *
     * @param map namedMap
     */
    public void addPatternMap(Map<String, String> map) {
        for (Map.Entry<String, String> item : map.entrySet()) {
            addPattern(item.getKey(), item.getValue());
        }
    }

    /**
     * config from properties
     * <pre>
     *         Properties attributes = new Properties();
     *         attributes.setProperty("select*", "slave");
     *         attributes.setProperty("add*", "master");
     *         attributes.setProperty("update*", "master");
     *         attributes.setProperty("delete*", "master");
     * </pre>
     *
     * @param properties ds properties
     */
    public void fromProperties(@Nonnull Properties properties) {
        Enumeration<?> propNames = properties.propertyNames();
        while (propNames.hasMoreElements()) {
            String methodName = (String) propNames.nextElement();
            String value = properties.getProperty(methodName);
            this.addPattern(methodName, value);
        }
    }


    private boolean isMatch(String methodName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }

    private String determineDatasourceKey(MethodInvocation invocation) {
        String key = findDsKey(invocation);
        return (key != null && key.startsWith(DYNAMIC_PREFIX)) ? dsProcessor.determineDatasource(invocation, key) : key;
    }

    private String findDsKey(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        if (!ClassUtils.isUserLevelMethod(method)) {
            return null;
        }

        // 寻找直接名称匹配。
        String methodName = method.getName();
        String dsKey = this.nameMap.get(methodName);

        if (dsKey == null) {
            //寻找最具体的名称匹配。
            String bestNameMatch = null;
            for (String mappedName : this.nameMap.keySet()) {
                boolean match1 = isMatch(methodName, mappedName);
                boolean match2 = bestNameMatch == null || bestNameMatch.length() <= mappedName.length();
                if (match1 && match2) {
                    dsKey = this.nameMap.get(mappedName);
                    bestNameMatch = mappedName;
                }
            }
        }
        return dsKey;
    }
}
