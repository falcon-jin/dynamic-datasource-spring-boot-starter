package com.falcon.dynamic.datasource.aop;

import com.falcon.dynamic.datasource.processor.DsProcessor;
import com.falcon.dynamic.datasource.support.DataSourceClassResolver;
import com.falcon.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 多数据的核心拦截器
 *
 * @author falcon
 * @since 1.2.0
 */
public class DynamicDataSourceAnnotationInterceptor implements MethodInterceptor {

    /**
     * SPEL的识别前缀
     */
    private static final String DYNAMIC_PREFIX = "#";

    private final DataSourceClassResolver dataSourceClassResolver;
    private final DsProcessor dsProcessor;

    public DynamicDataSourceAnnotationInterceptor(Boolean allowedPublicOnly, DsProcessor dsProcessor) {
        dataSourceClassResolver = new DataSourceClassResolver(allowedPublicOnly);
        this.dsProcessor = dsProcessor;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //获取数据源名称
        String dsKey = determineDatasourceKey(invocation);
        //将当前线程使用的数据源按照执行顺序保存下来
        DynamicDataSourceContextHolder.push(dsKey);
        try {
            return invocation.proceed();
        } finally {
            //当前方法执行结束 弹出数据源
            DynamicDataSourceContextHolder.poll();
        }
    }

    /**
     * 确定使用哪个数据源
     * @param invocation 加了ds注解的方法
     * @return
     */
    private String determineDatasourceKey(MethodInvocation invocation) {
        String key = dataSourceClassResolver.findKey(invocation.getMethod(), invocation.getThis());
        return key.startsWith(DYNAMIC_PREFIX) ? dsProcessor.determineDatasource(invocation, key) : key;
    }
}
