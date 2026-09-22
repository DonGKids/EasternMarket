package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
    private String description;

    /** 现价 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 角标文字（如"热销"、"新品"、"8折"），为空则不显示 */
    private String badge;

    /** 是否限时商品：0 否，1 是 */
    private Integer isFlash;

    /** 限时活动开始时间（非限时商品为 null） */
    private LocalDateTime flashStartTime;

    /** 限时活动结束时间（非限时商品为 null） */
    private LocalDateTime flashEndTime;

    /** 商品图片路径（如 /products/1.jpg） */
    private String imageUrl;

    /** 分类 ID */
    private Long categoryId;

    /** 库存数量 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** 状态：0 下架，1 上架 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public Integer getIsFlash() { return isFlash; }
    public void setIsFlash(Integer isFlash) { this.isFlash = isFlash; }

    public LocalDateTime getFlashStartTime() { return flashStartTime; }
    public void setFlashStartTime(LocalDateTime flashStartTime) { this.flashStartTime = flashStartTime; }

    public LocalDateTime getFlashEndTime() { return flashEndTime; }
    public void setFlashEndTime(LocalDateTime flashEndTime) { this.flashEndTime = flashEndTime; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}