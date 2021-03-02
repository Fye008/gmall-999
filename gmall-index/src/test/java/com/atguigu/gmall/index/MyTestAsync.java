package com.atguigu.gmall.index;


import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Async
public class MyTestAsync {

    public void async1(){

        try {
            System.out.println("async1开始执行...");
            TimeUnit.SECONDS.sleep(2);
            System.out.println("async1执行完毕...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void async2(){

        try {
            System.out.println("async2开始执行！！！");
            TimeUnit.SECONDS.sleep(3);
            System.out.println("async2执行完毕！！！");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
