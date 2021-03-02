package com.atguigu.gmall.order.pojo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import lombok.Data;


import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {

    private Long skuId;

    private String title;

    private String defaultImage;

    private BigDecimal price;

    private Integer count;

    private Boolean store;

    private List<SkuAttrValueEntity> saleAttrs;

    private String sales;

    private BigDecimal weight;
}
