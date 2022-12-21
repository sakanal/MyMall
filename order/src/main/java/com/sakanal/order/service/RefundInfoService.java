package com.sakanal.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.order.entity.RefundInfoEntity;

import java.util.Map;

/**
 * 退款信息
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:41:09
 */
public interface RefundInfoService extends IService<RefundInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

