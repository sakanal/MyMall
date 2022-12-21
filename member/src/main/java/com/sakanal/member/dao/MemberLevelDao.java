package com.sakanal.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakanal.member.entity.MemberLevelEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:40:02
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

}
