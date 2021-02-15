package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("spuAttrValueService")
public class SpuAttrValueServiceImpl extends ServiceImpl<SpuAttrValueMapper, SpuAttrValueEntity> implements SpuAttrValueService {

    @Autowired
    AttrMapper attrMapper;

    @Autowired
    SpuAttrValueMapper spuAttrValueMapper;


    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SpuAttrValueEntity> querySpuAttrValueByCidAndSpuId(long cid, long spuId) {
        //select * from pms_attr a where a.category_id = 225 and a.search_type = 1;
        //SELECT * from pms_spu_attr_value b WHERE b.attr_id in(4,5,6,8,9) and b.spu_id=7;

        //根据分类id和search_type为1检索类型 查询 attrEntityList
        List<AttrEntity> attrEntityList = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("category_id", cid).eq("search_type", 1));
        //将attrEntityList集合变成id集合，这个集合id中既有销售属性也有基本属性

        if (CollectionUtils.isEmpty(attrEntityList)){
            return null;
        }

        List<Long> attrIdList = attrEntityList.stream().map(attrEntity -> attrEntity.getId()).collect(Collectors.toList());

        return spuAttrValueMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().eq("spu_id",spuId).in("attr_id", attrIdList));


    }

}
