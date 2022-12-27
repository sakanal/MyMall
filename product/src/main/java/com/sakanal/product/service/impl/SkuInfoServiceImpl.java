package com.sakanal.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.product.dao.SkuInfoDao;
import com.sakanal.product.entity.SkuInfoEntity;
import com.sakanal.product.service.SkuInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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



}
