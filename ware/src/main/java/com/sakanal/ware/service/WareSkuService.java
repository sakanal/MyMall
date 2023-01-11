package com.sakanal.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakanal.common.bean.to.OrderTo;
import com.sakanal.common.bean.to.SkuHasStockVo;
import com.sakanal.common.bean.to.mq.StockLockedTo;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.ware.entity.WareSkuEntity;
import com.sakanal.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:42:15
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    boolean orderLockStock(WareSkuLockVo vo);

    void unlockStock(OrderTo orderTo);

    void unlockStock(StockLockedTo to);
}

