package com.sakanal.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.coupon.entity.CouponHistoryEntity;

import java.util.Map;

/**
 * 优惠券领取历史记录
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:36:06
 */
public interface CouponHistoryService extends IService<CouponHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

