package com.atguigu.gmall.seach.service;

import com.atguigu.gmall.seach.pojo.SearchParamVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void search(SearchParamVo searchParamVo) {


        SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDSL(searchParamVo));

        try {
            //查询
            SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            parseReponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //解析结果


    }

    private void parseReponse(SearchResponse response){

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
                AggregationBuilders.nested("attrAgg","seachAttrValues")
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
