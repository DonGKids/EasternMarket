package com.example.vo;

import java.math.BigDecimal;

/**
 * 商品展示对象
 * 在 Product 基础上补充分类名称，供前端列表展示使用
 */
public class ProductVO {

    /** 商品 ID */
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
    private String description;

    /** 现价 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 角标文字 */
    private String badge;

    /** 是否限时商品：0 否，1 是 */
    private Integer isFlash;

    /** 商品图片路径 */
    private String imageUrl;

    /** 分类名称（后端关联查询后填充） */
    private String categoryName;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

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

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }
}
