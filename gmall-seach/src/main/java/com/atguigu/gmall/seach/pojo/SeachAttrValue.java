package com.atguigu.gmall.seach.pojo;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
public class SeachAttrValue {


    @Field(type = FieldType.Long)
    private Long attrId;

    @Field(type = FieldType.Keyword)
    private String attrName;


    @Field(type = FieldType.Keyword)
    private String attrValue;


}
