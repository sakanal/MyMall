package com.sakanal.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakanal.ware.entity.WareSkuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:42:15
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(@Param("skuId") Long skuId);

    void unLockStock(@Param("skuId") Long skuId, @Param("wareId")Long wareId, @Param("num")Integer num);

    Long lockSkuStock(@Param("skuId")Long skuId, @Param("wareId")Long wareId, @Param("num")Integer num);

    List<Long> listWareIdHasSkuStock(@Param("skuId")Long skuId);
}

