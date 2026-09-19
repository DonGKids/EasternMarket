package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 活动（活动中心展示的活动项，一个活动可关联多个优惠券模板）
 * <p>
 * category: 1 常驻，2 限时
 * status:   0 未开始，1 进行中，2 已结束
 * rules:    活动规则，JSON 数组字符串，如 ["规则1","规则2"]
 */
@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 活动名称 */
    private String name;

    /** 分类：1 常驻，2 限时 */
    private Integer category;

    /** 活动描述 */
    private String description;

    /** 倒计时文案（如 "2天 05:30:12"），为空则不显示倒计时 */
    private String countdownText;

    /** 是否紧急（即将结束）：0 否，1 是 */
    private Integer urgent;

    /** 活动规则（JSON 数组字符串） */
    private String rules;

    /** 状态：0 未开始，1 进行中，2 已结束 */
    private Integer status;

    /** 排序（越小越靠前） */
    private Integer sort;

    /** 活动开始时间（常驻活动为 NULL） */
    private LocalDateTime startTime;

    /** 活动结束时间（常驻活动为 NULL） */
    private LocalDateTime endTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCategory() { return category; }
    public void setCategory(Integer category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCountdownText() { return countdownText; }
    public void setCountdownText(String countdownText) { this.countdownText = countdownText; }

    public Integer getUrgent() { return urgent; }
    public void setUrgent(Integer urgent) { this.urgent = urgent; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}