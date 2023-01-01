package com.sakanal.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.product.dao.AttrAttrgroupRelationDao;
import com.sakanal.product.dao.AttrDao;
import com.sakanal.product.dao.AttrGroupDao;
import com.sakanal.product.entity.AttrAttrgroupRelationEntity;
import com.sakanal.product.entity.AttrEntity;
import com.sakanal.product.entity.AttrGroupEntity;
import com.sakanal.product.service.AttrGroupService;
import com.sakanal.product.vo.AttrGroupRelationVo;
import com.sakanal.product.vo.AttrGroupWithAttrsVo;
import com.sakanal.product.vo.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    AttrDao attrDao;

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

    @Override
    public void deleteRelation(AttrGroupRelationVo[] attrGroupRelationVos) {
        List<AttrAttrgroupRelationEntity> relationEntityList = Arrays.stream(attrGroupRelationVos).map(attrGroupRelationVo -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(attrGroupRelationVo, attrAttrgroupRelationEntity);
            return attrAttrgroupRelationEntity;
        }).collect(Collectors.toList());
        baseMapper.deleteBatchRelation(relationEntityList);
    }


    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {

        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        return attrGroupEntities.stream().map(item -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);

            // 查询出所有的关联对象
            List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", item.getAttrGroupId()));

            // 查询出所有的attr_id
            List<Long> attrIds = attrAttrgroupRelationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

            // 根据attr_id集合查询出所有的attr对象
            if (attrIds.size() > 0) {
                List<AttrEntity> attrEntities = attrDao.selectBatchIds(attrIds);
                // 封装进attrGroupWithAttrsVo
                attrGroupWithAttrsVo.setAttrs(attrEntities);
            }

            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        //查出当前spu对应的所有属性的分组信息，当前分组下的所有属性对应的值
        List<SkuItemVo.SpuItemAttrGroupVo> vos = this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return null;
    }


}
