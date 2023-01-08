package com.sakanal.thirdparty.controller;

import com.sakanal.common.utils.R;
import com.sakanal.thirdparty.component.SmsComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Resource
    private SmsComponent smsComponent;

    @RequestMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone,
                      @RequestParam("code") String code){
        log.info(phone+"==>"+code);
        smsComponent.sentSmsCode(phone,code);
        return R.ok();
    }
}
