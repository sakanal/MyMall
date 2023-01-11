package com.sakanal.order.feign;

import com.sakanal.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient(value = "cartService",contextId = "order-cart")
public interface CartServiceClient {

    @GetMapping(value = "/currentUserCartItems")
    @ResponseBody
    public List<OrderItemVo> getCurrentCartItems();
}
