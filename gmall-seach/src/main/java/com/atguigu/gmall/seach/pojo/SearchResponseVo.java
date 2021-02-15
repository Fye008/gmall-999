package com.atguigu.gmall.seach.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {

    //品牌  品牌名，logo
    private List<BrandEntity> brandEntityList;

    //分类
    private List<CategoryEntity> categoryList;

    //规格参数过滤条件
    private List<SearchResponseAttrVo> filters;

    private Integer pageNum;

    private Integer pageSize;

    private Long total;

    private List<Goods> goodsList;

}
