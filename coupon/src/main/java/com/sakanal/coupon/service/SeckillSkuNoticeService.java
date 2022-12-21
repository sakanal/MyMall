package com.sakanal.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.coupon.entity.SeckillSkuNoticeEntity;

import java.util.Map;

/**
 * 秒杀商品通知订阅
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:36:06
 */
public interface SeckillSkuNoticeService extends IService<SeckillSkuNoticeEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

