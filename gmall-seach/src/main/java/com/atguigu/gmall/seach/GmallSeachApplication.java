package com.atguigu.gmall.seach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GmallSeachApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallSeachApplication.class, args);
    }

}
