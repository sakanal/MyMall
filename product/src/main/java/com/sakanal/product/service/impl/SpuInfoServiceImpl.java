package com.sakanal.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakanal.common.bean.to.SkuEsModel;
import com.sakanal.common.bean.to.SkuHasStockVo;
import com.sakanal.common.bean.to.SkuReductionTo;
import com.sakanal.common.bean.to.SpuBoundTo;
import com.sakanal.common.constant.ProductConstant;
import com.sakanal.common.feign.CouponClient;
import com.sakanal.common.feign.SearchClient;
import com.sakanal.common.feign.WareClient;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.common.utils.R;
import com.sakanal.product.dao.SpuInfoDao;
import com.sakanal.product.entity.*;
import com.sakanal.product.service.*;
import com.sakanal.product.vo.SpuSaveVo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    private CouponClient couponClient;
    @Autowired
    private SearchClient searchClient;
    @Autowired
    private WareClient wareClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), new QueryWrapper<>());

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo,spuInfoEntity);
        this.save(spuInfoEntity);

        //2、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.save(spuInfoDescEntity);

        //3、保存spu的图片集 pms_spu_images
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);

        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrsList = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrsList.stream().map(baseAttrs -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(baseAttrs.getAttrId());
            AttrEntity attrEntity = attrService.getById(baseAttrs.getAttrId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setAttrValue(baseAttrs.getAttrValues());
            productAttrValueEntity.setQuickShow(baseAttrs.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());

            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities);

        // 5. 保存spu的积分信息，gulimall_sms -> sms_spu_bounds
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponClient.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        //6、保存当前spu对应的所有sku信息；
        List<Skus> skus = spuSaveVo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                //6.1）、sku的基本信息；pms_sku_info
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                // 设置属性
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                // 保存信息
                skuInfoService.save(skuInfoEntity);

                //6.2）、sku的图片信息；pms_sku_image
                // 保存skuInfoEntity后skuId自增生成
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    // 设置属性
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    // 过滤不带图片路径的值
                    //返回true就是需要，false就是剔除
                    return StringUtils.hasText(entity.getImgUrl());
                }).collect(Collectors.toList());

                // 批量保存图片信息
                skuImagesService.saveBatch(imagesEntities);

                //6.3）、sku的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attrList = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrList.stream().map(attr -> {
                    // 设置属性
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);

                    return attrValueEntity;
                }).collect(Collectors.toList());
                // 保存销售属性
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //6.4）、sku的优惠、满减等信息：gulimall_sms -> sms_sku_ladder\sms_sku_full_reduction
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
                    R r1 = couponClient.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }

            });
        }
    }
    // 查询表pms_spu_info
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            wrapper.and(w -> w.eq("id", key).or().like("spu_name", key));
        }

        String status = (String) params.get("status");
        if (StringUtils.hasText(status)) {
            wrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (StringUtils.hasText(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (StringUtils.hasText(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }


    @Override
    @Transactional
    public void up(Long spuId) {
        // 1. 查出当前spuId对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        // 4. 查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrListForSpu(spuId);
        // 4.1. 获取所有对应SpuId的attrId
        List<Long> attrIds = productAttrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        // 4.2. 在表pms_attr查找所有对应attr_id的实体
        Collection<AttrEntity> attrEntities = attrService.listByIds(attrIds);
        // 4.3. 过滤掉所有search_type为0的attr，返回课用于检索的attrIds
        Set<Long> idSet = attrEntities.stream().filter(item -> item.getSearchType() == 1).map(AttrEntity::getAttrId).collect(Collectors.toSet());

        // 4.4 productAttrValueEntities过滤掉不可用于检索的attr
        List<SkuEsModel.Attrs> skuEsModelAttrs = productAttrValueEntities.stream()
                .filter(item -> idSet.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                })
                .collect(Collectors.toList());

        /* ************************************************************************
         远程调用查询库存
         ************************************************************************ */

        // 获取哦skus中的所有skuId作为集合
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // TODO 发送远程调用，库存系统查询是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareClient.getSkuHasStock(skuIdList);
            Object data = r.get("data");

            // 需要导入fastjson包
            String s = JSON.toJSONString(data);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {};
            // com.alibaba.fastjson.TypeReference;
            List<SkuHasStockVo> skuHasStockVos = JSON.parseObject(s, typeReference);

            // 将skuHasStockVos映射为map，skuId为键，HasStock为值
            stockMap = skuHasStockVos.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));

        } catch (Exception e) {
            log.error("库存查询服务出现异常，原因{}", e);
        }

        // 2. 封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> collect = skus.stream().map(sku -> {
            // 组装需要的数据
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            // 手动赋值其他属性
            // TODO 热度评分功能未实现
            skuEsModel.setHotScore(0L);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());

            // 设置库存
            if (finalStockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            // TODO 查询品牌和分类的名字信息
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());

            CategoryEntity category = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(category.getName());

            // 设置检索属性
            skuEsModel.setAttrs(skuEsModelAttrs);

            return skuEsModel;

        }).collect(Collectors.toList());

        // TODO 将数据发送给es进行保存
        R r = searchClient.productStatusUp(collect);

        if (r.getCode() == 0) {
            // 远程调用成功
            // TODO 修改当前spu的状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // 远程调用失败
            // TODO 重复调用，接口幂等性，重试机制
        }
    }


}
