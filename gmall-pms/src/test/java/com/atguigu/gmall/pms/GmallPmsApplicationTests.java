package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
class GmallPmsApplicationTests {


    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    AttrMapper attrMapper;


    @Autowired
    private SkuMapper skuMapper;

    @Test
    void contextLoads() {


        List<SkuEntity> skuEntities = skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", 7));

        List<Long> skuList = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        List<SkuAttrValueEntity> attrValueEntities = skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuList));


        Map<Long, List<SkuAttrValueEntity>> map = attrValueEntities.stream().collect(Collectors.groupingBy(t -> t.getAttrId()));

        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();

        map.forEach((attrId,skuAttrValueEntities)->{
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            saleAttrValueVo.setAttrName(skuAttrValueEntities.get(0).getAttrName());
            saleAttrValueVo.setAttrValues(skuAttrValueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet()));

            saleAttrValueVos.add(saleAttrValueVo);
        });

        System.out.println(saleAttrValueVos);

    }

}
