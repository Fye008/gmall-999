package com.atguigu.gmall.pms.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AttrGroupServiceImplTest {

    @Autowired
    AttrGroupServiceImpl attrGroupService;

    @Test
    void queryCategoryByCidSpuIdSkuId() {

        System.out.println(attrGroupService.queryCategoryByCidSpuIdSkuId(225l, 7l, 1l));

    }
}
