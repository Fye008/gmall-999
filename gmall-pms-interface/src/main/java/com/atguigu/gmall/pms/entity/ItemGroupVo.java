package com.atguigu.gmall.pms.entity;

import lombok.Data;

import java.util.List;

@Data
public class ItemGroupVo {

    private Long groupId;

    private String name;

    private List<AttrValueVo> attrValue;

}
