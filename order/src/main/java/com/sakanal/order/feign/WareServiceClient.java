package com.sakanal.order.feign;

import com.sakanal.common.utils.R;
import com.sakanal.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "wareService",contextId = "order-ware")
public interface WareServiceClient {

    // 查询sku是否有库存
    @RequestMapping("/ware/waresku/hasStock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds);


    /**
     * 查询运费和收货地址信息
     */
    @GetMapping(value = "/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);


    /**
     * 锁定库存
     */
    @PostMapping(value = "/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
