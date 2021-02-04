package com.atguigu.gmall.seach.repository;

import com.atguigu.gmall.seach.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
