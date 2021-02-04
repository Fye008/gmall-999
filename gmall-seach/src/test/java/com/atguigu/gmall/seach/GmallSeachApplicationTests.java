package com.atguigu.gmall.seach;

import com.atguigu.gmall.seach.pojo.Goods;
import com.atguigu.gmall.seach.repository.GoodsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@SpringBootTest
class GmallSeachApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;


    @Autowired
    private GoodsRepository repository;

    @Test
    void contextLoads() {

        this.restTemplate.createIndex(Goods.class);

        this.restTemplate.putMapping(Goods.class);

    }



    @Test
    void contextLoads2() {

        //repository.saveAll();

    }
}
