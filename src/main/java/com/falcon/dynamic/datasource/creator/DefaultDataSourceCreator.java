package com.falcon.dynamic.datasource.creator;

import com.falcon.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.List;

/**
 * 数据源创建器
 *
 * @author falcon
 * @since 2.3.0
 */
@Slf4j
@Setter
public class DefaultDataSourceCreator {

    private List<DataSourceCreator> creators;

    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        DataSourceCreator dataSourceCreator = null;
        for (DataSourceCreator creator : this.creators) {
            if (creator.support(dataSourceProperty)) {
                dataSourceCreator = creator;
                break;
            }
        }
        if (dataSourceCreator == null) {
            throw new IllegalStateException("creator must not be null,please check the DataSourceCreator");
        }
        return dataSourceCreator.createDataSource(dataSourceProperty);
    }

}
