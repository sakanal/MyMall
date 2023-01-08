package com.sakanal.common.feign;

import com.sakanal.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("thirdParty")
public interface SmsSendClient {

    @RequestMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone,
                      @RequestParam("code") String code);
}
