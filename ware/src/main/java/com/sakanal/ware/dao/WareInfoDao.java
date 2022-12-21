package com.sakanal.ware.dao;

import com.sakanal.ware.entity.WareInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库信息
 * 
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:42:15
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {
	
}
