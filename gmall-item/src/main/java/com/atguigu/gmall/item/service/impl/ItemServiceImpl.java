package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;


    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    TemplateEngine templateEngine;

    //商品详情
    public ItemVo queryItemBySkuId(Long skuId) {


        ItemVo itemVo = new ItemVo();

        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            //1通过skuid获取sku信息
            SkuEntity skuEntity = gmallPmsClient.querySkuById(skuId).getData();
            if (skuEntity == null) {
                return null;
            }

            BeanUtils.copyProperties(skuEntity, itemVo);

            return skuEntity;
        }, threadPoolExecutor);


        //2通过三级分类id查询一二三级分类


        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync((skuEntity) -> {

            //3通过sku_id获取品牌信息
            BrandEntity brandEntity = gmallPmsClient.queryBrandById(skuEntity.getBrandId()).getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);


        CompletableFuture<SpuEntity> spuFuture = skuFuture.thenApplyAsync(skuEntity -> {
            //4通过sku获取spu信息
            SpuEntity spuEntity = gmallPmsClient.querySpuById(skuEntity.getSpuId()).getData();
            if (spuEntity != null) {
                itemVo.setSpuName(spuEntity.getName());
            }
            return spuEntity;
        }, threadPoolExecutor);


        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(() -> {
            //5通过sku获取图片列表
            List<SkuImagesEntity> imagesEntityList = gmallPmsClient.queryImages(skuId).getData();
            if (!CollectionUtils.isEmpty(imagesEntityList)) {
                itemVo.setSkuImages(imagesEntityList.stream().map(SkuImagesEntity::getUrl).collect(Collectors.toList()));
            }
        }, threadPoolExecutor);


        //6查询营销信息


        CompletableFuture<Void> storeFuture = CompletableFuture.runAsync(() -> {
            //7通过sku判断是否有货
            List<WareSkuEntity> wareSkuEntities = gmallWmsClient.queryWareSkuBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> saleSpuFuture = spuFuture.thenAcceptAsync(spuEntity -> {
            //8查询所有的销售属性
            List<SaleAttrValueVo> saleAttrValueVos = gmallPmsClient.querySkuAttrValueBySpuId(spuEntity.getId()).getData();
            itemVo.setSaleAttrs(saleAttrValueVos);

        }, threadPoolExecutor);


        CompletableFuture<Void> saleSkuFuture = CompletableFuture.runAsync(() -> {
            //9查询当前sku的销售属性
            List<SkuAttrValueEntity> skuAttrValueEntities = gmallPmsClient.querySkuAttrValueBySkuId(skuId).getData();
            Map<Long, String> saleAttr = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
            itemVo.setSaleAttr(saleAttr);
        }, threadPoolExecutor);


        CompletableFuture<Void> skuJsonFuture = spuFuture.thenAcceptAsync((spuEntity) -> {
            //10skuId和销售属性映射
            String skuJson = gmallPmsClient.saleAttrMappingSkuIdBySpuId(spuEntity.getId()).getData();
            itemVo.setSkuJson(skuJson);
        }, threadPoolExecutor);

        CompletableFuture<Void> descFuture = spuFuture.thenAcceptAsync((spuEntity) -> {
            //11通过spuId获取spu的海报
            SpuDescEntity descEntity = gmallPmsClient.querySpuDescById(spuEntity.getId()).getData();
            if (descEntity != null && StringUtils.isNotBlank(descEntity.getDecript())) {
                itemVo.setSpuImages(Arrays.asList(descEntity.getDecript().split(",")));
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptAsync(skuEntity -> {
            //12通过categoryid 获取参数组
            List<ItemGroupVo> groupVos = gmallPmsClient.queryCategoryByCidSpuIdSkuId(skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId).getData();
            itemVo.setGroups(groupVos);
        }, threadPoolExecutor);


//        CompletableFuture<Void> groupFuture = skuFuture.thenAcceptBothAsync(spuFuture, (skuEntity, spuEntity) -> {
//            //12通过categoryid 获取参数组
//            List<ItemGroupVo> groupVos = gmallPmsClient.queryCategoryByCidSpuIdSkuId(skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId).getData();
//            itemVo.setGroups(groupVos);
//        });


        CompletableFuture.allOf(brandFuture, imgFuture, storeFuture, saleSpuFuture,
                saleSkuFuture, skuJsonFuture, descFuture, groupFuture).join();

        return itemVo;

    }



    //生成静态页面
    @Override
    public void generateHtml(Long skuId) {
        //通过sku查询最新的商品详情
        ItemVo itemVo = queryItemBySkuId(skuId);

        Context context = new Context();
        context.setVariable("itemVo", itemVo);
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(new File("F:\\staticHtml\\" + skuId + ".html"));
            templateEngine.process("item", context, printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }




}
