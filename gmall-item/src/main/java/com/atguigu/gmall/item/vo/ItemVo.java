package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ItemVo {


    //三级分类
    private List<CategoryEntity> categories;

    //品牌信息
    private Long brandId;
    private String brandName;

    //spu信息
    private Long spuId;
    private String spuName;

    //sku属性
    private Long skuId;
    private BigDecimal price;
    private String title;
    private String subTitle;
    private Integer weight;
    private String defaultImage;


    //图片列表
    private List<String> skuImages;


    //营销信息
    private List<ItemSalesVo> salesVo;

    //是否有货
    private Boolean store = false;


    //sku的spu所有销售属性
    private List<SaleAttrValueVo> saleAttrs;

    //当前sku销售属性  {4:'暗夜黑',5:'8G',6:'128G'}
    private Map<Long, String> saleAttr;


    //销售属性和sku的关系
    private String skuJson;


    //商品海报信息
    private List<String> spuImages;

    //规格参数组
    private List<ItemGroupVo> groups;


}
