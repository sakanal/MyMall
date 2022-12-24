package com.sakanal.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.product.dao.BrandDao;
import com.sakanal.product.dao.CategoryBrandRelationDao;
import com.sakanal.product.dao.CategoryDao;
import com.sakanal.product.entity.BrandEntity;
import com.sakanal.product.entity.CategoryBrandRelationEntity;
import com.sakanal.product.entity.CategoryEntity;
import com.sakanal.product.service.CategoryBrandRelationService;
import com.sakanal.product.vo.resp.BrandRespVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Autowired
    BrandDao brandDao;

    @Autowired
    CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {

        // 取出categoryBrandRelation里的brandId和catelogId
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        // 根据id查询数据库里存储的对象
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        // 设置categoryBrandRelation对象的商品名称和类别名
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        // 因为前端传来的数据中不包含 商品名 和 类别名，需要查询出他们后在保存
        this.save(categoryBrandRelation);

    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setBrandId(brandId);
        relationEntity.setBrandName(name);
        this.update(relationEntity, new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setCatelogId(catId);
        relationEntity.setCatelogName(name);
        this.update(relationEntity, new UpdateWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
    }


    @Override
    public List<BrandRespVo> getByCatId(Long catId) {
        List<CategoryBrandRelationEntity> entities = this.list(new QueryWrapper<CategoryBrandRelationEntity>()
                .eq("catelog_id", catId));

        return entities.stream().map(item -> {
            BrandRespVo brandRespVo = new BrandRespVo();
            BeanUtils.copyProperties(item, brandRespVo);
            return brandRespVo;
        }).collect(Collectors.toList());
    }



}
