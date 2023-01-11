package com.sakanal.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakanal.order.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:41:09
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    void updateOrderStatus(@Param("orderSn") String orderSn,
                           @Param("code")Integer code,
                           @Param("payType")Integer payType);
}
