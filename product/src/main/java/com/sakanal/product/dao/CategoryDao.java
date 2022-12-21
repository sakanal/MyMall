package com.sakanal.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakanal.product.entity.CategoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 12:40:44
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {

}
