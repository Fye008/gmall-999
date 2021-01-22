package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.SkuSaleFeign;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;

import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    SpuDescMapper spuDescMapper;

    @Autowired
    SpuAttrValueService spuAttrValueService;

    @Autowired
    SkuAttrValueService skuAttrValueService;

    @Autowired
    SkuService skuService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleFeign skuSaleFeign;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo queryBrandPageByCategoryId(Long categoryId, PageParamVo pageParamVo) {
        //http://api.gmall.com/pms/spu/category/225?t=1611052243815&pageNum=1&pageSize=10&key=1
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //如果categoryId=0，是查全站，否则查本站
        if (categoryId != 0) {
            wrapper.eq("category_id", categoryId);
        }

        String key = pageParamVo.getKey();

        //查询条件不为空的话
        if (StringUtils.isNotBlank(key)) {
            wrapper.and((t) -> t.like("name", key).or().eq("id", key));
        }
        IPage<SpuEntity> page = this.page(pageParamVo.getPage(), wrapper);

        return new PageResultVo(page);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSpuVo(SpuVo spuVo) throws FileNotFoundException {

        //保存spu
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());
        this.save(spuVo);
        Long spuId = spuVo.getId();

        //如果不为空，保存spu图片
        List<String> spuImages = spuVo.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)) {
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setSpuId(spuId);
            spuDescEntity.setDecript(org.apache.commons.lang3.StringUtils.join(spuImages, ","));
            spuDescMapper.insert(spuDescEntity);
        }

        //保存spu基本属性
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {

            List<SpuAttrValueEntity> entityList = baseAttrs.stream().map((baseAttr) -> {

                SpuAttrValueVo spuAttrValueVo = new SpuAttrValueVo();
                BeanUtils.copyProperties(baseAttr, spuAttrValueVo);
                spuAttrValueVo.setSpuId(spuId);
                return spuAttrValueVo;
            }).collect(Collectors.toList());
            spuAttrValueService.saveBatch(entityList);
        }


        //保存sku
        List<SkuVo> skus = spuVo.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {
            skus.forEach(skuVo -> {
                skuVo.setSpuId(spuId);
                skuVo.setBrandId(spuVo.getBrandId());
                skuVo.setCategoryId(spuVo.getCategoryId());
                List<String> skuVoImages = skuVo.getImages();
                if (!CollectionUtils.isEmpty(skuVoImages)) {
                    //以后，如果用户没有传默认图片，就设置第一张为默认图片
                    skuVo.setDefaultImage(StringUtils.isNotBlank(skuVo.getDefaultImage()) ? skuVo.getDefaultImage() : skuVoImages.get(0));
                }
                skuService.save(skuVo);
                Long skuId = skuVo.getId();

                //保存sku图片
                if (!CollectionUtils.isEmpty(skuVoImages)) {
                    skuImagesService.saveBatch(skuVoImages.stream().map(image -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setSkuId(skuId);
                        skuImagesEntity.setUrl(image);
                        //如果图片是默认图片，DefaultStatus是1,否则是0
                        skuImagesEntity.setDefaultStatus(StringUtils.equals(skuVo.getDefaultImage(), image) ? 1 : 0);
                        return skuImagesEntity;
                    }).collect(Collectors.toList()));
                }

                //保存sku基本属性
                List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
                if (!CollectionUtils.isEmpty(saleAttrs)) {
                    saleAttrs.forEach(saleAttr -> saleAttr.setSkuId(skuId));
                    skuAttrValueService.saveBatch(saleAttrs);
                }

                SkuSaleVo skuSaleVo = new SkuSaleVo();
                BeanUtils.copyProperties(skuVo,skuSaleVo);
                skuSaleVo.setSkuId(skuId);
                skuSaleFeign.saveSkuSale(skuSaleVo);

                System.out.println(skuSaleVo);
                System.out.println("===========");

            });
        }


    }

}
