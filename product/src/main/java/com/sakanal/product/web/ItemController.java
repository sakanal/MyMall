package com.sakanal.product.web;

import com.sakanal.product.service.SkuInfoService;
import com.sakanal.product.vo.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class ItemController {
    @Autowired
    private SkuInfoService skuInfoService;
    @GetMapping("/{skuId}.html")
    public ModelAndView skuItem(@PathVariable("skuId")Long skuId){
        ModelAndView modelAndView = new ModelAndView("item");
        System.out.println(skuId);
        SkuItemVo skuInfoVo = skuInfoService.item(skuId);
        log.info(String.valueOf(skuInfoVo));
        modelAndView.addObject("item",skuInfoVo);
        return modelAndView;
    }
}
