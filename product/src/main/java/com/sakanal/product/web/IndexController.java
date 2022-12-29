package com.sakanal.product.web;

import com.sakanal.product.entity.CategoryEntity;
import com.sakanal.product.service.CategoryService;
import com.sakanal.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @GetMapping({"/","index.html"})
    public ModelAndView toIndex(){
        ModelAndView modelAndView = new ModelAndView("index");
        // TODO 查出所有的1级分类
        List<CategoryEntity> categoryEntityList =  categoryService.getLevel_1_Categorys();
        modelAndView.addObject("categorys",categoryEntityList);
        return modelAndView;
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        Map<String, List<Catelog2Vo>> catalogJson =  categoryService.getCatalogJson();
        return catalogJson;
    }

}
