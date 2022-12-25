package com.sakanal.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.ware.dao.PurchaseDetailDao;
import com.sakanal.ware.entity.PurchaseDetailEntity;
import com.sakanal.ware.service.PurchaseDetailService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseDetailEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        String key = (String) params.get("key");
        lambdaQueryWrapper.and(StringUtils.hasText(key),queryWrapper -> {
            queryWrapper.eq(PurchaseDetailEntity::getPurchaseId,key)
                    .or().eq(PurchaseDetailEntity::getSkuId,key);
        });

        String status = (String) params.get("status");
        lambdaQueryWrapper.eq(StringUtils.hasText(status),PurchaseDetailEntity::getStatus,status);

        String wareId = (String) params.get("wareId");
        lambdaQueryWrapper.eq(StringUtils.hasText(wareId),PurchaseDetailEntity::getWareId,wareId);

        IPage<PurchaseDetailEntity> page = this.page(new Query<PurchaseDetailEntity>().getPage(params), lambdaQueryWrapper);

        return new PageUtils(page);
    }
    @Override
    public List<PurchaseDetailEntity> listDetailByPurChaseId(Long id) {

        return this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));

    }


}
