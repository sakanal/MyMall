package com.sakanal.order.dao;

import com.sakanal.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:41:09
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
