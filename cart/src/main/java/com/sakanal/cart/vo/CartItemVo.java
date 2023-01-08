package com.sakanal.cart.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author yaoxinjia
 */
@Data
public class CartItemVo {

    private Long skuId;
    private Boolean check = true;
    private String title;
    private String image;
    /**
     * 商品套餐属性
     */
    private List<String> skuAttrValues;
    private BigDecimal price;
    private Integer count;
    @Getter(AccessLevel.NONE)
    private BigDecimal totalPrice;

    /**
     * 计算当前购物项总价
     */
    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal("" + this.count));
    }

}
