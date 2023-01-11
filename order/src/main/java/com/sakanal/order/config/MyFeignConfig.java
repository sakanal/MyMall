package com.sakanal.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Configuration
public class MyFeignConfig {
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor(){
            @Override
            public void apply(RequestTemplate template) {
                log.info("处理Feign请求，添加Cookie");
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (requestAttributes!=null){
                    HttpServletRequest request = requestAttributes.getRequest();
                    //同步请求头数据，cookie
                    String oldCookie = request.getHeader("Cookie");
                    template.header("Cookie",oldCookie);
                }else {
                    log.info("ServletRequestAttributes。。。null");
                }
            }
        };
    }
}
