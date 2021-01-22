package com.atguigu.gmall.pms.vo;


import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;

import org.apache.commons.lang3.StringUtils;

import java.util.List;


public class SpuAttrValueVo extends SpuAttrValueEntity {

    private List<String> valueSelected;

    public void setValueSelected(List<String> valueSelected) {
        this.setAttrValue(StringUtils.join(valueSelected,","));
        //this.setAttrValue(org.apache.commons.lang.StringUtils.join(valueSelected,","));
    }
}
