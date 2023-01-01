package com.sakanal.search.service.impl;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.json.JsonData;
import com.sakanal.common.bean.to.SkuEsModel;
import com.sakanal.common.constant.EsConstant;
import com.sakanal.common.feign.ProductClient;
import com.sakanal.search.service.MallSearchService;
import com.sakanal.search.vo.SearchParam;
import com.sakanal.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private ProductClient productClient;

    @Override
    public SearchResult search(SearchParam param){
        SearchResult searchResult = new SearchResult();
        //1构建检索请求
        SearchRequest searchRequest = buildSearchRequest(param);
        try {
            //2执行检索
            SearchResponse<SkuEsModel> search = elasticsearchClient.search(searchRequest, SkuEsModel.class);

            //3分析结果，封装数据
            searchResult = buildSearchResult(search,param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }


    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchRequest.Builder builder = new SearchRequest.Builder().index(EsConstant.PRODUCT_INDEX);
        /*
          模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
         */
        // 基础bool
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        // must-模糊查询
        if (StringUtils.hasText(param.getKeyword())){
            boolQueryBuilder.must(must->must.match(matchQurey->matchQurey.field("skuTitle").query(param.getKeyword())));
        }
        // filter 三级分类id
        if (param.getCatalog3Id()!=null){
            boolQueryBuilder.filter(filter-> filter.term(term->term.field("catalogId").value(param.getCatalog3Id())));
        }
        // filter 品牌id
        if (param.getBrandId()!=null && param.getBrandId().size()>0){
            boolQueryBuilder.filter(filter->filter.terms(terms->terms.field("brandId").terms(field->{
                List<Long> brandIds = param.getBrandId();
                List<FieldValue> fieldValues = new ArrayList<>();
                for (Long brandId : brandIds) {
                    fieldValues.add(FieldValue.of(brandId));
                }
                return field.value(fieldValues);
            })));
        }
        // filter 按照所有指定的属性规格查询
        if (param.getAttrs()!=null && param.getAttrs().size()>0){
            List<String> attrs = param.getAttrs();
            //attrs=1_5寸&attrs2_1G:2G
            for (String attr : attrs) {
                //1_5寸:6寸
                String[] split = attr.split("_");
                String attrId = split[0];
                String[] attrValues = split[1].split(":");
                boolQueryBuilder.filter(filter->filter.nested(nested->{
                    return nested.path("attrs").scoreMode(ChildScoreMode.None)
                            .query(query->query.bool(bool->{
                                return bool.must(must->must.term(term->term.field("attrs.attrId").value(attrId)))
                                        .must(must-> must.terms(terms->{
                                            List<FieldValue> fieldValues = new ArrayList<>();
                                            for (String attrValue : attrValues) {
                                                fieldValues.add(FieldValue.of(attrValue));
                                            }
                                            return terms.field("attrs.attrValue").terms(t->t.value(fieldValues));
                                        }));
                        }));
                }));
            }
        }
        // filter 是否有库存
        if (param.getHasStock()!=null){
            boolQueryBuilder.filter(filter->filter.term(term-> term.field("hasStock").value(param.getHasStock() == 1)));
        }
        // filter 价格区间  1_500/_500/500_
        if (StringUtils.hasText(param.getSkuPrice())){
            boolQueryBuilder.filter(filter->filter.range(range->{
                range.field("skuPrice");
                String skuPrice = param.getSkuPrice();
                String[] split = skuPrice.split("_");
                // 1_500/_500/1_
                if (split.length==2){
                    if (Objects.equals(split[0], "")){
                        //_500
                        return range.lte(JsonData.fromJson(split[1]));
                    }else {
                        //1_500
                        return range.gte(JsonData.fromJson(split[0])).lte(JsonData.fromJson(split[1]));
                    }
                }else if (split.length==1){
                    // 1_
                    return range.gte(JsonData.fromJson(split[0]));
                }else {
                    // _
                    return range.gte(JsonData.fromJson("0"));
                }
            }));
        }

        builder.query(query->query.bool(boolQueryBuilder.build()));
        /*
          排序，分页，高亮
         */
        // 排序 sort=saleCount_asc/desc
        if (StringUtils.hasText(param.getSort())){
            String sortType = param.getSort();
            String[] split = sortType.split("_");
            builder.sort(sort->sort.field(field-> {
                field.field(split[0]);
                if ("desc".equalsIgnoreCase(split[1])){
                    return field.order(SortOrder.Desc);
                }else {
                    return field.order(SortOrder.Asc);
                }
            }));
        }
        // 分页
        builder.from((param.getPageNum()-1)*EsConstant.PRODUCT_PAGE_SIZE);
        builder.size(EsConstant.PRODUCT_PAGE_SIZE);
        // 高亮
        if (StringUtils.hasText(param.getKeyword())){
            builder.highlight(highlight->highlight.fields("skuTitle",field->{
                return field.preTags("<b style='color: red'>")
                        .postTags("</b>");
            }));
        }

        /*
          聚合分析
         */
        //品牌聚合
        builder.aggregations("agg&brand",aggregation->{
            return aggregation.terms(terms->terms.field("brandId"))
                    .aggregations("agg&brand&name",aggre->aggre.terms(terms->terms.field("brandName")))
                    .aggregations("agg&brand&img",aggre->aggre.terms(terms->terms.field("brandImg")));
        //分类聚合
        }).aggregations("agg&catalog",aggregation->{
            return aggregation.terms(terms->terms.field("catalogId"))
                    .aggregations("agg&catalog&name",aggre->aggre.terms(terms->terms.field("catalogName").size(1)));
        //属性聚合
        }).aggregations("agg&attr",aggregation-> aggregation.nested(nested->nested.path("attrs"))
                .aggregations("agg&attr&id",aggre-> {
                    return aggre.terms(terms -> terms.field("attrs.attrId"))
                            .aggregations("agg&attr&name",agg->agg.terms(terms->terms.field("attrs.attrName").size(1)))
                            .aggregations("agg&attr&value",agg->agg.terms(terms->terms.field("attrs.attrValue")));
                }));


        return builder.build();
    }

    private SearchResult buildSearchResult(SearchResponse<SkuEsModel> searchResponse, SearchParam param) {
        SearchResult searchResult = new SearchResult();
        //所有查询到的商品
        List<Hit<SkuEsModel>> hitList = searchResponse.hits().hits();
        List<SkuEsModel> skuEsModelList = new ArrayList<>();
        if (hitList!=null && hitList.size()>0) {
            for (Hit<SkuEsModel> skuEsModelHit : hitList) {
                SkuEsModel source = skuEsModelHit.source();
                if (StringUtils.hasText(param.getKeyword()) && source!=null) {
                    List<String> skuTitle = skuEsModelHit.highlight().get("skuTitle");
                    String highlight = skuTitle.get(0);
                    source.setSkuTitle(highlight);
                }
                skuEsModelList.add(source);
            }
        }
        searchResult.setProducts(skuEsModelList);


        Map<String, Aggregate> aggregateMap = searchResponse.aggregations();
        //所有聚合的商品涉及的属性信息
        List<LongTermsBucket> attrBuckets = aggregateMap.get("agg&attr").nested().aggregations().get("agg&attr&id").lterms().buckets().array();
        ArrayList<SearchResult.AttrVo> attrVos = new ArrayList<>();
        for (LongTermsBucket longTermsBucket : attrBuckets) {
            String attrId = longTermsBucket.key();
            String attrName = longTermsBucket.aggregations().get("agg&attr&name").sterms().buckets().array().get(0).key().stringValue();
            // 属性消息可能选多个
            List<StringTermsBucket> array = longTermsBucket.aggregations().get("agg&attr&value").sterms().buckets().array();
//            ArrayList<String> attrValueList = new ArrayList<>();
//            for (StringTermsBucket stringTermsBucket : array) {
//                String attrValue = stringTermsBucket.key().stringValue();
//                attrValueList.add(attrValue);
//            }
            List<String> attrValueList = array.stream().map(stringTermsBucket -> stringTermsBucket.key().stringValue()).collect(Collectors.toList());
            if (StringUtils.hasText(attrId)) {
                SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
                attrVo.setAttrId(Long.valueOf(attrId));
                attrVo.setAttrName(attrName);
                attrVo.setAttrValue(attrValueList);
                attrVos.add(attrVo);
            }
        }
        searchResult.setAttrs(attrVos);

        //所有聚合的商品涉及的品牌信息
        List<LongTermsBucket> brandBuckets = aggregateMap.get("agg&brand").lterms().buckets().array();
        ArrayList<SearchResult.BrandVo> brandVos = new ArrayList<>();
        for (LongTermsBucket brandBucket : brandBuckets) {
            String brandId = brandBucket.key();
            List<StringTermsBucket> brandNameBucket = brandBucket.aggregations().get("agg&brand&name").sterms().buckets().array();
            String brandName = brandNameBucket.get(0).key().stringValue();
            List<StringTermsBucket> brandImgBucket = brandBucket.aggregations().get("agg&brand&img").sterms().buckets().array();
            String brandImg = brandImgBucket.get(0).key().stringValue();
            if (brandId!=null){
                SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
                brandVo.setBrandId(Long.valueOf(brandId));
                brandVo.setBrandName(brandName);
                brandVo.setBrandImg(brandImg);
                brandVos.add(brandVo);
            }
        }
        searchResult.setBrands(brandVos);

        //所有聚合的商品涉及的分类信息
        List<LongTermsBucket> catalogBuckets = aggregateMap.get("agg&catalog").lterms().buckets().array();
        ArrayList<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        for (LongTermsBucket catalogBucket : catalogBuckets) {
            String catalogId = catalogBucket.key();
            String catalogName = catalogBucket.aggregations().get("agg&catalog&name").sterms().buckets().array().get(0).key().stringValue();
            if (StringUtils.hasText(catalogId)) {
                SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
                catalogVo.setCatalogId(Long.valueOf(catalogId));
                catalogVo.setCatalogName(catalogName);
                catalogVos.add(catalogVo);
            }
        }
        searchResult.setCatalogs(catalogVos);


        //分页消息 - 总记录数
        TotalHits totalHits = searchResponse.hits().total();
        long total = 0;
        if (totalHits!=null){
            total = totalHits.value();
        }
        searchResult.setTotal(total);
        //分页消息 - 页码
        searchResult.setPageNum(param.getPageNum());
        //分页消息 - 总页码
        int totalPages = (int) Math.ceil((double) total / EsConstant.PRODUCT_PAGE_SIZE);
        searchResult.setTotalPages(totalPages);
        // 分页消息 - 分页码
        ArrayList<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);


        // 构建面包屑导航功能  属性相关
        if (param.getAttrs() !=null && param.getAttrs().size()>0){
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                //1、分析每个attrs传递过来的参数值
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                //1_1G:2G
                String[] s = attr.split("_");
                attrVos.forEach(attrVo -> {
                    if (s[0].equals(attrVo.getAttrId().toString())){
                        navVo.setNavName(attrVo.getAttrName());
                    }
                });
                navVo.setNavValue(s[1]);
                searchResult.getAttrIds().add(Long.valueOf(s[0]));
                //2、取消了这个面包屑之后，跳转的地方，将请求的地址的url里面的当前条件置空
                //拿到所有的查询条件去掉当前
                String replace = replaceQueryString(param, attr,"attrs");
                navVo.setLink("http://search.gulimall.com:9001/list.html?"+replace);

                return navVo;
            }).collect(Collectors.toList());
            searchResult.setNavs(collect);
        }
        //品牌相关
        if (param.getBrandId() != null && param.getBrandId().size() >0){
            List<SearchResult.NavVo> navs = searchResult.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            StringBuilder builder = new StringBuilder();
            String replace = "";
            for (SearchResult.BrandVo brandVo : brandVos) {
                builder.append(brandVo.getBrandName()).append(";");
                replace = replaceQueryString(param, brandVo.getBrandId()+"","brandId");
            }
            navVo.setNavValue(builder.toString());
            navVo.setLink("http://search.gulimall.com:9001/list.html?"+replace);
            navs.add(navVo);
            searchResult.setNavs(navs);

        }

        return searchResult;
    }

    private String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+", "%20");//浏览器和java对+号的差异化处理
            encode = encode.replace("%28", "(");//浏览器和java对(号的差异化处理
            encode = encode.replace("%29", ")");//浏览器和java对)号的差异化处理
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return param.get_queryString().replace(key+"=" + encode, "");
    }



}

