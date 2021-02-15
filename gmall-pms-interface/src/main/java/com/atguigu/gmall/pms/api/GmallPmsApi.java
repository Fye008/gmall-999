package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    @PostMapping("pms/spuattrvalue/search/{cid}")
    ResponseVo<List<SpuAttrValueEntity>> querySpuAttrValueByCidAndSpuId(@PathVariable long cid, @RequestParam long spuId);

    @PutMapping("/pms/spu/json")
    ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    @GetMapping("/pms/sku/spu/{spuId}")
    ResponseVo<List<SkuEntity>> querySkuBySpuId(@PathVariable long spuId);


    @PostMapping("pms/skuattrvalue/search/{cid}")
    ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueByCidAndSkuId(@PathVariable long cid, @RequestParam long skuId);


    @GetMapping("pms/brand/{id}")
    ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);


    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") long id);

}
