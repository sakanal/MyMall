package com.sakanal.ware.dao;

import com.sakanal.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:42:15
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
