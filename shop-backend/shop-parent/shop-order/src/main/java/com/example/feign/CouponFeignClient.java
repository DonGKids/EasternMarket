package com.example.feign;

import com.example.common.result.Result;
import com.example.vo.AvailableCouponVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * 调用 shop-coupon 服务的 Feign 客户端
 * 用于下单时校验/计算优惠券金额，支付成功后核销，取消已支付订单时释放
 *
 * 两阶段用券（参考项目约束）：
 * - 下单：仅校验可用性 + 后端计算优惠金额，不修改券状态
 * - 支付成功：调用 use 置为已使用
 * - 取消已付款订单：调用 release 回退为未使用
 */
@FeignClient(name = "shop-coupon", path = "/shop/coupon/user")
public interface CouponFeignClient {

    /**
     * 查询当前可用优惠券（下单时校验 + 取后端算好的优惠金额）
     */
    @GetMapping("/available")
    Result<List<AvailableCouponVO>> available(@RequestParam("userId") Long userId,
                                              @RequestParam("orderAmount") BigDecimal orderAmount);

    /**
     * 核销优惠券（支付成功后调用）
     */
    @PostMapping("/use")
    Result<Void> use(@RequestParam("userCouponId") Long userCouponId,
                    @RequestParam("orderId") String orderId);

    /**
     * 释放优惠券（取消已付款订单时调用）
     */
    @PostMapping("/release")
    Result<Void> release(@RequestParam("userCouponId") Long userCouponId);
}
