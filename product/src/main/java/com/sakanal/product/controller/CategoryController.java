package com.sakanal.product.controller;

import com.sakanal.common.utils.R;
import com.sakanal.product.entity.CategoryEntity;
import com.sakanal.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;



/**
 * 商品三级分类
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 12:40:44
 */
@RefreshScope
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查出所有分类以及子分类，以树形结构组装起来
     */
    @GetMapping("/list/tree")
    // @RequiresPermissions("product:category:list")
    public R list(){
        List<CategoryEntity> categoryList = categoryService.listWithTree();

        return R.ok().put("data", categoryList);
    }


    /**
     * 信息
     */
    @GetMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateById(category);

        return R.ok();
    }

    /**
     * 修改分类
     */
    @PutMapping("/update/sort")
    // @RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity[] category){

        categoryService.updateBatchById(Arrays.asList(category));
        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    //@RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds){
        int result = categoryService.removeCategoryByIds(Arrays.asList(catIds));

        return result== catIds.length?R.ok():R.error("未完全删除");
    }

}
