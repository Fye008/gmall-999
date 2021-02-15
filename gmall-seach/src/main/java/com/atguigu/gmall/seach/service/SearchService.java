package com.atguigu.gmall.seach.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.seach.pojo.Goods;
import com.atguigu.gmall.seach.pojo.SearchParamVo;
import com.atguigu.gmall.seach.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.seach.pojo.SearchResponseVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public SearchResponseVo search(SearchParamVo searchParamVo) {


        SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDSL(searchParamVo));

        try {
            //查询
            SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //解析结果
            SearchResponseVo searchResponseVo = parseResponse(response);
            return searchResponseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private SearchResponseVo parseResponse(SearchResponse response) {

        SearchResponseVo searchResponseVo = new SearchResponseVo();

        SearchHits hits = response.getHits();
        //设置总记录数
        searchResponseVo.setTotal(hits.getTotalHits());

        SearchHit[] hitsHits = hits.getHits();

        List<Goods> goodsList = Arrays.stream(hitsHits).map(hitsHit -> {
            //这是一个json字符串，将他转成对象
            String sourceAsString = hitsHit.getSourceAsString();
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            //还需要获取高亮结果集，覆盖_source中的普通title
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField hightTitle = highlightFields.get("title");

            Text[] fragments = hightTitle.fragments();
            goods.setTitle(fragments[0].string());
            return goods;
        }).collect(Collectors.toList());

        //设置商品集合
        searchResponseVo.setGoodsList(goodsList);


        //获取所有聚合
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();

        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> brandIdAggBuckets = brandIdAgg.getBuckets();

        //获取品牌聚合,设置品牌 id name logo
        searchResponseVo.setBrandEntityList(brandIdAggBuckets.stream().map(bucket -> {

            BrandEntity brandEntity = new BrandEntity();

            brandEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());

            //获取品牌id聚合
            Map<String, Aggregation> stringAggregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();
            //获取子聚合的 品牌名称聚合
            ParsedStringTerms brandNameAgg = (ParsedStringTerms) stringAggregationMap.get("brandName");
            List<? extends Terms.Bucket> brandNameAggBuckets = brandNameAgg.getBuckets();
            if (!CollectionUtils.isEmpty(brandNameAggBuckets)) {
                brandEntity.setName(brandNameAggBuckets.get(0).getKeyAsString());
            }
            //获取子聚合的 品牌logo聚合
            ParsedStringTerms brandLogoAgg = (ParsedStringTerms) stringAggregationMap.get("brandLogo");
            List<? extends Terms.Bucket> logoAggBuckets = brandLogoAgg.getBuckets();
            if (!CollectionUtils.isEmpty(logoAggBuckets)) {
                brandEntity.setLogo(logoAggBuckets.get(0).getKeyAsString());
            }
            return brandEntity;
        }).collect(Collectors.toList()));


        //获取分类聚合
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregationMap.get("categoryIdAgg");

        List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();

        //设置分类，包括分类id，分类名称
        searchResponseVo.setCategoryList(categoryIdAggBuckets.stream().map(bucket -> {
            CategoryEntity categoryEntity = new CategoryEntity();
            //设置分类id
            categoryEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
            ParsedStringTerms categoryNameAgg = ((Terms.Bucket) bucket).getAggregations().get("categoryName");
            //设置分类名称

            List<? extends Terms.Bucket> buckets = categoryNameAgg.getBuckets();
            if (!CollectionUtils.isEmpty(buckets)) {
                categoryEntity.setName(buckets.get(0).getKeyAsString());
            }
            return categoryEntity;
        }).collect(Collectors.toList()));

        //获取规格参数聚合
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        ParsedStringTerms attrIdAgg = (ParsedStringTerms) attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)) {

            searchResponseVo.setFilters(buckets.stream().map(bucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();

                searchResponseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                Map<String, Aggregation> stringAggregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();
                //设置属性名称
                ParsedStringTerms attrName = (ParsedStringTerms) stringAggregationMap.get("attrName");
                searchResponseAttrVo.setAttrName(attrName.getBuckets().get(0).getKeyAsString());

                //设置属性值集合
                ParsedStringTerms attrValueAgg = (ParsedStringTerms) stringAggregationMap.get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueBucket = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrValueBucket)) {
                    searchResponseAttrVo.setAttrValues(attrValueBucket.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList()));
                }
                return searchResponseAttrVo;
            }).collect(Collectors.toList()));
        }


        return searchResponseVo;
    }


    private SearchSourceBuilder buildDSL(SearchParamVo searchParamVo) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        String keyword = searchParamVo.getKeyword();

        if (StringUtils.isBlank(keyword)) {
            return searchSourceBuilder;
        }


        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        searchSourceBuilder.query(boolQueryBuilder);

        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));


        //品牌过滤
        List<Long> brandIdList = searchParamVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandIdList)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandId", brandIdList));
        }

        //分类过滤
        List<Long> categoryIdList = searchParamVo.getCategoryId();
        if (!CollectionUtils.isEmpty(categoryIdList)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId", categoryIdList));
        }

        Double priceMax = searchParamVo.getPriceMax();
        Double priceMin = searchParamVo.getPriceMin();

        //价格区间过滤
        if (priceMax != null || priceMin != null) {

            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (priceMin != null) {
                rangeQuery.gte(priceMin);
            }
            if (priceMax != null) {
                rangeQuery.lte(priceMax);
            }
            boolQueryBuilder.filter(rangeQuery);
        }


        //是否有货过滤
        Boolean store = searchParamVo.getStore();
        if (store != null && store) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }


        //规格参数过滤
        List<String> props = searchParamVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach((prop) -> {
                //每一个prop 4:8G-12G
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length == 2) {

                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    //用冒号分隔attrId和attrValues
                    boolQuery.must(QueryBuilders.termQuery("seachAttrValues.attrName", attr[0]));

                    String[] attrValues = StringUtils.split(attr[1], "-");

                    boolQuery.must(QueryBuilders.termsQuery("seachAttrValues.attrValue", attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("seachAttrValues", boolQuery, ScoreMode.None));
                }

            });
        }

        //排序
        Integer sort = searchParamVo.getSort();
        switch (sort) {
            case 1:
                searchSourceBuilder.sort("price", SortOrder.DESC);
                break;
            case 2:
                searchSourceBuilder.sort("price", SortOrder.ASC);
                break;
            case 3:
                searchSourceBuilder.sort("sales", SortOrder.DESC);
                break;
            case 4:
                searchSourceBuilder.sort("createTime", SortOrder.DESC);
                break;
            default:
                //默认是得分
                searchSourceBuilder.sort("_score", SortOrder.DESC);
                break;
        }

        //构建分页
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        searchSourceBuilder.size(pageSize);

        //构建高亮
        searchSourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<font style='color:red;'>").postTags("</font style='color:red;'>"));


        //构建品牌聚合
        searchSourceBuilder.aggregation(
                AggregationBuilders.terms("brandIdAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandName").field("brandName"))
                        .subAggregation(AggregationBuilders.terms("brandLogo").field("logo"))
        );

        //构建分类聚合
        searchSourceBuilder.aggregation(
                AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                        .subAggregation(AggregationBuilders.terms("categoryName").field("categoryName"))
        );

        //构建属性聚合
        searchSourceBuilder.aggregation(
                AggregationBuilders.nested("attrAgg", "seachAttrValues")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("seachAttrValues.attrId")
                                .subAggregation(AggregationBuilders.terms("attrName").field("seachAttrValues.attrName"))
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("seachAttrValues.attrValue")))

        );


        //结果集过滤
        searchSourceBuilder.fetchSource(new String[]{"skuId", "defaultImg", "title", "subTitle", "price"}, null);

        //System.out.println(searchSourceBuilder);
        return searchSourceBuilder;
    }
}
