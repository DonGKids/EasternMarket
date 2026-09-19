package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板（规则定义，1 个模板可被多用户领取）
 */
@TableName("template_coupon")
public class TemplateCoupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 券名称 */
    private String name;

    /** 类型：1 满减，2 折扣，3 立减 */
    private Integer type;

    /** 面值/立减金额（满减、立减时使用） */
    private BigDecimal faceValue;

    /** 折扣率（折扣券时使用，如 0.85 表示 85 折） */
    private BigDecimal discount;

    /** 使用门槛（满多少可用，0 表示无门槛） */
    private BigDecimal threshold;

    /** 发行总量，0 表示不限 */
    private Integer totalCount;

    /** 已领取数量 */
    private Integer receivedCount;

    /** 每人限领数量 */
    private Integer perLimit;

    /** 获取方式：1 新人专享，2 限时抢券，3 邀请奖励，4 签到领取 */
    private Integer getWay;

    /** 关联的活动 ID（activity.id），为空表示不属于任何活动 */
    private Long activityId;

    /** 适用范围：1 全场，2 指定分类，3 指定商品 */
    private Integer scopeType;

    /** 关联的分类/商品 ID（scopeType=1 时为空） */
    private Long scopeId;

    /** 领取开始时间 */
    private LocalDateTime validStart;

    /** 领取结束时间 */
    private LocalDateTime validEnd;

    /** 领取后有效天数（领取时据此计算 expireTime） */
    private Integer validDays;

    /** 模板状态：0 未开始，1 进行中，2 已结束 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public Integer getReceivedCount() { return receivedCount; }
    public void setReceivedCount(Integer receivedCount) { this.receivedCount = receivedCount; }

    public Integer getPerLimit() { return perLimit; }
    public void setPerLimit(Integer perLimit) { this.perLimit = perLimit; }

    public Integer getGetWay() { return getWay; }
    public void setGetWay(Integer getWay) { this.getWay = getWay; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public Integer getScopeType() { return scopeType; }
    public void setScopeType(Integer scopeType) { this.scopeType = scopeType; }

    public Long getScopeId() { return scopeId; }
    public void setScopeId(Long scopeId) { this.scopeId = scopeId; }

    public LocalDateTime getValidStart() { return validStart; }
    public void setValidStart(LocalDateTime validStart) { this.validStart = validStart; }

    public LocalDateTime getValidEnd() { return validEnd; }
    public void setValidEnd(LocalDateTime validEnd) { this.validEnd = validEnd; }

    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
