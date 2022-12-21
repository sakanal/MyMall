package com.sakanal.coupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakanal.coupon.entity.CouponSpuRelationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券与产品关联
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:36:06
 */
@Mapper
public interface CouponSpuRelationDao extends BaseMapper<CouponSpuRelationEntity> {

}
