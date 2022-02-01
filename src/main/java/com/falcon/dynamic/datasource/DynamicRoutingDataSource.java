package com.falcon.dynamic.datasource;

import com.falcon.dynamic.datasource.ds.AbstractRoutingDataSource;
import com.falcon.dynamic.datasource.ds.GroupDataSource;
import com.falcon.dynamic.datasource.ds.ItemDataSource;
import com.falcon.dynamic.datasource.exception.CannotFindDataSourceException;
import com.falcon.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.falcon.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import com.falcon.dynamic.datasource.strategy.LoadBalanceDynamicDataSourceStrategy;
import com.falcon.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.p6spy.engine.spy.P6DataSource;
import io.seata.rm.datasource.DataSourceProxy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 核心动态数据源组件
 *
 * @author falcon 
 * @since 1.0.0
 */
@Slf4j
public class DynamicRoutingDataSource extends AbstractRoutingDataSource implements InitializingBean, DisposableBean {

    private static final String UNDERLINE = "_";
    /**
     * 所有数据库
     */
    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    /**
     * 分组数据库
     */
    private final Map<String, GroupDataSource> groupDataSources = new ConcurrentHashMap<>();
    @Autowired
    private List<DynamicDataSourceProvider> providers;
    @Setter
    private Class<? extends DynamicDataSourceStrategy> strategy = LoadBalanceDynamicDataSourceStrategy.class;
    @Setter
    private String primary = "master";
    @Setter
    private Boolean strict = false;
    @Setter
    private Boolean p6spy = false;
    @Setter
    private Boolean seata = false;

    /**
     * 确定使用数据源
     * @return
     */
    @Override
    public DataSource determineDataSource() {
        String dsKey = DynamicDataSourceContextHolder.peek();
        return getDataSource(dsKey);
    }
    /**
     * 确定主数据源
     * @return
     */
    private DataSource determinePrimaryDataSource() {
        log.debug("dynamic-datasource switch to the primary datasource");
        DataSource dataSource = dataSourceMap.get(primary);
        if (dataSource != null) {
            return dataSource;
        }
        GroupDataSource groupDataSource = groupDataSources.get(primary);
        if (groupDataSource != null) {
            return groupDataSource.determineDataSource();
        }
        throw new CannotFindDataSourceException("dynamic-datasource can not find primary datasource");
    }

    /**
     * 获取所有的数据源
     *
     * @return 当前所有数据源
     */
    public Map<String, DataSource> getDataSources() {
        return dataSourceMap;
    }

    /**
     * 获取的所有的分组数据源
     *
     * @return 当前所有的分组数据源
     */
    public Map<String, GroupDataSource> getGroupDataSources() {
        return groupDataSources;
    }

    /**
     * 获取数据源
     *
     * @param ds 数据源名称
     * @return 数据源
     */
    public DataSource getDataSource(String ds) {
        //没有传入数据源名称 就是没有加ds注解 默认使用主数据源
        if (StringUtils.isEmpty(ds)) {
            return determinePrimaryDataSource();
        } else if (!groupDataSources.isEmpty() && groupDataSources.containsKey(ds)) {
            log.debug("dynamic-datasource switch to the datasource named [{}]", ds);
            //动态数据源切换到名为的数据源
            return groupDataSources.get(ds).determineDataSource();
        } else if (dataSourceMap.containsKey(ds)) {
            log.debug("dynamic-datasource switch to the datasource named [{}]", ds);
            return dataSourceMap.get(ds);
        }
        //如果启用严格模式 没有找到数据源会抛出异常 没有启用的化默认使用主数据源
        if (strict) {
            throw new CannotFindDataSourceException("dynamic-datasource could not find a datasource named" + ds);
        }
        return determinePrimaryDataSource();
    }

    /**
     * 添加数据源
     *
     * @param ds         数据源名称
     * @param dataSource 数据源
     */
    public synchronized void addDataSource(String ds, DataSource dataSource) {
        DataSource oldDataSource = dataSourceMap.put(ds, dataSource);
        // 新数据源添加到分组
        this.addGroupDataSource(ds, dataSource);
        // 关闭老的数据源
        if (oldDataSource != null) {
            closeDataSource(ds, oldDataSource);
        }
        log.info("dynamic-datasource - add a datasource named [{}] success", ds);
    }

