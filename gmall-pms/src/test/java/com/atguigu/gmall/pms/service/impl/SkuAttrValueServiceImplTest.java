package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@SpringBootTest
class SkuAttrValueServiceImplTest {

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    AttrMapper attrMapper;


    @Autowired
    private SkuMapper skuMapper;

    @Test
    void saleAttrMappingSkuIdBySpuId() {


        //先通过spuid查询所有的sku

        List<SkuEntity> skuEntities = skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id",7));

        //得到sku id的集合
        List<Long> skuIds = skuEntities.stream().map(skuEntity -> skuEntity.getId()).collect(Collectors.toList());

        List<Map<String, Object>> maps = skuAttrValueMapper.saleAttrMappingSkuId(skuIds);

        Map<String, Long> mappingMap = maps.stream().collect(Collectors.toMap(map -> map.get("attr_value").toString(), map -> (Long)map.get("sku_id")));
        System.out.println(JSON.toJSONString(mappingMap));
    }
}
