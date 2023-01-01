package com.sakanal.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.product.dao.SkuInfoDao;
import com.sakanal.product.entity.SkuImagesEntity;
import com.sakanal.product.entity.SkuInfoEntity;
import com.sakanal.product.entity.SpuInfoDescEntity;
import com.sakanal.product.service.*;
import com.sakanal.product.vo.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    private SkuImagesService imagesService;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        queryWrapper.and(StringUtils.hasText(key),wrapper -> wrapper.eq("sku_id", key).or().like("sku_name", key));
//        if (StringUtils.hasText(key)) {
//            queryWrapper.and((wrapper) -> wrapper.eq("sku_id", key).or().like("sku_name", key));
//        }
        String catelogId = (String) params.get("catelogId");
        queryWrapper.eq(StringUtils.hasText(catelogId) && !"0".equalsIgnoreCase(catelogId),"catalog_id", catelogId);
//        if (StringUtils.hasText(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
//            queryWrapper.eq("catalog_id", catelogId);
//        }
        String brandId = (String) params.get("brandId");
        queryWrapper.eq(StringUtils.hasText(brandId) && !"0".equalsIgnoreCase(catelogId),"brand_id", brandId);
//        if (StringUtils.hasText(brandId) && !"0".equalsIgnoreCase(catelogId)) {
//            queryWrapper.eq("brand_id", brandId);
//        }
        String min = (String) params.get("min");
        queryWrapper.ge(StringUtils.hasText(min),"price",min);
//        if (StringUtils.hasText(min)) {
//            queryWrapper.ge("price", min);
//        }
        String max = (String) params.get("max");
        if (StringUtils.hasText(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception ignored) {
                // TODO 传递的值为英文 min/max
            }
        }
        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {

        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));

        return list;
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        //1、sku基本信息获取 pms_sku_info
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = getById(skuId);
            log.info(String.valueOf(info));
            skuItemVo.setInfo(info);
            return info;
        }, executor);

        //1.1、获取spu的销售属性组合
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuItemVo.SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        //1.2、获取spu的介绍 pms_spu_info_desc
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(res -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, executor);

        //1.3、获取spu的规格参数信息
        CompletableFuture<Void> attrGroupFuture = infoFuture.thenAcceptAsync(res -> {
            List<SkuItemVo.SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);

        //2、sku图片信息    pms_sku_images
        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImagesEntityList = imagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(skuImagesEntityList);
        }, executor);
//
//        CompletableFuture<Void> secKillFuture = CompletableFuture.runAsync(() -> {
//            //3、远程调用查询当前sku是否参与秒杀优惠活动
//            R skuSecKillInfo = secKillFeignService.getSkuSecKillInfo(skuId);
//            if (skuSecKillInfo.getCode() == 0) {
//                //查询成功
//                SkuItemVo.SecKillSkuVo seckilInfoData = skuSecKillInfo.getData("data", new TypeReference<SkuItemVo.SecKillSkuVo>() {
//                });
//                skuItemVo.setSecKillSkuVo(seckilInfoData);
//
//                if (seckilInfoData != null) {
//                    long currentTime = System.currentTimeMillis();
//                    if (currentTime > seckilInfoData.getEndTime()) {
//                        skuItemVo.setSecKillSkuVo(null);
//                    }
//                }
//            }
//        }, executor);

        //等待所有任务都完成
        try {
            CompletableFuture.allOf(saleAttrFuture,descFuture,attrGroupFuture,imgFuture).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return skuItemVo;
    }


}
