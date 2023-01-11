package com.sakanal.order.web;

import com.alipay.api.AlipayApiException;
import com.sakanal.order.config.AlipayTemplate;
import com.sakanal.order.service.OrderService;
import com.sakanal.order.vo.PayVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class PayWebController {
    @Resource
    private OrderService orderService;
    @Resource
    private AlipayTemplate alipayTemplate;
    /**
     * 用户下单:支付宝支付
     * 1、让支付页让浏览器展示 produces = "text/html"
     * 2、支付成功以后，跳转到用户的订单列表页
     */
    @ResponseBody
    @GetMapping(value = "/aliPayOrder",produces = "text/html")
    public String aliPayOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);//根据订单号获取订单信息
        String pay = alipayTemplate.pay(payVo);
        System.out.println(pay);
        return pay;
    }

}
