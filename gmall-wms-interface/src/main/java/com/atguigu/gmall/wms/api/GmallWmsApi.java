package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallWmsApi {


    @GetMapping("wms/waresku/sku/{skuId}")
    ResponseVo<List<WareSkuEntity>> queryWareSkuBySkuId(@PathVariable long skuId);


}

