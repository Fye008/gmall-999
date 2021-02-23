package com.atguigu.gmall.index;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GmallIndexApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallIndexApplication.class, args);
    }

}
