package com.falcon.dynamic.datasource.creator;

import com.falcon.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.falcon.dynamic.datasource.spring.boot.autoconfigure.dbcp2.Dbcp2Config;
import com.falcon.dynamic.datasource.toolkit.ConfigMergeCreator;
import com.falcon.dynamic.datasource.support.DdConstants;
import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * DBCP数据源创建器
 *
 * @author falcon
 * @since 2021/5/18
 */
public class Dbcp2DataSourceCreator extends AbstractDataSourceCreator implements DataSourceCreator, InitializingBean {

    private static final ConfigMergeCreator<Dbcp2Config, BasicDataSource> MERGE_CREATOR = new ConfigMergeCreator<>("Dbcp2", Dbcp2Config.class, BasicDataSource.class);

    private Dbcp2Config gConfig;

    @Override
    @SneakyThrows
    public DataSource doCreateDataSource(DataSourceProperty dataSourceProperty) {
        BasicDataSource dataSource = MERGE_CREATOR.create(gConfig, dataSourceProperty.getDbcp2());
        dataSource.setUsername(dataSourceProperty.getUsername());
        dataSource.setPassword(dataSourceProperty.getPassword());
        dataSource.setUrl(dataSourceProperty.getUrl());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (!StringUtils.isEmpty(driverClassName)) {
            dataSource.setDriverClassName(driverClassName);
        }
        if (Boolean.FALSE.equals(dataSourceProperty.getLazy())) {
            dataSource.start();
        }
        return dataSource;
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return type == null || DdConstants.DBCP2_DATASOURCE.equals(type.getName());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        gConfig = properties.getDbcp2();
    }
}
