package com.sakanal.search.controller;

import com.sakanal.search.service.MallSearchService;
import com.sakanal.search.vo.SearchParam;
import com.sakanal.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
public class SearchController {
    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping("list.html")
    public ModelAndView listPage(SearchParam searchParam, HttpServletRequest httpServletRequest){
        ModelAndView modelAndView = new ModelAndView("list");
        searchParam.set_queryString(httpServletRequest.getQueryString());
        SearchResult search = mallSearchService.search(searchParam);
        modelAndView.addObject("result",search);
        return modelAndView;
    }

}
