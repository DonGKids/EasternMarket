package com.example.controller;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.example.common.constant.OrderStatusConstant;
import com.example.common.result.Result;
import com.example.config.AlipayConfig;
import com.example.entity.Order;
import com.example.service.OrderService;
import com.example.vo.CreateOrderRequest;
import com.example.vo.OrderItemVO;
import com.example.vo.OrderVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单 Controller（下单、支付、取消、发货、确认收货、查询）
 * <p>
 * 支付宝接入：
 * /shop/order/pay    —— 生成 alipay.trade.page.pay 电脑网站支付表单 HTML
 * /shop/order/notify —— 支付宝异步通知验签并更新订单状态
 */
@RestController
@RequestMapping("/shop/order")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConfig alipayConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 下单
     */
    @PostMapping
    public Result<String> create(@RequestBody CreateOrderRequest req) {
        return orderService.createOrder(req);
    }

    /**
     * 支付：生成支付宝电脑网站支付表单（alipay.trade.page.pay）
     * 返回自动提交的 HTML 表单字符串，前端 document.write 后会跳转到支付宝收银台
     */
    @GetMapping("/pay")
    public Result<String> pay(@RequestParam String orderNo) {
        Result<OrderVO> r = orderService.detail(orderNo);
        if (!r.isSuccess() || r.getData() == null) {
            return Result.fail("订单不存在：" + orderNo);
        }
        OrderVO order = r.getData();
        if (order.getStatus() != 0) {
            return Result.fail("订单状态不允许支付");
        }

        // 拼接商品标题
        List<OrderItemVO> items = order.getItems();
        String subject;
        if (items == null || items.isEmpty()) {
            subject = "布卡商城订单 " + orderNo;
        } else if (items.size() == 1) {
            subject = items.get(0).getProductName();
        } else {
            subject = items.get(0).getProductName() + " 等" + items.size() + "件商品";
        }

        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            // TODO 排障：暂时只开同步跳转，notify_url 待收银台稳定后恢复
            // request.setNotifyUrl(alipayConfig.getNotifyUrl());
            request.setReturnUrl(alipayConfig.getReturnUrl());

            // 用 Map + Jackson 构造 bizContent，避免手动转义
            Map<String, Object> biz = new HashMap<>();
            biz.put("out_trade_no", orderNo);
            biz.put("product_code", "FAST_INSTANT_TRADE_PAY");
            biz.put("total_amount", order.getPayAmount().toPlainString());
            // 排障：商品标题用简单英文，避免中文编码触发沙箱网关异常
            biz.put("subject", "BukaShop Order " + orderNo);
            request.setBizContent(objectMapper.writeValueAsString(biz));

            // pageExecute 生成自动提交到支付宝网关的 form 表单 HTML
            String form = alipayClient.pageExecute(request).getBody();
            return Result.ok("支付表单已生成", form);
        } catch (Exception e) {
            log.error("生成支付表单失败，orderNo={}", orderNo, e);
            return Result.fail("生成支付表单失败：" + e.getMessage());
        }
    }

    /**
     * 扫码支付：生成支付宝扫码二维码（alipay.trade.precreate）
     * 不走浏览器表单跳转，Java SDK 直接 JSON 请求沙箱，绕过 502 网关问题
     * 前端用返回的 qr_code 渲染二维码，用户用支付宝 App 扫码付款
     */
    @GetMapping("/pay/qrcode")
    public Result<String> payQrcode(@RequestParam String orderNo) {
        Order order = orderService.getOrderByNo(orderNo);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!OrderStatusConstant.WAIT_PAY.equals(order.getStatus())) {
            return Result.fail("订单状态错误（当前状态：" + order.getStatus() + "）");
        }

        try {
            AlipayTradePrecreateRequest req = new AlipayTradePrecreateRequest();
            req.setNotifyUrl(alipayConfig.getNotifyUrl());

            Map<String, Object> biz = new HashMap<>();
            biz.put("out_trade_no", orderNo);
            biz.put("total_amount", order.getPayAmount().toPlainString());
            biz.put("subject", "BukaShop Order " + orderNo);
            req.setBizContent(objectMapper.writeValueAsString(biz));

            AlipayTradePrecreateResponse resp = alipayClient.execute(req);
            if (resp.isSuccess()) {
                log.info("支付二维码生成成功，orderNo={}, qrCode={}", orderNo, resp.getQrCode());
                return Result.ok("支付二维码已生成", resp.getQrCode());
            } else {
                log.error("沙箱返回生成二维码失败，orderNo={}, code={}, msg={}, subMsg={}",
                    orderNo, resp.getCode(), resp.getMsg(), resp.getSubMsg());
                return Result.fail("生成二维码失败：" + (resp.getSubMsg() != null ? resp.getSubMsg() : resp.getMsg()));
            }
        } catch (Exception e) {
            log.error("生成支付二维码异常，orderNo={}", orderNo, e);
            return Result.fail("生成二维码异常：" + e.getMessage());
        }
    }

    /**
     * 支付成功回调（可选：前端 return_url 跳回后可调用确认）
     * 真实状态变更以支付宝异步通知 /notify 为准
     */
    @PostMapping("/paySuccess")
    public Result<Void> paySuccess(@RequestParam String orderNo) {
        return orderService.paySuccess(orderNo);
    }

    /**
     * 主动查询交易状态（用于异步通知丢失时的兜底：用户点「我已支付」时调用）
     * 直接向沙箱查询 alipay.trade.query，若已交易成功则立即标记订单已付款
     */
    @PostMapping("/pay/check")
    public Result<String> payCheck(@RequestParam String orderNo) {
        Order order = orderService.getOrderByNo(orderNo);
        if (order == null) return Result.fail("订单不存在");
        if (OrderStatusConstant.WAIT_PAY.equals(order.getStatus())) {
            try {
                AlipayTradeQueryRequest req = new AlipayTradeQueryRequest();
                Map<String, Object> biz = new HashMap<>();
                biz.put("out_trade_no", orderNo);
                req.setBizContent(objectMapper.writeValueAsString(biz));
                AlipayTradeQueryResponse resp = alipayClient.execute(req);
                if (resp.isSuccess()) {
                    String status = resp.getTradeStatus();
                    if ("TRADE_SUCCESS".equals(status) || "TRADE_FINISHED".equals(status)) {
                        Result<Void> r = orderService.paySuccess(orderNo);
                        if (r.isSuccess()) return Result.ok("已确认付款，订单状态已更新", status);
                        return Result.fail(r.getMsg());
                    } else if ("WAIT_BUYER_PAY".equals(status)) {
                        return Result.fail("待支付：尚未扫码付款或未支付完成");
                    } else if ("TRADE_CLOSED".equals(status)) {
                        return Result.fail("交易已关闭（二维码超时或订单已取消）");
                    }
                    return Result.fail("交易状态：" + status);
                } else {
                    return Result.fail("查询失败：" + (resp.getSubMsg() != null ? resp.getSubMsg() : resp.getMsg()));
                }
            } catch (Exception e) {
                log.error("主动查询交易异常，orderNo={}", orderNo, e);
                return Result.fail("查询异常：" + e.getMessage());
            }
        } else {
            return Result.ok("订单已处理，无需再次确认", "status-" + order.getStatus());
        }
    }

    /**
     * 支付宝异步通知入口
     * 1. 验签
     * 2. 校验 trade_status == TRADE_SUCCESS
     * 3. 调用 OrderService.paySuccess 更新订单状态（内部乐观锁保证幂等）
     * 4. 返回 "success" 告知支付宝已处理
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        try {
            // 收集支付宝回调的所有参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
                String[] values = entry.getValue();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    sb.append(i == values.length - 1 ? values[i] : values[i] + ",");
                }
                params.put(entry.getKey(), sb.toString());
            }

            // 1. 验签
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params, alipayConfig.getPublicKey(), "UTF-8", alipayConfig.getSignType());
            if (!signVerified) {
                log.warn("支付宝异步通知验签失败");
                return "fail";
            }

            // 2. 校验交易状态
            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                return "success"; // 非支付成功状态，直接返回 success 避免支付宝重发
            }

            // 3. 更新订单状态（paySuccess 内部乐观锁保证幂等，重复通知不会重复核销）
            String outTradeNo = params.get("out_trade_no");
            Result<Void> r = orderService.paySuccess(outTradeNo);
            if (r.isSuccess()) {
                return "success";
            }
            return "fail";
        } catch (Exception e) {
            log.error("处理支付宝异步通知异常", e);
            return "fail";
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancel(@PathVariable Long orderId) {
        return orderService.cancelOrder(orderId);
    }

    /**
     * 发货（运营端）
     */
    @PutMapping("/{orderId}/ship")
    public Result<Void> ship(@PathVariable Long orderId) {
        return orderService.ship(orderId);
    }

    /**
     * 确认收货（用户端）
     */
    @PutMapping("/{orderId}/confirm")
    public Result<Void> confirm(@PathVariable Long orderId) {
        return orderService.confirmReceipt(orderId);
    }

    /**
     * 订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return orderService.detail(orderNo);
    }

    /**
     * 我的订单（按状态筛选）
     * status: null 全部，0 待付款，1 已付款，2 已发货，3 已完成，4 已取消
     */
    @GetMapping("/mine")
    public Result<List<OrderVO>> mine(@RequestParam Long userId,
                                      @RequestParam(required = false) Integer status) {
        return orderService.myOrders(userId, status);
    }

    /**
     * 运营端订单列表（全部）
     */
    @GetMapping("/list")
    public Result<List<OrderVO>> list() {
        return orderService.list();
    }
}
