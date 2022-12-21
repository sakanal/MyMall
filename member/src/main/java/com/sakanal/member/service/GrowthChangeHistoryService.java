package com.sakanal.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.member.entity.GrowthChangeHistoryEntity;

import java.util.Map;

/**
 * 成长值变化历史记录
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 13:40:02
 */
public interface GrowthChangeHistoryService extends IService<GrowthChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

