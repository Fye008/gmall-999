package com.atguigu.gmall.item.service.impl;



import com.atguigu.gmall.common.bean.ResponseVo;
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


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    //商品详情
    public ItemVo queryItemBySkuId(Long skuId) {


        ItemVo itemVo = new ItemVo();

        //1通过skuid获取sku信息
        SkuEntity skuEntity = gmallPmsClient.querySkuById(skuId).getData();
        if (skuEntity == null) {
            return null;
        }

        BeanUtils.copyProperties(skuEntity, itemVo);


        //2通过三级分类id查询一二三级分类


        //3通过sku_id获取品牌信息
        BrandEntity brandEntity = gmallPmsClient.queryBrandById(skuEntity.getBrandId()).getData();
        if (brandEntity != null) {
            itemVo.setBrandId(brandEntity.getId());
            itemVo.setBrandName(brandEntity.getName());
        }

        //4通过sku获取spu信息
        SpuEntity spuEntity = gmallPmsClient.querySpuById(skuEntity.getSpuId()).getData();
        if (spuEntity != null) {
            itemVo.setSpuName(spuEntity.getName());
        }


        //5通过sku获取图片列表
        List<SkuImagesEntity> imagesEntityList = gmallPmsClient.queryImages(skuId).getData();
        if (!CollectionUtils.isEmpty(imagesEntityList)) {
            itemVo.setSkuImages(imagesEntityList.stream().map(SkuImagesEntity::getUrl).collect(Collectors.toList()));
        }
        //6查询营销信息


        //7通过sku判断是否有货
        List<WareSkuEntity> wareSkuEntities = gmallWmsClient.queryWareSkuBySkuId(skuId).getData();
        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
            itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity ->  wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
        }

        //8查询所有的销售属性
        List<SaleAttrValueVo> saleAttrValueVos = gmallPmsClient.querySkuAttrValueBySpuId(spuEntity.getId()).getData();
        itemVo.setSaleAttrs(saleAttrValueVos);


        //9查询当前sku的销售属性
        List<SkuAttrValueEntity> skuAttrValueEntities = gmallPmsClient.querySkuAttrValueBySkuId(skuId).getData();
        Map<Long, String> saleAttr = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
        itemVo.setSaleAttr(saleAttr);


        //10skuId和销售属性映射
        String skuJson = gmallPmsClient.saleAttrMappingSkuIdBySpuId(spuEntity.getId()).getData();
        itemVo.setSkuJson(skuJson);

        //11通过spuId获取spu的海报
        SpuDescEntity descEntity = gmallPmsClient.querySpuDescById(spuEntity.getId()).getData();
        if (descEntity != null && StringUtils.isNotBlank(descEntity.getDecript())) {
            itemVo.setSpuImages(Arrays.asList(descEntity.getDecript().split(",")));
        }
        //12通过categoryid 获取参数组
        List<ItemGroupVo> groupVos = gmallPmsClient.queryCategoryByCidSpuIdSkuId(skuEntity.getCategoryId(), spuEntity.getId(), skuId).getData();

        itemVo.setGroups(groupVos);

        return itemVo;

    }


}
