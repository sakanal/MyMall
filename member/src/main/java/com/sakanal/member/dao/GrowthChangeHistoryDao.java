package com.sakanal.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakanal.member.entity.GrowthChangeHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成长值变化历史记录
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:40:02
 */
@Mapper
public interface GrowthChangeHistoryDao extends BaseMapper<GrowthChangeHistoryEntity> {

}
