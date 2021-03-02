package com.atguigu.gmall.index;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallIndexApplicationTests {

    @Autowired
    private MyTestAsync myTestAsync;


    @Test
    void contextLoads() {

        myTestAsync.async1();
        myTestAsync.async2();

        System.out.println("主线程执行完毕.......");

    }

}
