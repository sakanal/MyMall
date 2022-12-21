package com.sakanal.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.product.entity.AttrEntity;

import java.util.Map;

/**
 * 商品属性
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 12:40:44
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

