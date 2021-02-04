package com.atguigu.gmall.seach.pojo;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

@Data
@Document(indexName = "goods", type = "info", shards = 3, replicas = 2)
public class Goods {


    //商品的基本属性
    @Id
    private Long skuId;

    @Field(type = FieldType.Keyword,index = false)
    private String defaultImg;

    @Field(type = FieldType.Double)
    private double price;

    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Keyword,index = false)
    private String subTitle;


    //排序所需字段
    @Field(type = FieldType.Integer)
    private Integer saleCount; //销量

    @Field(type = FieldType.Date)
    private Date createTime;

    //过滤的字段
    @Field(type = FieldType.Boolean)
    private Boolean store;//库存


    //品牌聚合所需要的字段
    @Field(type = FieldType.Long)
    private Long brandId;

    @Field(type = FieldType.Keyword)
    private String brandName;

    @Field(type = FieldType.Keyword)
    private String logo;


    //分类聚合所需要的字段

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private String categoryName;


    //规格参数聚合所需要的字段
    @Field(type = FieldType.Nested)
    private List<SeachAttrValue> seachAttrValues;


}
