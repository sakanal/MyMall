package com.sakanal.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.product.dao.AttrDao;
import com.sakanal.product.entity.AttrEntity;
import com.sakanal.product.service.AttrService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

}
