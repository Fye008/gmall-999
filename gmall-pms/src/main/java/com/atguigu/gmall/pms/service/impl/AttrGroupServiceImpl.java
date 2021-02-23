package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrMapper attrMapper;

    @Autowired
    AttrGroupMapper attrGroupMapper;

    @Autowired
    SpuAttrValueMapper spuAttrValueMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;


    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrGroupEntity> queryGroupAndAttr(Long catId) {

        List<AttrGroupEntity> attrGroupEntityList = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", catId));

        //判断集合是否为空
        if (CollectionUtils.isEmpty(attrGroupEntityList)) {
            return null;
        }
        attrGroupEntityList.forEach((attrGroupEntity) -> {

            List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()));

            attrGroupEntity.setAttrEntities(attrEntities);

        });

        return attrGroupEntityList;
    }

    @Override
    public List<ItemGroupVo> queryCategoryByCidSpuIdSkuId(Long cid, Long spuId, Long skuId) {


        List<AttrGroupEntity> attrGroupEntityList = attrGroupMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));

        if (CollectionUtils.isEmpty(attrGroupEntityList)) {
            return null;
        }

        List<ItemGroupVo> itemGroupVos = attrGroupEntityList.stream().map(attrGroupEntity -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();

            List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()));

            if (!CollectionUtils.isEmpty(attrEntities)) {
                List<Long> ids = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());


                List<AttrValueVo> attrValueVos = new ArrayList<>();
                List<SpuAttrValueEntity> spuAttrValueEntityList = spuAttrValueMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().eq("spu_id", spuId).in("attr_id", ids));

                if (!CollectionUtils.isEmpty(spuAttrValueEntityList)) {
                    attrValueVos.addAll(spuAttrValueEntityList.stream().map(spuAttrValueEntity -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                        return attrValueVo;
                    }).collect(Collectors.toList()));
                }

                List<SkuAttrValueEntity> skuAttrValueEntityList = skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId).in("attr_id", ids));

                if (!CollectionUtils.isEmpty(skuAttrValueEntityList)) {
                    attrValueVos.addAll(skuAttrValueEntityList.stream().map(skuAttrValueEntity -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
                        return attrValueVo;
                    }).collect(Collectors.toList()));
                }

                itemGroupVo.setAttrValue(attrValueVos);
            }


            itemGroupVo.setName(attrGroupEntity.getName());
            itemGroupVo.setGroupId(attrGroupEntity.getId());
            return itemGroupVo;
        }).collect(Collectors.toList());


        return itemGroupVos;
    }

}
