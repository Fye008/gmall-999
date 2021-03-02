package com.atguigu.gmall.scheduled.jobHandler;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.entity.CartEntity;
import com.atguigu.gmall.scheduled.feign.GmallCartClient;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class CartJobHandler {

    @Autowired
    private GmallCartClient gmallCartClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String EXCEPTION_KEY = "cart:exception:info";

    private static final String KEY_PREFIX = "cart:info:";

    @XxlJob("cartJobHandler")
    public ReturnT<String> executor(String param) {

        System.out.println("hhhhaahaah");

        if (!redisTemplate.hasKey(EXCEPTION_KEY)){
            return ReturnT.SUCCESS;
        }
        BoundSetOperations<String, String> setOps = redisTemplate.boundSetOps(EXCEPTION_KEY);

        String userId = setOps.pop();

        System.out.println(StringUtils.isNotBlank(userId)+ "1-------");

        while (StringUtils.isNotBlank(userId)){
            System.out.println("-------");
            //1.先删除数据库中该用户的数据
            gmallCartClient.deleteUserAllCart(userId);
            //2判断redis中该用户数据是否为空，为空直接返回
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
            if (hashOps.size() == 0){
                return ReturnT.SUCCESS;
            }

            //3.在mysql中同步数据
            hashOps.values().forEach(cartJson ->{
                CartEntity cartEntity = JSON.parseObject(cartJson.toString(), CartEntity.class);

                System.out.println(cartEntity + "===========");
                gmallCartClient.insertUserCart(cartEntity);

                System.out.println("----------------");
            });
            userId = setOps.pop();
        }
        return ReturnT.SUCCESS;
    }

}
