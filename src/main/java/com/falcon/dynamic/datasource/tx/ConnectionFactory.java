package com.falcon.dynamic.datasource.tx;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接工厂
 * 记录一个线程执行方法所有的数据库连接
 * @author funkye
 */
public class ConnectionFactory {

    /**
     * 记录一个线程执行方法所有的数据库连接
     */
    private static final ThreadLocal<Map<String, ConnectionProxy>> CONNECTION_HOLDER =
            new ThreadLocal<Map<String, ConnectionProxy>>() {
                @Override
                protected Map<String, ConnectionProxy> initialValue() {
                    return new ConcurrentHashMap<>(8);
                }
            };

    public static void putConnection(String ds, ConnectionProxy connection) {
        Map<String, ConnectionProxy> concurrentHashMap = CONNECTION_HOLDER.get();
        if (!concurrentHashMap.containsKey(ds)) {
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            concurrentHashMap.put(ds, connection);
        }
    }

    public static ConnectionProxy getConnection(String ds) {
        return CONNECTION_HOLDER.get().get(ds);
    }
    /**
     * 通知提交事物还是回滚事物
     * @param state 事物执行状态
     */
    public static void notify(Boolean state) {
        try {
            Map<String, ConnectionProxy> concurrentHashMap = CONNECTION_HOLDER.get();
            for (ConnectionProxy connectionProxy : concurrentHashMap.values()) {
                connectionProxy.notify(state);
            }
        } finally {
            CONNECTION_HOLDER.remove();
        }
    }

}
