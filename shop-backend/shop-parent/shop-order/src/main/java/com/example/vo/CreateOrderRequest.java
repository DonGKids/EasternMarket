package com.example.vo;

import java.util.List;

/**
 * 下单请求
 */
public class CreateOrderRequest {

    /** 用户 ID */
    private Long userId;

    /** 购买商品列表 */
    private List<OrderLine> items;

    /** 使用的用户券 ID（user_coupon.id），为空表示不使用优惠券 */
    private Long userCouponId;

    /** 收货人 */
    private String consignee;

    /** 联系电话 */
    private String phone;

    /** 收货地址 */
    private String address;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<OrderLine> getItems() { return items; }
    public void setItems(List<OrderLine> items) { this.items = items; }

    public Long getUserCouponId() { return userCouponId; }
    public void setUserCouponId(Long userCouponId) { this.userCouponId = userCouponId; }

    public String getConsignee() { return consignee; }
    public void setConsignee(String consignee) { this.consignee = consignee; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    /**
     * 下单明细行
     */
    public static class OrderLine {
        /** 商品 ID */
        private Long productId;
        /** 购买数量 */
        private Integer quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
