package com.falcon.dynamic.datasource.event;

import com.falcon.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.falcon.dynamic.datasource.toolkit.CryptoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多数据源默认解密事件
 *
 * @author falcon
 */
@Slf4j
public class EncDataSourceInitEvent implements DataSourceInitEvent {

    /**
     * 加密正则
     */
    private static final Pattern ENC_PATTERN = Pattern.compile("^ENC\\((.*)\\)$");

    @Override
    public void beforeCreate(DataSourceProperty dataSourceProperty) {
        String publicKey = dataSourceProperty.getPublicKey();
        if (StringUtils.hasText(publicKey)) {
            dataSourceProperty.setUrl(decrypt(publicKey, dataSourceProperty.getUrl()));
            dataSourceProperty.setUsername(decrypt(publicKey, dataSourceProperty.getUsername()));
            dataSourceProperty.setPassword(decrypt(publicKey, dataSourceProperty.getPassword()));
        }
    }

    @Override
    public void afterCreate(DataSource dataSource) {

    }

    /**
     * 字符串解密
     */
    private String decrypt(String publicKey, String cipherText) {
        if (StringUtils.hasText(cipherText)) {
            Matcher matcher = ENC_PATTERN.matcher(cipherText);
            if (matcher.find()) {
                try {
                    return CryptoUtils.decrypt(publicKey, matcher.group(1));
                } catch (Exception e) {
                    log.error("DynamicDataSourceProperties.decrypt error ", e);
                }
            }
        }
        return cipherText;
    }
}
