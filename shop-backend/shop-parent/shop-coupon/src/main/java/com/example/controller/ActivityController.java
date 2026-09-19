package com.example.controller;

import com.example.common.result.Result;
import com.example.entity.Activity;
import com.example.entity.TemplateCoupon;
import com.example.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 活动 Controller（用户端：活动列表 + 活动关联的优惠券）
 */
@RestController
@RequestMapping("/shop/coupon/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * 活动列表（仅进行中，按 sort 排序）
     */
    @GetMapping("/list")
    public Result<List<Activity>> list() {
        return activityService.list();
    }

    /**
     * 活动详情
     */
    @GetMapping("/{id}")
    public Result<Activity> detail(@PathVariable Long id) {
        return activityService.detail(id);
    }

    /**
     * 某活动下可领取的优惠券
     */
    @GetMapping("/{id}/coupons")
    public Result<List<TemplateCoupon>> couponsByActivity(@PathVariable Long id) {
        return activityService.couponsByActivity(id);
    }

    /**
     * 活动列表 + 每个活动关联的优惠券（一次性返回）
     */
    @GetMapping("/listWithCoupons")
    public Result<List<Map<String, Object>>> listWithCoupons() {
        return activityService.listWithCoupons();
    }
}