    /**
     * 新数据源添加到分组
     *
     * @param ds         新数据源的名字
     * @param dataSource 新数据源
     */
    private void addGroupDataSource(String ds, DataSource dataSource) {
        if (ds.contains(UNDERLINE)) {
            String group = ds.split(UNDERLINE)[0];
            GroupDataSource groupDataSource = groupDataSources.get(group);
            if (groupDataSource == null) {
                try {
                    groupDataSource = new GroupDataSource(group, strategy.getDeclaredConstructor().newInstance());
                    groupDataSources.put(group, groupDataSource);
                } catch (Exception e) {
                    throw new RuntimeException("dynamic-datasource - add the datasource named " + ds + " error", e);
                }
            }
            groupDataSource.addDatasource(ds, dataSource);
        }
    }

    /**
     * 删除数据源
     *
     * @param ds 数据源名称
     */
    public synchronized void removeDataSource(String ds) {
        if (!StringUtils.hasText(ds)) {
            throw new RuntimeException("remove parameter could not be empty");
        }
        if (primary.equals(ds)) {
            throw new RuntimeException("could not remove primary datasource");
        }
        if (dataSourceMap.containsKey(ds)) {
            DataSource dataSource = dataSourceMap.remove(ds);
            closeDataSource(ds, dataSource);
            if (ds.contains(UNDERLINE)) {
                String group = ds.split(UNDERLINE)[0];
                if (groupDataSources.containsKey(group)) {
                    DataSource oldDataSource = groupDataSources.get(group).removeDatasource(ds);
                    if (oldDataSource == null) {
                        log.warn("fail for remove datasource from group. dataSource: {} ,group: {}", ds, group);
                    }
                }
            }
            log.info("dynamic-datasource - remove the database named [{}] success", ds);
        } else {
            log.warn("dynamic-datasource - could not find a database named [{}]", ds);
        }
    }

    @Override
    public void destroy() throws Exception {
        log.info("dynamic-datasource start closing ....");
        for (Map.Entry<String, DataSource> item : dataSourceMap.entrySet()) {
            closeDataSource(item.getKey(), item.getValue());
        }
        log.info("dynamic-datasource all closed success,bye");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 检查开启了配置但没有相关依赖
        checkEnv();
        // 添加并分组数据源
        Map<String, DataSource> dataSources = new HashMap<>(16);
        for (DynamicDataSourceProvider provider : providers) {
            dataSources.putAll(provider.loadDataSources());
        }
        for (Map.Entry<String, DataSource> dsItem : dataSources.entrySet()) {
            addDataSource(dsItem.getKey(), dsItem.getValue());
        }
        // 检测默认数据源是否设置
        if (groupDataSources.containsKey(primary)) {
            log.info("dynamic-datasource initial loaded [{}] datasource,primary group datasource named [{}]", dataSources.size(), primary);
        } else if (dataSourceMap.containsKey(primary)) {
            log.info("dynamic-datasource initial loaded [{}] datasource,primary datasource named [{}]", dataSources.size(), primary);
        } else {
            log.warn("dynamic-datasource initial loaded [{}] datasource,Please add your primary datasource or check your configuration", dataSources.size());
        }
    }

    private void checkEnv() {
        if (p6spy) {
            try {
                Class.forName("com.p6spy.engine.spy.P6DataSource");
                log.info("dynamic-datasource detect P6SPY plugin and enabled it");
            } catch (Exception e) {
                throw new RuntimeException("dynamic-datasource enabled P6SPY ,however without p6spy dependency", e);
            }
        }
        if (seata) {
            try {
                Class.forName("io.seata.rm.datasource.DataSourceProxy");
                log.info("dynamic-datasource detect ALIBABA SEATA and enabled it");
            } catch (Exception e) {
                throw new RuntimeException("dynamic-datasource enabled ALIBABA SEATA,however without seata dependency", e);
            }
        }
    }

    /**
     * 关闭数据源
     *
     * @param ds         dsName
     * @param dataSource db
     */
    private void closeDataSource(String ds, DataSource dataSource) {
        try {
            if (dataSource instanceof ItemDataSource) {
                ((ItemDataSource) dataSource).close();
            } else {
                if (seata) {
                    if (dataSource instanceof DataSourceProxy) {
                        DataSourceProxy dataSourceProxy = (DataSourceProxy) dataSource;
                        dataSource = dataSourceProxy.getTargetDataSource();
                    }
                }
                if (p6spy) {
                    if (dataSource instanceof P6DataSource) {
                        Field realDataSourceField = P6DataSource.class.getDeclaredField("realDataSource");
                        realDataSourceField.setAccessible(true);
                        dataSource = (DataSource) realDataSourceField.get(dataSource);
                    }
                }
                Method closeMethod = ReflectionUtils.findMethod(dataSource.getClass(), "close");
                if (closeMethod != null) {
                    closeMethod.invoke(dataSource);
                }
            }
        } catch (Exception e) {
            log.warn("dynamic-datasource closed datasource named [{}] failed", ds, e);
        }
    }

}