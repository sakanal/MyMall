package com.sakanal.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakanal.common.bean.vo.SocialUser;
import com.sakanal.common.bean.vo.UserLoginVo;
import com.sakanal.common.bean.vo.UserRegisterVo;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:40:02
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterVo vo);

    void checkPhoneUnique(String phone);
    void checkUserNameUnique(String userName);

    MemberEntity login(UserLoginVo vo);

    MemberEntity login(SocialUser socialUser) throws Exception;

    MemberEntity login(String accessTokenInfo);
}

