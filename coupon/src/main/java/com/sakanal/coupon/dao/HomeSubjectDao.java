package com.sakanal.coupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakanal.coupon.entity.HomeSubjectEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页专题表【jd首页下面很多专题，每个专题链接新的页面，展示专题商品信息】
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:36:06
 */
@Mapper
public interface HomeSubjectDao extends BaseMapper<HomeSubjectEntity> {

}
