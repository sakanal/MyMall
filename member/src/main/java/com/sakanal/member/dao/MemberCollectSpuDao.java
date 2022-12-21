package com.sakanal.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakanal.member.entity.MemberCollectSpuEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员收藏的商品
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:40:02
 */
@Mapper
public interface MemberCollectSpuDao extends BaseMapper<MemberCollectSpuEntity> {

}
