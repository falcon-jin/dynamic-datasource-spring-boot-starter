package com.falcon.dynamic.datasource.tx;

import org.springframework.util.StringUtils;

/**
 * 仿造seata打造的事物控制方案
 * @author funkye
 */
public class TransactionContext {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 获取绑定xid.
     *
     * @return the xid
     */
    public static String getXID() {
        String xid = CONTEXT_HOLDER.get();
        if (!StringUtils.isEmpty(xid)) {
            return xid;
        }
        return null;
    }

    /**
     * 移除绑定.
     *
     * @return the string
     */
    public static String unbind(String xid) {
        CONTEXT_HOLDER.remove();
        return xid;
    }

    /**
     * 绑定xid.
     *
     * @return the string
     */
    public static String bind(String xid) {
        CONTEXT_HOLDER.set(xid);
        return xid;
    }

    /**
     * 事物执行完毕移除xid
     */
    public static void remove() {
        CONTEXT_HOLDER.remove();
    }

}
