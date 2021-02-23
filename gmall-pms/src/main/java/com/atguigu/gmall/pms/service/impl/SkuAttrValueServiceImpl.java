package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    AttrMapper attrMapper;


    @Autowired
    private SkuMapper skuMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySkuAttrValueByCidAndSkuId(long cid, long skuId) {


        //select * from pms_attr a where a.category_id = 225 and a.search_type = 1;
        List<AttrEntity> attrEntityList = attrMapper.selectList(new QueryWrapper<AttrEntity>()
                .eq("category_id", cid).eq("search_type", 1));

        if (CollectionUtils.isEmpty(attrEntityList)) {
            return null;
        }

        //将attrEntityList集合变成id集合
        List<Long> attrIdList = attrEntityList.stream().map(attrEntity -> attrEntity.getId()).collect(Collectors.toList());

        return skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId).in("attr_id", attrIdList));


    }

    @Override
    public List<SaleAttrValueVo> querySkuAttrValueBySpuId(Long spuId) {

        List<SkuEntity> skuEntities = skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));

        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }

        List<Long> skuList = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        List<SkuAttrValueEntity> attrValueEntities = skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuList));

        if (CollectionUtils.isEmpty(attrValueEntities)) {
            return null;
        }

        Map<Long, List<SkuAttrValueEntity>> map = attrValueEntities.stream().collect(Collectors.groupingBy(t -> t.getAttrId()));

        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();

        map.forEach((attrId,skuAttrValueEntities)->{
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            saleAttrValueVo.setAttrName(skuAttrValueEntities.get(0).getAttrName());
            saleAttrValueVo.setAttrValues(skuAttrValueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet()));

            saleAttrValueVos.add(saleAttrValueVo);
        });

        return saleAttrValueVos;
    }

    @Override
    public List<SkuAttrValueEntity> querySkuAttrValueBySkuId(Long skuId) {

        List<SkuAttrValueEntity>  skuAttrValueEntities =  skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id",skuId));

        return skuAttrValueEntities;
    }

    @Override
    public String saleAttrMappingSkuIdBySpuId(Long spuId) {

        //先通过spuid查询所有的sku

        List<SkuEntity> skuEntities = skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id",spuId));

        if (CollectionUtils.isEmpty(skuEntities)){
            return null;
        }
        //得到sku id的集合
        List<Long> skuIds = skuEntities.stream().map(skuEntity -> skuEntity.getId()).collect(Collectors.toList());

        List<Map<String, Object>> maps = skuAttrValueMapper.saleAttrMappingSkuId(skuIds);

        Map<String, Long> mappingMap = maps.stream().collect(Collectors.toMap(map -> map.get("attr_value").toString(), map -> (Long) map.get("sku_id")));

        return JSON.toJSONString(mappingMap);
    }

}
