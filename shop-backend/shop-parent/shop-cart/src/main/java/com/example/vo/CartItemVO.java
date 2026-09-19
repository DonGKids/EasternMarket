package com.example.vo;

import java.math.BigDecimal;

/**
 * 购物车条目 VO（含商品快照，供前端直接渲染）
 */
public class CartItemVO {

    /** 购物车条目 ID */
    private Long id;

    /** 商品 ID */
    private Long productId;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
    private String desc;

    /** 商品现价 */
    private BigDecimal price;

    /** 商品原价 */
    private BigDecimal originalPrice;

    /** 角标文字 */
    private String badge;

    /** 是否限时商品：0 否，1 是 */
    private Integer isFlash;

    /** 商品图片 URL */
    private String image;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** 购买数量 */
    private Integer quantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public Integer getIsFlash() { return isFlash; }
    public void setIsFlash(Integer isFlash) { this.isFlash = isFlash; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
