package com.example.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券展示对象
 * 在 UserCoupon 基础上平铺模板信息 + 后端算好的优惠金额，供前端下单选券使用
 */
public class UserCouponVO {

    /** user_coupon.id */
    private Long id;

    private Long templateId;

    /** 券名称 */
    private String name;

    /** 类型：1 满减，2 折扣，3 立减 */
    private Integer type;

    /** 面值/立减金额 */
    private BigDecimal faceValue;

    /** 折扣率（0.85 表示 85 折） */
    private BigDecimal discount;

    /** 使用门槛 */
    private BigDecimal threshold;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 后端按订单金额算好的优惠金额（前端直接展示用） */
    private BigDecimal discountAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public BigDecimal getFaceValue() { return faceValue; }
    public void setFaceValue(BigDecimal faceValue) { this.faceValue = faceValue; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }

    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
}
