package com.sakanal.member.dao;

import com.sakanal.member.entity.MemberStatisticsInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员统计信息
 * 
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:40:03
 */
@Mapper
public interface MemberStatisticsInfoDao extends BaseMapper<MemberStatisticsInfoEntity> {
	
}
