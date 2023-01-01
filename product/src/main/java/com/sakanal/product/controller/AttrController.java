package com.sakanal.product.controller;

import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.R;
import com.sakanal.product.entity.ProductAttrValueEntity;
import com.sakanal.product.service.AttrService;
import com.sakanal.product.service.ProductAttrValueService;
import com.sakanal.product.vo.AttrVo;
import com.sakanal.product.vo.resp.AttrRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品属性
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 12:40:44
 */
@RefreshScope
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 获取基本属性和销售属性
     * @param params 基本分页参数和searchKey
     * @param attrType 1：基本属性，0：销售属性
     * @param catelogId 分类id
     * @return page<ArrtRespVo>
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("attrType") String attrType,
                          @PathVariable("catelogId") Long catelogId) {
        PageUtils page = attrService.queryBaseAttrPage(params,attrType, catelogId);
        return R.ok().put("page", page);
    }

    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListForSpu(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> entityList =  productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data", entityList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R attrInfo(@PathVariable("attrId") Long attrId){
//		AttrEntity attr = attrService.getById(attrId);
        AttrRespVo respVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
// @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updateAttr(attr);

        return R.ok();
    }


    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entityList) {
        productAttrValueService.updateSpuAttr(spuId, entityList);
        return R.ok();
    }

}
