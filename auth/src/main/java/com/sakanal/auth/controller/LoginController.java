package com.sakanal.auth.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.TypeReference;
import com.sakanal.common.bean.vo.MemberEntity;
import com.sakanal.common.bean.vo.UserLoginVo;
import com.sakanal.common.bean.vo.UserRegisterVo;
import com.sakanal.common.constant.AuthServerConstant;
import com.sakanal.common.exception.BizCodeEnum;
import com.sakanal.common.feign.MemberClient;
import com.sakanal.common.feign.SmsSendClient;
import com.sakanal.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class LoginController {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private MemberClient memberClient;

    @Resource
    private SmsSendClient smsSendClient;
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone){
        String codeRedis = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone);
        if (StringUtils.hasText(codeRedis)){
            long time = Long.parseLong(codeRedis.split("_")[1]);
            if (System.currentTimeMillis() - time <60000){
                // 60秒内不允许多次发送验证码
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        // 超过60秒或缓存中无验证码==>生成验证码
        String code = String.valueOf(RandomUtil.randomInt(10000, 100000));
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code+"_"+System.currentTimeMillis(),5, TimeUnit.MINUTES);

        log.info(phone+"==>"+code);
        smsSendClient.sendCode(phone, code);
        return R.ok();
    }


    /**
     * @param result 利用session原理，将数据放在session中，只要跳到下一个页面的取出这个数据以后，session里面的数据就会被删掉
     * @param redirectAttributes: 模拟重定向携带数据
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes){
        if (result.hasErrors()){
            //校验出错，转发到注册页
            Map<String, String> map = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors",map);
            return "redirect:http://auth.gulimall.com:9001/reg.html";
        }
        //真正注册，调用远程服务注册
        //1、校验验证码
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.hasText(s)){
            if (code.equals(s.split("_")[0])){
                //删除验证码;令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //验证码校验通过,真正注册，调用远程服务注册
                R r = memberClient.register(vo);
                if (r.getCode()==0){
                    //成功,转到登录页
                    return "redirect:http://auth.gulimall.com:9001/login.html";
                }else{
                    Map<String,Object> map = new HashMap<>();
                    map.put("msg",r.get("msg"));
                    System.out.println(map);
                    redirectAttributes.addFlashAttribute("errors",map);
                    return "redirect:http://auth.gulimall.com:9001/reg.html";
                }
            }else {
                Map<String,Object> map = new HashMap<>();
                map.put("code","验证码输入错误");
                redirectAttributes.addFlashAttribute("errors",map);
                //校验出错，转发到注册页
                return "redirect:http://auth.gulimall.com:9001/reg.html";
            }
        }else{
            Map<String,Object> map = new HashMap<>();
            map.put("code","验证码已过期");
            redirectAttributes.addFlashAttribute("errors",map);
            //校验出错，转发到注册页
            return "redirect:http://auth.gulimall.com:9001/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){
        //远程登录
        R r = memberClient.login(vo);
        if (r.getCode()==0){
            MemberEntity data = r.getData(new TypeReference<MemberEntity>() {});
            session.setAttribute(AuthServerConstant.SESSION_LOGIN_KEY,data);
            return "redirect:http://gulimall.com:9001";
        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com:9001/login.html";
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.SESSION_LOGIN_KEY);
        log.info(String.valueOf(attribute));
        if (attribute == null){
            //没登录去登录页
            return "login";
        }else {
            //已登录去首页
            return "redirect:http://gulimall.com:9001";
        }
    }
    @GetMapping("/logout.html")
    public String logout(HttpSession session){
        session.removeAttribute(AuthServerConstant.SESSION_LOGIN_KEY);
        return "redirect:http://gulimall.com:9001";
    }
}
