package com.atguigu.gmall.pms.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.service.SpuService;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo queryBrandPageByCategoryId(Long categoryId, PageParamVo pageParamVo) {
        //http://api.gmall.com/pms/spu/category/225?t=1611052243815&pageNum=1&pageSize=10&key=1
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //如果categoryId=0，是查全站，否则查本站
        if (categoryId != 0) {
            wrapper.eq("category_id", categoryId);
        }

        String key = pageParamVo.getKey();

        //查询条件不为空的话
        if (StringUtils.isNotBlank(key)) {
            wrapper.and((t)->t.like("name",key).or().eq("id", key));
        }
        IPage<SpuEntity> page = this.page(pageParamVo.getPage(), wrapper);

        return new PageResultVo(page);
    }

}
