package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuEntity;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * spu信息
 *
 * @author xiaofangfang
 * @email xiaofangfang@atguigu.com
 * @date 2021-01-18 17:41:46
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo queryBrandPageByCategoryId(Long categoryId, PageParamVo pageParamVo);

    void saveSpuVo(SpuVo spuVo) throws FileNotFoundException;
}

