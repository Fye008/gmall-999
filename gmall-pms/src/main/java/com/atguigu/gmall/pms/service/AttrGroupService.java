package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.ItemGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author xiaofangfang
 * @email xiaofangfang@atguigu.com
 * @date 2021-01-18 17:41:46
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> queryGroupAndAttr(Long catId);

    List<ItemGroupVo> queryCategoryByCidSpuIdSkuId(Long cid, Long spuId, Long skuId);
}

