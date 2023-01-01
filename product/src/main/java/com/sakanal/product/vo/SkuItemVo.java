package com.sakanal.product.vo;

import com.sakanal.product.entity.SkuImagesEntity;
import com.sakanal.product.entity.SkuInfoEntity;
import com.sakanal.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuItemVo {
    //1、sku基本信息获取 pms_sku_info
    SkuInfoEntity info;

    //库存
    boolean hasStock = true;

    //2、sku图片信息    pms_sku_images
    List<SkuImagesEntity> images;

    //3、获取spu的销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;

    //4、获取spu的介绍
    SpuInfoDescEntity desc;

    //5、获取spu的规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

    //6、秒杀商品的优惠信息
    private SecKillSkuVo secKillSkuVo;

    @Data
    public static class SkuItemSaleAttrVo{
        private Long attrId;
        private String attrName;
        private List<AttrValueWithSkuIdVo> attrValues;
    }
    @Data
    public static class AttrValueWithSkuIdVo{
        private String attrValue;
        private String skuIds;
    }
    @Data
    public static class SpuItemAttrGroupVo{
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }
    @Data
    public static class SpuBaseAttrVo{
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
    @Data
    public static class SecKillSkuVo{
        /**
         * 活动id
         */
        private Long promotionId;
        /**
         * 活动场次id
         */
        private Long promotionSessionId;
        /**
         * 商品id
         */
        private Long skuId;
        /**
         * 秒杀价格
         */
        private BigDecimal secKillPrice;
        /**
         * 秒杀总量
         */
        private Integer secKillCount;
        /**
         * 每人限购数量
         */
        private Integer secKillLimit;
        /**
         * 排序
         */
        private Integer secKillSort;

        //当前商品秒杀的开始时间
        private Long startTime;

        //当前商品秒杀的结束时间
        private Long endTime;

        //当前商品秒杀的随机码
        private String randomCode;

    }
}
