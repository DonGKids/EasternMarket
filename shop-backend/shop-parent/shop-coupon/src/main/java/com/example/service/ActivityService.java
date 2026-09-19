package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.result.Result;
import com.example.entity.Activity;
import com.example.entity.TemplateCoupon;
import com.example.mapper.ActivityMapper;
import com.example.mapper.CouponTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动 Service
 * 提供活动列表查询 + 活动关联的优惠券查询
 */
@Service
public class ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private CouponTemplateMapper couponTemplateMapper;

    /**
     * 活动列表（按 sort 排序，仅进行中）
     */
    public Result<List<Activity>> list() {
        List<Activity> list = activityMapper.selectList(
                new QueryWrapper<Activity>()
                        .eq("status", 1)
                        .orderByAsc("sort"));
        return Result.ok("ok", list);
    }

    /**
     * 活动详情
     */
    public Result<Activity> detail(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return Result.fail("活动不存在");
        }
        return Result.ok("ok", activity);
    }

    /**
     * 查询某活动下可领取的优惠券模板
     */
    public Result<List<TemplateCoupon>> couponsByActivity(Long activityId) {
        List<TemplateCoupon> list = couponTemplateMapper.selectList(
                new QueryWrapper<TemplateCoupon>()
                        .eq("activity_id", activityId)
                        .eq("status", 1)
                        .orderByAsc("id"));
        return Result.ok("ok", list);
    }

    /**
     * 活动列表 + 每个活动关联的优惠券（一次性返回，减少前端请求）
     * 返回结构：[{ activity: {...}, coupons: [...] }, ...]
     */
    public Result<List<Map<String, Object>>> listWithCoupons() {
        List<Activity> activities = activityMapper.selectList(
                new QueryWrapper<Activity>()
                        .eq("status", 1)
                        .orderByAsc("sort"));

        List<Map<String, Object>> result = activities.stream().map(a -> {
            List<TemplateCoupon> coupons = couponTemplateMapper.selectList(
                    new QueryWrapper<TemplateCoupon>()
                            .eq("activity_id", a.getId())
                            .eq("status", 1)
                            .orderByAsc("id"));
            Map<String, Object> item = new HashMap<>();
            item.put("activity", a);
            item.put("coupons", coupons);
            return item;
        }).toList();

        return Result.ok("ok", result);
    }
}
