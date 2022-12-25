package com.sakanal.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakanal.common.constant.WareConstant;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.ware.dao.PurchaseDao;
import com.sakanal.ware.entity.PurchaseDetailEntity;
import com.sakanal.ware.entity.PurchaseEntity;
import com.sakanal.ware.service.PurchaseDetailService;
import com.sakanal.ware.service.PurchaseService;
import com.sakanal.ware.service.WareSkuService;
import com.sakanal.ware.vo.MergeVo;
import com.sakanal.ware.vo.PurchaseDoneVo;
import com.sakanal.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;
    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }
    @Override
    public PageUtils queryPageUnReceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);

    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId(); // 采购单id
        // 如果没有传递采购单id，则新建一个采购单
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();

            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity); // 保存采购单记录
            purchaseId = purchaseEntity.getId(); // 获取保存后生成的id

        }

        //TODO 确认采购单status是0，1才可以合并

        // 获取所有采购需求的id
        List<Long> items = mergeVo.getItems();
        // 局部内部类里面使用外部变量的时候，这个变量需要是final类型的或者是没有被修改过值的变量，但是purchaseId判空时有可能会修改，所以另外赋值给finalPurchaseId
        Long finalPurchaseId = purchaseId;
        // 根据id更新所有的采购需求记录的purchase_id和状态
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();

            purchaseDetailEntity.setId(i);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);
    }

    @Override
    public void received(List<Long> ids) {
        // 1、确认当前采购单是否是新建或者是已分配状态
        // 1.1、根据id查询出purchaseEntity
        List<PurchaseEntity> purchaseEntities = ids.stream().map(this::getById).filter(purchaseEntity -> {
            // 1.2、过滤掉状态不是0和1的purchaseEntity
            return purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode();
        }).peek(purchaseEntity -> {
            // 设置purchaseEntity状态为已领取，并更新时间
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            purchaseEntity.setUpdateTime(new Date());
        }).collect(Collectors.toList());

        // 2、改变采购单的状态
        this.updateBatchById(purchaseEntities);

        // 3、改变采购需求的状态
        purchaseEntities.forEach(purchaseEntity -> {
            // 3.1、查询到对应的所有采购需求
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listDetailByPurChaseId(purchaseEntity.getId());
            // 3.2、修改所有采购需求的状态
            purchaseDetailEntities.forEach(purchaseDetailEntity -> {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
            });
            purchaseDetailService.updateBatchById(purchaseDetailEntities);
        });

    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {
        // 1、改变采购项的状态
        AtomicBoolean flag = new AtomicBoolean(true);
        List<PurchaseItemDoneVo> purchaseItemDoneVos = doneVo.getItems(); // 获取采购需求信息反馈

        // 将purchaseItemDoneVos转变成purchaseDetailEntities
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseItemDoneVos.stream().map(item -> {
            // 根据id查询出采购需求记录实体类
            PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(item.getItemId());
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag.set(false);
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode());
            } else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                // 2、将成功采购的进行入库
                wareSkuService.addStock(purchaseDetailEntity.getSkuId(), purchaseDetailEntity.getWareId(), purchaseDetailEntity.getSkuNum());

            }
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        // 更新采购需求信息
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        // 2、改变采购单状态
        PurchaseEntity purchaseEntity = this.getById(doneVo.getId());
        purchaseEntity.setStatus(flag.get() ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }


}
