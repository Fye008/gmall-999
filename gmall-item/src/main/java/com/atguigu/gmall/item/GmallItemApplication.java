package com.atguigu.gmall.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

@SpringBootApplication
@EnableFeignClients
public class GmallItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallItemApplication.class, args);
    }

}
