package com.sakanal.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.product.dao.ProductAttrValueDao;
import com.sakanal.product.entity.ProductAttrValueEntity;
import com.sakanal.product.service.ProductAttrValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Autowired
    private ProductAttrValueDao productAttrValueDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(new Query<ProductAttrValueEntity>().getPage(params), new QueryWrapper<>());
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entityList) {
        entityList = entityList.stream().peek(productAttrValueEntity -> productAttrValueEntity.setSpuId(spuId)).collect(Collectors.toList());
        // 数据库中已经存在的数据
        List<ProductAttrValueEntity> productAttrValueList = productAttrValueDao.selectList(new LambdaQueryWrapper<ProductAttrValueEntity>().eq(ProductAttrValueEntity::getSpuId, spuId));
        List<ProductAttrValueEntity> updateEntityList = new ArrayList<>(entityList);

        // 获取数据库中不存在的数据，该数据需要新增
        entityList.removeAll(productAttrValueList);
        // 该部分数据需要更新
        updateEntityList.retainAll(productAttrValueList);

        updateEntityList.forEach(productAttrValueEntity -> productAttrValueDao.update(productAttrValueEntity, new UpdateWrapper<ProductAttrValueEntity>().eq("spu_id", spuId).eq("attr_id", productAttrValueEntity.getAttrId())));
        this.saveBatch(entityList);
    }
    @Override
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {
        return this.baseMapper.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
    }

}
