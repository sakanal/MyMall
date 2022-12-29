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
import com.sakanal.product.vo.Catelog2Vo;
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
    @Override
    public List<CategoryEntity> getLevel_1_Categorys() {
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntityList;
    }


    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {

        // 查询出表pms_category所有的记录实体
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);

        // 查出所有的一次分类
        List<CategoryEntity> level_1_categorys = getParentCid(categoryEntityList, 0L);

        // 封装数据，构造一个以1级id为键，2级分类列表为值的map
        Map<String, List<Catelog2Vo>> collect = level_1_categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
            // 根据一级分类id查找二级分类
            List<CategoryEntity> level_2_categorys = getParentCid(categoryEntityList, l1.getCatId());
            // 封装结果为Catelog2Vo的集合
            List<Catelog2Vo> catelog2Vos = null;
            if (level_2_categorys != null) {
                // 把 level_2_categorys 封装为 catelog2Vos
                catelog2Vos = level_2_categorys.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(l1.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 根据二级分类id查找三级分类
                    List<CategoryEntity> level_3_categorys = getParentCid(categoryEntityList, l2.getCatId());
                    // 将 level_3_categorys 封装为 catelog3Vos
                    if (level_3_categorys != null) {
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = level_3_categorys.stream().map(l3 -> {
                            return new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        return collect;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList, Long parent_cid) {
        return selectList.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
    }


    private void findParentPath(Long catelogId, List<Long> CatelogPath){
        CategoryEntity category = this.getById(catelogId);
        if (category.getParentCid() != 0){
            findParentPath(category.getParentCid(),CatelogPath);
        }
        CatelogPath.add(catelogId);
    }

}
