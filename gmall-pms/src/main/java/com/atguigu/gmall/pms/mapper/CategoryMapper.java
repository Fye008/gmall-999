package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author xiaofangfang
 * @email xiaofangfang@atguigu.com
 * @date 2021-01-18 17:41:46
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {
	
}
