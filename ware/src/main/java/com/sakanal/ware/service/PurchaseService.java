package com.sakanal.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.ware.entity.PurchaseEntity;

import java.util.Map;

/**
 * 采购信息
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:42:15
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

