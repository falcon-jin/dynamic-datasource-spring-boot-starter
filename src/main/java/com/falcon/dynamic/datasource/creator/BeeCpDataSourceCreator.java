package com.falcon.dynamic.datasource.creator;

import cn.beecp.BeeDataSource;
import cn.beecp.BeeDataSourceConfig;
import com.falcon.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.falcon.dynamic.datasource.spring.boot.autoconfigure.beecp.BeeCpConfig;
import com.falcon.dynamic.datasource.toolkit.ConfigMergeCreator;
import com.falcon.dynamic.datasource.support.DdConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * BeeCp数据源创建器
 *
 * @author falcon
 * @since 2020/5/14
 */
@Slf4j
public class BeeCpDataSourceCreator extends AbstractDataSourceCreator implements DataSourceCreator, InitializingBean {

    private static final ConfigMergeCreator<BeeCpConfig, BeeDataSourceConfig> MERGE_CREATOR = new ConfigMergeCreator<>("BeeCp", BeeCpConfig.class, BeeDataSourceConfig.class);

    private static Method copyToMethod = null;

    static {
        try {
            copyToMethod = BeeDataSourceConfig.class.getDeclaredMethod("copyTo", BeeDataSourceConfig.class);
            copyToMethod.setAccessible(true);
        } catch (NoSuchMethodException ignored) {
        }
    }

    private BeeCpConfig gConfig;

    @Override
    public DataSource doCreateDataSource(DataSourceProperty dataSourceProperty) {
        BeeDataSourceConfig config = MERGE_CREATOR.create(gConfig, dataSourceProperty.getBeecp());
        config.setUsername(dataSourceProperty.getUsername());
        config.setPassword(dataSourceProperty.getPassword());
        config.setJdbcUrl(dataSourceProperty.getUrl());
        config.setPoolName(dataSourceProperty.getPoolName());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (!StringUtils.isEmpty(driverClassName)) {
            config.setDriverClassName(driverClassName);
        }
        if (Boolean.FALSE.equals(dataSourceProperty.getLazy())) {
            return new BeeDataSource(config);
        }
        BeeDataSource beeDataSource = new BeeDataSource();
        try {
            copyToMethod.invoke(config, beeDataSource);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return beeDataSource;
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return type == null || DdConstants.BEECP_DATASOURCE.equals(type.getName());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        gConfig = properties.getBeecp();
    }
}
