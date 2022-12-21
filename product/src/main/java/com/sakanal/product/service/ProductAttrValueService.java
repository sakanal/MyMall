package com.sakanal.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.product.entity.ProductAttrValueEntity;

import java.util.Map;

/**
 * spu属性值
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 12:40:44
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

