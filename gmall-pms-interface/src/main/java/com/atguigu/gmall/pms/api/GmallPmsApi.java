package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    @GetMapping("pms/skuimages/sku/{skuId}")
    public ResponseVo<List<SkuImagesEntity>> queryImages(@PathVariable Long skuId);



    @GetMapping("pms/attrgroup/query/spu/sku/{cid}")
    public ResponseVo<List<ItemGroupVo>> queryCategoryByCidSpuIdSkuId(@PathVariable  Long cid, @RequestParam("spuId") Long spuId, @RequestParam("skuId") Long skuId);


    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySkuAttrValueBySpuId(@PathVariable Long spuId);

    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueBySkuId(@PathVariable Long skuId);


    @GetMapping("pms/skuattrvalue/spu/mapping/{spuId}")
    public ResponseVo<String> saleAttrMappingSkuIdBySpuId(@PathVariable Long spuId);


    @GetMapping("pms/spudesc/{spuId}")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);


    @GetMapping("pms/sku/{id}")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);


    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);


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
