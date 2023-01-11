package com.sakanal.ware.feign;

import com.sakanal.common.bean.entity.OrderEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "orderService",contextId = "ware-order")
public interface OrderClient {
    /**
     * 根据订单编号查询订单状态
     */
    @GetMapping(value = "/order/order/status/{orderSn}")
    public OrderEntity getOrderStatus(@PathVariable("orderSn") String orderSn);
}
