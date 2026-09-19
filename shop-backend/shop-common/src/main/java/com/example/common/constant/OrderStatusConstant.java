package com.example.common.constant;

/**
 * 订单状态常量
 * <p>
 * 状态值对应 orders 表 status 字段：
 * 0 待付款，1 已付款，2 已发货，3 已完成，4 已取消
 */
public final class OrderStatusConstant {

    private OrderStatusConstant() {}

    /** 待付款 */
    public static final Integer WAIT_PAY = 0;
    /** 已付款 */
    public static final Integer PAID = 1;
    /** 已发货 */
    public static final Integer SHIPPED = 2;
    /** 已完成 */
    public static final Integer DONE = 3;
    /** 已取消 */
    public static final Integer CANCELLED = 4;
}
