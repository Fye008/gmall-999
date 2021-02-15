package com.atguigu.gmall.seach.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParamVo {

    private String keyword;

    private List<Long> brandId;

    private List<Long> categoryId;

    private List<String> props;

    private Integer sort = 0;

    private Double priceMin;

    private Double priceMax;

    private Integer pageNum = 1;

    private final Integer pageSize = 20;

    private Boolean store;
}
