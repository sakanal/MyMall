package com.sakanal.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.coupon.entity.HomeAdvEntity;

import java.util.Map;

/**
 * 首页轮播广告
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:36:06
 */
public interface HomeAdvService extends IService<HomeAdvEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

