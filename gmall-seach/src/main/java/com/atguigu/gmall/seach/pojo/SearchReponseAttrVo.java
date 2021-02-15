package com.atguigu.gmall.seach.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SearchReponseAttrVo {

    private Integer attrId;

    private String attrName;

    private List<String> attrValues;
}
