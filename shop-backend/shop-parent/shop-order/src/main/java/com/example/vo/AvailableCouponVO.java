package com.example.vo;

import java.math.BigDecimal;

/**
 * 可用优惠券信息（用于反序列化 shop-coupon 返回的 UserCouponVO）
 * 订单模块下单时仅需 id 与后端算好的优惠金额
 */
public class AvailableCouponVO {

    /** user_coupon.id */
    private Long id;

    /** 后端按订单金额算好的优惠金额 */
    private BigDecimal discountAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
}
