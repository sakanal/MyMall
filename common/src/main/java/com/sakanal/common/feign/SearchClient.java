package com.sakanal.common.feign;

import com.sakanal.common.bean.to.SkuEsModel;
import com.sakanal.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("searchService")
public interface SearchClient {

    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
