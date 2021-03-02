package com.atguigu.gmall.cart.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final String EXCEPTION_KEY = "cart:exception:info";

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {

        log.error("异常为:{},出现异常的方法是:{},参数是:{}", throwable, method, objects);

        //这里不能使用threadLocal获取userId，因为出现错误的时子线程async.

        //这里我们规定 参数必须以userId作为第一个参数
        String userId = objects[0].toString();
        BoundSetOperations<String, String> setOps = redisTemplate.boundSetOps(EXCEPTION_KEY);
        setOps.add(userId);
    }
}
