package com.atguigu.gmall.seach.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.seach.feign.GmallPmsClient;
import com.atguigu.gmall.seach.feign.GmallWmsClient;
import com.atguigu.gmall.seach.pojo.Goods;
import com.atguigu.gmall.seach.pojo.SeachAttrValue;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import com.sun.xml.internal.bind.v2.TODO;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RabbitMqListener {

    @Autowired
    private ElasticsearchRepository repository;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;


    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "SEARCH_INSERT_QUEUE", durable = "true"),
            exchange = @Exchange(name = "pms_spu_exchange", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"spu_insert"}))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {

        if (spuId == null){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

            return;
        }

        SpuEntity spuEntity = gmallPmsClient.querySpuById(spuId).getData();
        if (spuEntity == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

            return;
        }


        //第一步，查询spu下所有的sku
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

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        //后续可以完成 异常重试，拒绝登



    }

}
