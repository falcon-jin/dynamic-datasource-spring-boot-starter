package com.falcon.dynamic.datasource.exception;

/**
 * 数据源初始化失败异常
 *
 * @author falcon
 * @since 2.5.6
 */
public class ErrorCreateDataSourceException extends RuntimeException {

    public ErrorCreateDataSourceException(String message) {
        super(message);
    }

    public ErrorCreateDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
