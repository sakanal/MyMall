package com.sakanal.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakanal.member.entity.MemberCollectSubjectEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员收藏的专题活动
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:40:02
 */
@Mapper
public interface MemberCollectSubjectDao extends BaseMapper<MemberCollectSubjectEntity> {

}
