package com.sakanal.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.ware.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:42:15
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

