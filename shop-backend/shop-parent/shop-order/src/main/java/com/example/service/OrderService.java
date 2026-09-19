package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.common.result.Result;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.Product;
import com.example.feign.CouponFeignClient;
import com.example.feign.ProductFeignClient;
import com.example.mapper.OrderItemMapper;
import com.example.mapper.OrderMapper;
import com.example.vo.AvailableCouponVO;
import com.example.vo.CreateOrderRequest;
import com.example.vo.OrderItemVO;
import com.example.vo.OrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单 Service（下单、支付回调、取消、发货、确认收货、查询）
 *
 * 关键设计（参考项目约束）：
 * 1. 优惠券两阶段：下单仅校验可用性 + 后端计算优惠金额，不修改券状态；
 *    支付成功回调才核销（use）；取消已付款订单才释放（release）。
 * 2. 金额用 BigDecimal，禁用 double；优惠金额由后端计算。
 * 3. 库存通过 Feign 调用 shop-product 原子扣减/恢复，失败时手动回滚已扣减库存。
 * 4. 订单状态：0 待付款，1 已付款，2 已发货，3 已完成，4 已取消。
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    // 订单状态常量
    private static final int STATUS_PENDING = 0;    // 待付款
    private static final int STATUS_PAID = 1;       // 已付款
    private static final int STATUS_SHIPPED = 2;    // 已发货
    private static final int STATUS_DONE = 3;       // 已完成
    private static final int STATUS_CANCELLED = 4;  // 已取消

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private CouponFeignClient couponFeignClient;

    /**
     * 下单
     * 1. 校验参数
     * 2. 查商品快照 + 原子扣减库存（失败回滚已扣减）
     * 3. 校验优惠券 + 后端计算优惠金额
     * 4. 落库订单与明细
     */
    @Transactional
    public Result<String> createOrder(CreateOrderRequest req) {
        // 1. 参数校验
        if (req == null) {
            return Result.fail("请求不能为空");
        }
        if (req.getUserId() == null) {
            return Result.fail("用户ID不能为空");
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            return Result.fail("购买商品不能为空");
        }
        for (CreateOrderRequest.OrderLine line : req.getItems()) {
            if (line.getProductId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
                return Result.fail("商品或数量不合法");
            }
        }

        // 2. 查商品快照 + 扣减库存
        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        // 已扣减库存记录：productId -> 已扣减件数，用于失败时按件回滚
        Map<Long, Integer> deductedCount = new LinkedHashMap<>();

        for (CreateOrderRequest.OrderLine line : req.getItems()) {
            Result<Product> pr = productFeignClient.detail(line.getProductId());
            if (pr.getData() == null) {
                rollbackStock(deductedCount);
                return Result.fail("商品不存在: " + line.getProductId());
            }
            Product p = pr.getData();

            // 按购买数量逐件扣减库存（与取消订单时逐件恢复保持一致）
            for (int i = 0; i < line.getQuantity(); i++) {
                Result<Void> dr = productFeignClient.decrStock(line.getProductId());
                if (!dr.isSuccess()) {
                    rollbackStock(deductedCount);
                    return Result.fail("库存不足: " + p.getName());
                }
                deductedCount.merge(line.getProductId(), 1, Integer::sum);
            }

            OrderItem item = new OrderItem();
            item.setProductId(p.getId());
            item.setProductName(p.getName());
            item.setProductImage(p.getImageUrl());
            item.setPrice(p.getPrice());
            item.setQuantity(line.getQuantity());
            item.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
            items.add(item);
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        // 3. 优惠券校验 + 金额计算（后端计算，保证数据一致性）
        BigDecimal discountAmount = BigDecimal.ZERO;
        Long userCouponId = req.getUserCouponId();
        if (userCouponId != null) {
            // 防止同一券被多个未完结订单占用
            Long occupied = orderMapper.selectCount(
                    new QueryWrapper<Order>()
                            .eq("user_coupon_id", userCouponId)
                            .in("status", STATUS_PENDING, STATUS_PAID, STATUS_SHIPPED));
            if (occupied != null && occupied > 0) {
                rollbackStock(deductedCount);
                return Result.fail("该优惠券已被其他订单占用");
            }

            Result<List<AvailableCouponVO>> cr = couponFeignClient.available(req.getUserId(), totalAmount);
            if (!cr.isSuccess() || cr.getData() == null) {
                rollbackStock(deductedCount);
                return Result.fail("优惠券校验失败");
            }
            final Long couponId = userCouponId;
            AvailableCouponVO coupon = cr.getData().stream()
                    .filter(c -> couponId.equals(c.getId()))
                    .findFirst()
                    .orElse(null);
            if (coupon == null) {
                rollbackStock(deductedCount);
                return Result.fail("优惠券不可用或未达到使用门槛");
            }
            discountAmount = coupon.getDiscountAmount() == null
                    ? BigDecimal.ZERO : coupon.getDiscountAmount();
        }
        BigDecimal payAmount = totalAmount.subtract(discountAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        // 4. 落库订单与明细
        String orderNo = "ORD" + System.currentTimeMillis() + (int) (Math.random() * 1000);
        LocalDateTime now = LocalDateTime.now();
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(req.getUserId());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(payAmount);
        order.setUserCouponId(userCouponId);
        order.setStatus(STATUS_PENDING);
        order.setConsignee(req.getConsignee());
        order.setPhone(req.getPhone());
        order.setAddress(req.getAddress());
        order.setCreateTime(now);
        order.setUpdateTime(now);

        try {
            orderMapper.insert(order);
            for (OrderItem item : items) {
                item.setOrderId(order.getId());
                item.setCreateTime(now);
                orderItemMapper.insert(item);
            }
        } catch (Exception e) {
            // 本地落库失败：回滚已扣减库存（本地 DB 由 @Transactional 回滚）
            rollbackStock(deductedCount);
            log.error("订单落库失败，已回滚库存，orderNo={}, err={}", orderNo, e.getMessage());
            throw new RuntimeException("订单创建失败", e);
        }

        return Result.ok("下单成功", orderNo);
    }

    /**
     * 回滚已扣减的库存（按已扣减件数逐件恢复）
     */
    private void rollbackStock(Map<Long, Integer> deductedCount) {
        deductedCount.forEach((pid, cnt) -> {
            for (int i = 0; i < cnt; i++) {
                try {
                    productFeignClient.incrStock(pid);
                } catch (Exception e) {
                    log.warn("回滚库存失败，productId={}, err={}", pid, e.getMessage());
                }
            }
        });
    }

    /**
     * 支付成功回调（支付宝异步通知验签通过后调用）
     * 核销优惠券 + 订单置为已付款
     *
     * TODO 接入支付宝时：
     * 1. 在 /shop/order/pay 生成支付宝 PC 网页支付表单（alipay.trade.page.pay）
     * 2. 在 /shop/order/notify 接收支付宝异步通知，验签后调用本方法
     */
    @Transactional
    public Result<Void> paySuccess(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            return Result.fail("订单号不能为空");
        }
        Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() != STATUS_PENDING) {
            return Result.fail("订单状态不允许支付");
        }

        // 核销优惠券（两阶段：此处才置为已使用）
        if (order.getUserCouponId() != null) {
            Result<Void> ur = couponFeignClient.use(order.getUserCouponId(), orderNo);
            if (!ur.isSuccess()) {
                return Result.fail("优惠券核销失败：" + ur.getMsg());
            }
        }

        int affected = orderMapper.update(null,
                new UpdateWrapper<Order>()
                        .eq("id", order.getId())
                        .eq("status", STATUS_PENDING)
                        .set("status", STATUS_PAID)
                        .set("pay_time", LocalDateTime.now())
                        .set("update_time", LocalDateTime.now()));
        if (affected == 0) {
            return Result.fail("支付失败，订单状态已变更");
        }
        return Result.ok("支付成功");
    }

    /**
     * 取消订单
     * - 待付款：直接取消 + 恢复库存（券未核销，无需处理）
     * - 已付款：恢复库存 + 释放优惠券（退款流程，支付接入后配合退款）
     * - 已发货/已完成：不允许取消
     */
    @Transactional
    public Result<Void> cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() == STATUS_SHIPPED || order.getStatus() == STATUS_DONE) {
            return Result.fail("订单已发货或已完成，无法取消");
        }
        if (order.getStatus() == STATUS_CANCELLED) {
            return Result.fail("订单已取消");
        }

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", orderId));
        for (OrderItem it : items) {
            for (int i = 0; i < it.getQuantity(); i++) {
                try {
                    productFeignClient.incrStock(it.getProductId());
                } catch (Exception e) {
                    log.warn("恢复库存失败，productId={}, err={}", it.getProductId(), e.getMessage());
                }
            }
        }

        // 已付款订单：释放优惠券（退款流程）
        if (order.getStatus() == STATUS_PAID && order.getUserCouponId() != null) {
            try {
                couponFeignClient.release(order.getUserCouponId());
            } catch (Exception e) {
                log.warn("释放优惠券失败，userCouponId={}, err={}", order.getUserCouponId(), e.getMessage());
            }
        }

        int affected = orderMapper.update(null,
                new UpdateWrapper<Order>()
                        .eq("id", orderId)
                        .in("status", STATUS_PENDING, STATUS_PAID)
                        .set("status", STATUS_CANCELLED)
                        .set("cancel_time", LocalDateTime.now())
                        .set("update_time", LocalDateTime.now()));
        if (affected == 0) {
            return Result.fail("取消失败，订单状态已变更");
        }
        return Result.ok("订单已取消");
    }

    /**
     * 发货（运营端）
     */
    public Result<Void> ship(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() != STATUS_PAID) {
            return Result.fail("仅已付款订单可发货");
        }
        int affected = orderMapper.update(null,
                new UpdateWrapper<Order>()
                        .eq("id", orderId)
                        .eq("status", STATUS_PAID)
                        .set("status", STATUS_SHIPPED)
                        .set("ship_time", LocalDateTime.now())
                        .set("update_time", LocalDateTime.now()));
        if (affected == 0) {
            return Result.fail("发货失败，订单状态已变更");
        }
        return Result.ok("已发货");
    }

    /**
     * 确认收货（用户端）
     */
    public Result<Void> confirmReceipt(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() != STATUS_SHIPPED) {
            return Result.fail("仅已发货订单可确认收货");
        }
        int affected = orderMapper.update(null,
                new UpdateWrapper<Order>()
                        .eq("id", orderId)
                        .eq("status", STATUS_SHIPPED)
                        .set("status", STATUS_DONE)
                        .set("finish_time", LocalDateTime.now())
                        .set("update_time", LocalDateTime.now()));
        if (affected == 0) {
            return Result.fail("确认收货失败，订单状态已变更");
        }
        return Result.ok("已完成");
    }

    /**
     * 订单详情（含明细）
     */
    public Result<OrderVO> detail(String orderNo) {
        Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        if (order == null) {
            return Result.fail("订单不存在");
        }
        return Result.ok("ok", toVO(order, loadItems(order.getId())));
    }

    /**
     * 按订单号查询订单实体（支付二维码 / 主动查询交易状态用）
     */
    public Order getOrderByNo(String orderNo) {
        return orderMapper.selectOne(new QueryWrapper<Order>().eq("order_no", orderNo));
    }

    /**
     * 我的订单（按状态筛选）
     */
    public Result<List<OrderVO>> myOrders(Long userId, Integer status) {
        QueryWrapper<Order> wrapper = new QueryWrapper<Order>()
                .eq("user_id", userId)
                .orderByDesc("create_time");
        if (status != null) {
            wrapper.eq("status", status);
        }
        List<Order> orders = orderMapper.selectList(wrapper);
        List<OrderVO> list = orders.stream()
                .map(o -> toVO(o, loadItems(o.getId())))
                .toList();
        return Result.ok("ok", list);
    }

    /**
     * 运营端订单列表（全部）
     */
    public Result<List<OrderVO>> list() {
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>().orderByDesc("create_time"));
        List<OrderVO> list = orders.stream()
                .map(o -> toVO(o, loadItems(o.getId())))
                .toList();
        return Result.ok("ok", list);
    }

    /**
     * 加载订单明细并转 VO
     */
    private List<OrderItemVO> loadItems(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", orderId));
        return items.stream().map(it -> {
            OrderItemVO v = new OrderItemVO();
            v.setId(it.getId());
            v.setProductId(it.getProductId());
            v.setProductName(it.getProductName());
            v.setProductImage(it.getProductImage());
            v.setPrice(it.getPrice());
            v.setQuantity(it.getQuantity());
            v.setSubtotal(it.getSubtotal());
            return v;
        }).toList();
    }

    /**
     * Order -> OrderVO
     */
    private OrderVO toVO(Order o, List<OrderItemVO> items) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setUserId(o.getUserId());
        vo.setTotalAmount(o.getTotalAmount());
        vo.setDiscountAmount(o.getDiscountAmount());
        vo.setPayAmount(o.getPayAmount());
        vo.setUserCouponId(o.getUserCouponId());
        vo.setStatus(o.getStatus());
        vo.setConsignee(o.getConsignee());
        vo.setPhone(o.getPhone());
        vo.setAddress(o.getAddress());
        vo.setPayTime(o.getPayTime());
        vo.setShipTime(o.getShipTime());
        vo.setFinishTime(o.getFinishTime());
        vo.setCancelTime(o.getCancelTime());
        vo.setCreateTime(o.getCreateTime());
        vo.setItems(items);
        return vo;
    }
}
