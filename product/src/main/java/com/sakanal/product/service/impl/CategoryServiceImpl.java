package com.sakanal.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.product.dao.CategoryDao;
import com.sakanal.product.entity.CategoryEntity;
import com.sakanal.product.service.CategoryBrandRelationService;
import com.sakanal.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1 查出所有分类
        List<CategoryEntity> all = baseMapper.selectList(null);
        // 2 组装成父子的树形结构
        return all.stream()
                //先获取顶层父类，ParentCid为0
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                //对获取到的父类进行组装，将它的子类放入
                .peek((categoryEntity)-> categoryEntity.setChildren(getChildren(categoryEntity,all)))
                //对结果排序
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                //搜集最终结果转为List
                .collect(Collectors.toList());
    }
    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                //找到当前父节点对应的所有子节点
                .filter(categoryEntity -> Objects.equals(categoryEntity.getParentCid(), root.getCatId()))
                //对获取到的父类进行组装，将它的子类放入
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity, all)))
                //对结果排序
                .sorted(Comparator.comparingInt(categoryEntity -> (categoryEntity.getSort() == null ? 0 : categoryEntity.getSort())))
                //搜集最终结果转为List
                .collect(Collectors.toList());
    }


    @Override
    public int removeCategoryByIds(List<Long> asList) {
        // TODO 1 检查当前删除的菜单，是否被别的地方引用

        // 逻辑删除
        return baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> CatelogPath = new ArrayList<>();
        findParentPath(catelogId,CatelogPath);
        return CatelogPath.toArray(new Long[0]);
    }

    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        if (!category.getName().isEmpty()) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

            // TODO 更新其他关联
        }
    }

    private void findParentPath(Long catelogId, List<Long> CatelogPath){
        CategoryEntity category = this.getById(catelogId);
        if (category.getParentCid() != 0){
            findParentPath(category.getParentCid(),CatelogPath);
        }
        CatelogPath.add(catelogId);
    }


}
