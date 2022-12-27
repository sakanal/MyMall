package com.sakanal.common.feign;

import com.sakanal.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("wareService")
public interface WareClient {

    @PostMapping("/ware/waresku/hasStock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

}
