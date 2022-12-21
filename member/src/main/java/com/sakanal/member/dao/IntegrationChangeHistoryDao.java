package com.sakanal.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sakanal.member.entity.IntegrationChangeHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分变化历史记录
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:40:02
 */
@Mapper
public interface IntegrationChangeHistoryDao extends BaseMapper<IntegrationChangeHistoryEntity> {

}
