package com.atguigu.gmall.seach;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.seach.feign.GmallPmsClient;
import com.atguigu.gmall.seach.feign.GmallWmsClient;
import com.atguigu.gmall.seach.pojo.Goods;
import com.atguigu.gmall.seach.pojo.SeachAttrValue;
import com.atguigu.gmall.seach.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSeachApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;


    @Autowired
    private GoodsRepository repository;

    @Test
    void contextLoads() {

        this.restTemplate.createIndex(Goods.class);

        this.restTemplate.putMapping(Goods.class);

    }

    @Test
    void contextLoads3() {
        int pageSize = 100;
        int pageNum = 1;

        PageParamVo pageParamVo = new PageParamVo(pageNum, pageSize, null);
        ResponseVo<List<SpuEntity>> listResponseVo = gmallPmsClient.querySpuByPageJson(pageParamVo);

        System.out.println("===============");
        System.out.println(listResponseVo);

    }


    @Test
    void contextLoads2() {
        int pageSize = 100;
        int pageNum = 1;



        do {
            PageParamVo pageParamVo = new PageParamVo(pageNum, pageSize, null);
            ResponseVo<List<SpuEntity>> responseVo = gmallPmsClient.querySpuByPageJson(pageParamVo);
            //通过分页查询，拿到当前页所有的spu
            List<SpuEntity> spuEntityList = responseVo.getData();


            //遍历spu得到所有sku
            if (CollectionUtils.isEmpty(spuEntityList)) {
                break;
            }


            for (SpuEntity spuEntity : spuEntityList) {
                //第一步，查询spu下所有的sku
                Long spuId = spuEntity.getId();
                List<SkuEntity> skuList = gmallPmsClient.querySkuBySpuId(spuId).getData();

                //将所有的sku转成goods
                if (!CollectionUtils.isEmpty(skuList)) {

                    List<Goods> goodsList = skuList.stream().map(skuEntity -> {
                        Goods goods = new Goods();
                        goods.setSkuId(skuEntity.getId());
                        goods.setPrice(skuEntity.getPrice().doubleValue());
                        goods.setDefaultImg(skuEntity.getDefaultImage());
                        goods.setTitle(skuEntity.getTitle());
                        goods.setSubTitle(skuEntity.getSubtitle());


                        //创建时间
                        goods.setCreateTime(spuEntity.getCreateTime());


                        //品牌
                        ResponseVo<BrandEntity> brandEntityResponseVo = gmallPmsClient.queryBrandById(spuEntity.getBrandId());
                        BrandEntity brandEntity = brandEntityResponseVo.getData();
                        if (brandEntity != null) {
                            goods.setBrandId(brandEntity.getId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }


                        //库存和销量
                        ResponseVo<List<WareSkuEntity>> listResponseVo = gmallWmsClient.queryWareSkuBySkuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntityList = listResponseVo.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntityList)) {
                            goods.setSaleCount(wareSkuEntityList.stream().map(wareSkuEntity -> wareSkuEntity.getSales()).reduce((a, b) -> a + b).get());
                            goods.setStore(wareSkuEntityList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                        }




                        //分类
                        ResponseVo<CategoryEntity> categoryEntityResponseVo = gmallPmsClient.queryCategoryById(skuEntity.getCategoryId());

                        CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                        if (categoryEntity != null) {
                            goods.setCategoryId(categoryEntity.getId());
                            goods.setCategoryName(categoryEntity.getName());
                        }

                       // 检索参数

                        List<SeachAttrValue> seachAttrValueList = new ArrayList<>();

                        ResponseVo<List<SkuAttrValueEntity>> saleAttrReponseVo = gmallPmsClient.querySkuAttrValueByCidAndSkuId(categoryEntity.getId(), skuEntity.getId());
                        List<SkuAttrValueEntity> saleAttrReponseVoData = saleAttrReponseVo.getData();
                        if (!CollectionUtils.isEmpty(saleAttrReponseVoData)) {

                            seachAttrValueList.addAll(saleAttrReponseVoData.stream().map(skuAttrValueEntity -> {
                                SeachAttrValue seachAttrValue = new SeachAttrValue();
                                BeanUtils.copyProperties(skuAttrValueEntity, seachAttrValue);
                                return seachAttrValue;
                            }).collect(Collectors.toList()));

                        }

                        List<SpuAttrValueEntity> spuAttrValueEntities = gmallPmsClient.querySpuAttrValueByCidAndSpuId(categoryEntity.getId(), spuId).getData();

                        if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {

                            seachAttrValueList.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                SeachAttrValue seachAttrValue = new SeachAttrValue();
                                BeanUtils.copyProperties(spuAttrValueEntity, seachAttrValue);
                                return seachAttrValue;
                            }).collect(Collectors.toList()));

                        }
                        goods.setSeachAttrValues(seachAttrValueList);

                        return goods;
                    }).collect(Collectors.toList());

                   repository.saveAll(goodsList);

                }

            }
            pageSize = spuEntityList.size();
            pageNum++;

        } while (pageSize == 100);


    }
}
