package com.sakanal.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.sakanal.common.bean.to.SkuEsModel;
import com.sakanal.common.constant.EsConstant;
import com.sakanal.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {

        // 1. 给es中建立索引，product，建立好映射关系

        // 2. 保存数据
        BulkRequest.Builder builder = new BulkRequest.Builder();
        skuEsModels.forEach(skuEsModel -> {
            builder.operations(bulk -> bulk.index(index -> {
                        return index.index(EsConstant.PRODUCT_INDEX)
                                .id(String.valueOf(skuEsModel.getSkuId()))
                                .document(skuEsModel);
                    })
            );
        });
        BulkResponse bulkResponse = elasticsearchClient.bulk(builder.build());
        boolean errors = bulkResponse.errors();
        if (errors){
            log.info(bulkResponse.toString());
        }else {
            List<String> collect = bulkResponse.items().stream().map(BulkResponseItem::id).collect(Collectors.toList());
            log.info("商品上架完成: {}", collect);
        }

        return !errors;

//
//
//        boolean b = bulk.hasFailures();
//        if (b) {
//            bulk.buildFailureMessage();
//        } else {
//            List<String> collect = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
//            log.info("商品上架完成: {}", collect);
//        }
//        return !b;
    }

}
