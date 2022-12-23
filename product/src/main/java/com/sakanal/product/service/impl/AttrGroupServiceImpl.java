package com.sakanal.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.product.dao.AttrGroupDao;
import com.sakanal.product.entity.AttrGroupEntity;
import com.sakanal.product.service.AttrGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }
    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {

        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        if (catelogId != 0){
            queryWrapper.eq("catelog_id", catelogId);
        }
        String searchKey = (String) params.get("key");
        if (StringUtils.hasText(searchKey)) {
            queryWrapper.and(wrapper -> wrapper.eq("attr_group_id", searchKey).or().like("attr_group_name", searchKey));
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);

//        // 查询所有
//        if (catelogId == 0) {
//            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), new QueryWrapper<>());
//            return new PageUtils(page);
//        }
//        // 查询指定的值
//        else {
//            String searchKey = (String) params.get("key");
//            QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId);
//            if (StringUtils.hasText(searchKey)) {
//                queryWrapper.and(wrapper -> wrapper.eq("attr_group_id", searchKey).or().like("attr_group_name", searchKey));
//            }
//            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),queryWrapper);
//            return new PageUtils(page);
//        }

    }


}
