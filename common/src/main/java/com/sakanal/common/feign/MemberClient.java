package com.sakanal.common.feign;

import com.sakanal.common.bean.vo.MemberAddressVo;
import com.sakanal.common.bean.vo.SocialUser;
import com.sakanal.common.bean.vo.UserLoginVo;
import com.sakanal.common.bean.vo.UserRegisterVo;
import com.sakanal.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("memberService")
public interface MemberClient {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    @PostMapping(value = "/member/member/login")
    public R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauth2Login(@RequestBody SocialUser vo) throws Exception;

    @PostMapping(value = "/member/member/weixin/login")
    R weixinLogin(@RequestParam("accessTokenInfo") String accessTokenInfo);

    @GetMapping("/member/memberreceiveaddress/address/{addressId}")
    public MemberAddressVo getAddressInfo(@PathVariable("addressId") Long addressId);
}
