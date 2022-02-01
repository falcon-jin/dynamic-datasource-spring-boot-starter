package com.falcon.dynamic.datasource.aop;

import com.falcon.dynamic.datasource.tx.LocalTxUtil;
import com.falcon.dynamic.datasource.tx.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.StringUtils;

/**
 * 动态本地事务处理
 * @author funkye
 */
@Slf4j
public class DynamicLocalTransactionInterceptor implements MethodInterceptor {

    /**
     * 拦截加了DSTransactional注解的方法
     * @param methodInvocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        //已经记录事物id直接放行
        if (!StringUtils.isEmpty(TransactionContext.getXID())) {
            return methodInvocation.proceed();
        }
        //事物状态默认为true
        boolean state = true;
        Object result;
        //开启事务
        LocalTxUtil.startTransaction();
        try {
            //执行方法
            result = methodInvocation.proceed();
        } catch (Exception e) {
            //报错 事物状态为false 回滚事务
            state = false;
            throw e;
        } finally {
            if (state) {
                //提交事务
                LocalTxUtil.commit();
            } else {
                //将事物状态传递
                LocalTxUtil.rollback();
            }
        }
        return result;
    }
}
