package com.sakanal.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.sakanal.common.bean.vo.SocialUser;
import com.sakanal.common.bean.vo.UserLoginVo;
import com.sakanal.common.bean.vo.UserRegisterVo;
import com.sakanal.common.exception.BizCodeEnum;
import com.sakanal.common.exception.RRException;
import com.sakanal.common.utils.HttpClientUtils;
import com.sakanal.common.utils.HttpUtils;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.member.dao.MemberDao;
import com.sakanal.member.dao.MemberLevelDao;
import com.sakanal.member.entity.MemberEntity;
import com.sakanal.member.entity.MemberLevelEntity;
import com.sakanal.member.service.MemberService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(UserRegisterVo vo) throws RRException  {
        MemberEntity memberEntity = new MemberEntity();
        //设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.selectOne(new LambdaQueryWrapper<MemberLevelEntity>().eq(MemberLevelEntity::getDefaultStatus, 1));
        memberEntity.setLevelId(memberLevelEntity.getId());
        //设置手机号
        checkPhoneUnique(vo.getPhone());
        memberEntity.setMobile(vo.getPhone());
        //设置用户名
        checkUserNameUnique(vo.getUserName());
        memberEntity.setUsername(vo.getUserName());
        //设置密码
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassWord());
        memberEntity.setPassword(encode);
        //设置初始积分
        memberEntity.setIntegration(0);
        //设置初始成长值
        memberEntity.setGender(0);
        //设置启用状态
        memberEntity.setStatus(1);

        this.save(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws RRException {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getMobile, phone));
        if (count>0){
            throw new RRException(BizCodeEnum.PHONE_EXISTS_EXCEPTION.getMsg(),BizCodeEnum.PHONE_EXISTS_EXCEPTION.getCode());
        }
    }

    @Override
    public void checkUserNameUnique(String userName) throws RRException {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getUsername, userName));
        if (count>0){
            throw new RRException(BizCodeEnum.USER_EXISTS_EXCEPTION.getMsg(),BizCodeEnum.USER_EXISTS_EXCEPTION.getCode());
        }
    }

    @Override
    public MemberEntity login(UserLoginVo vo) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        passwordEncoder.encode(vo.getPassWord());
        LambdaQueryWrapper<MemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberEntity::getUsername,vo.getLoginAccount())
                .or().eq(MemberEntity::getMobile,vo.getLoginAccount());
        List<MemberEntity> memberEntities = baseMapper.selectList(queryWrapper);
        ArrayList<MemberEntity> result = new ArrayList<>();
        if (memberEntities!=null && memberEntities.size()>0){
            for (MemberEntity memberEntity : memberEntities) {
                String password = memberEntity.getPassword();
                boolean matches = passwordEncoder.matches(vo.getPassWord(), password);
                if (matches){
                    result.add(memberEntity);
                }
            }
            if (result.size() == 1){
                return result.get(0);
            }else {
                return null;
            }
        }else {
            return null;
        }
    }


    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {

        //具有登录和注册逻辑
        String uid = socialUser.getUid();

        //1、判断当前社交用户是否已经登录过系统
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));

        if (memberEntity != null) {
            //这个用户已经注册过
            //更新用户的访问令牌的时间和access_token
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            this.baseMapper.updateById(update);

            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;
        } else {
            //2、没有查到当前社交用户对应的记录我们就需要注册一个
            MemberEntity register = new MemberEntity();
            //3、查询当前社交用户的社交账号信息（昵称、性别等）
            Map<String,String> query = new HashMap<>();
            query.put("access_token",socialUser.getAccess_token());
            query.put("uid",socialUser.getUid());
            HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);

            if (response.getStatusLine().getStatusCode() == 200) {
                //查询成功
                String json = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = JSON.parseObject(json);
                String name = jsonObject.getString("name");
                String gender = jsonObject.getString("gender");
                String profileImageUrl = jsonObject.getString("profile_image_url");

                register.setNickname(name);
                register.setGender("m".equals(gender)?1:0);
                register.setHeader(profileImageUrl);
                register.setSocialUid(socialUser.getUid());
                register.setAccessToken(socialUser.getAccess_token());
                register.setExpiresIn(socialUser.getExpires_in());

                //把用户信息插入到数据库中
                this.baseMapper.insert(register);

            }
            return register;
        }

    }

    @Override
    public MemberEntity login(String accessTokenInfo) {

        //从accessTokenInfo中获取出来两个值 access_token 和 oppenid
        //把accessTokenInfo字符串转换成map集合，根据map里面中的key取出相对应的value
        Gson gson = new Gson();
        HashMap accessMap = gson.fromJson(accessTokenInfo, HashMap.class);
        String accessToken = (String) accessMap.get("access_token");
        String openid = (String) accessMap.get("openid");

        //3、拿到access_token 和 oppenid，再去请求微信提供固定的API，获取到扫码人的信息
        //TODO 查询数据库当前用用户是否曾经使用过微信登录

        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", openid));

        if (memberEntity == null) {
            System.out.println("新用户注册");
            //访问微信的资源服务器，获取用户信息
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openid=%s";
            String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openid);
            //发送请求
            String resultUserInfo = null;
            try {
                resultUserInfo = HttpClientUtils.get(userInfoUrl);
                System.out.println("resultUserInfo==========" + resultUserInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //解析json
            HashMap userInfoMap = gson.fromJson(resultUserInfo, HashMap.class);
            String nickName = (String) userInfoMap.get("nickname");      //昵称
            Double sex = (Double) userInfoMap.get("sex");        //性别
            String headimgurl = (String) userInfoMap.get("headimgurl");      //微信头像

            //把扫码人的信息添加到数据库中
            memberEntity = new MemberEntity();
            memberEntity.setNickname(nickName);
            memberEntity.setGender(sex.intValue());
            memberEntity.setHeader(headimgurl);
            memberEntity.setSocialUid(openid);
            // register.setExpiresIn(socialUser.getExpires_in());
            this.baseMapper.insert(memberEntity);
        }
        return memberEntity;
    }



}
