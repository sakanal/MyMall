package com.sakanal.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakanal.product.entity.CategoryBrandRelationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌分类关联
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 12:40:44
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

}
