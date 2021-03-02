package com.atguigu.gmall.cart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

@SpringBootTest
class GmallCartApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Test
    void contextLoads() {
        Map<Object, Object> map = redisTemplate.opsForHash().entries("123456");


        String v1 = (String) map.get("k1");
        String v2 = (String) map.get("k2");

        System.out.println(v1 + "====" + v2);

    }

}
