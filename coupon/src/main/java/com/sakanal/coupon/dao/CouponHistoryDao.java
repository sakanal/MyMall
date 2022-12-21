package com.sakanal.coupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakanal.coupon.entity.CouponHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:36:06
 */
@Mapper
public interface CouponHistoryDao extends BaseMapper<CouponHistoryEntity> {

}
