package com.example.controller;

import com.example.common.result.Result;
import com.example.service.UserCouponService;
import com.example.vo.UserCouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户优惠券 Controller（用户端：领取、查询、核销、释放）
 */
@RestController
@RequestMapping("/shop/coupon/user")
public class UserCouponController {

    @Autowired
    private UserCouponService userCouponService;

    /**
     * 领取优惠券
     */
    @PostMapping("/receive")
    public Result<Void> receive(@RequestParam Long userId, @RequestParam Long templateId) {
        return userCouponService.receive(userId, templateId);
    }

    /**
     * 我的优惠券列表（按状态筛选）
     * status: null 全部，0 未使用，1 已使用，2 已过期
     * 返回 VO，含模板字段（name/type/faceValue/discount/threshold）
     */
    @GetMapping("/mine")
    public Result<List<UserCouponVO>> mine(@RequestParam Long userId,
                                            @RequestParam(required = false) Integer status) {
        return userCouponService.myCoupons(userId, status);
    }

    /**
     * 查询当前可用的优惠券（下单时调用）
     * 返回 VO，含后端算好的优惠金额 discountAmount
     */
    @GetMapping("/available")
    public Result<List<UserCouponVO>> available(@RequestParam Long userId,
                                                @RequestParam BigDecimal orderAmount) {
        return userCouponService.available(userId, orderAmount);
    }

    /**
     * 核销优惠券（支付成功后调用）
     */
    @PostMapping("/use")
    public Result<Void> use(@RequestParam Long userCouponId, @RequestParam String orderId) {
        return userCouponService.use(userCouponId, orderId);
    }

    /**
     * 释放优惠券（取消订单时调用）
     */
    @PostMapping("/release")
    public Result<Void> release(@RequestParam Long userCouponId) {
        return userCouponService.release(userCouponId);
    }
}
