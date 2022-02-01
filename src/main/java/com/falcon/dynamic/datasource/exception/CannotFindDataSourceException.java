package com.falcon.dynamic.datasource.exception;

/**
 * 数据源无法选择时的异常
 *
 * @author falcon
 * @since 2.5.6
 */
public class CannotFindDataSourceException extends RuntimeException {

    public CannotFindDataSourceException(String message) {
        super(message);
    }

    public CannotFindDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
