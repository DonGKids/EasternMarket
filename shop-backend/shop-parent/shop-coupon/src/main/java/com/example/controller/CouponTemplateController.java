package com.example.controller;

import com.example.common.result.Result;
import com.example.entity.TemplateCoupon;
import com.example.service.CouponTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券模板 Controller（运营端：增删改查 + 状态管理）
 */
@RestController
@RequestMapping("/shop/coupon/template")
public class CouponTemplateController {

    @Autowired
    private CouponTemplateService couponTemplateService;

    /**
     * 新增模板
     */
    @PostMapping
    public Result<Void> add(@RequestBody TemplateCoupon template) {
        return couponTemplateService.add(template);
    }

    /**
     * 修改模板
     */
    @PutMapping
    public Result<Void> update(@RequestBody TemplateCoupon template) {
        return couponTemplateService.update(template);
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return couponTemplateService.delete(id);
    }

    /**
     * 模板详情
     */
    @GetMapping("/{id}")
    public Result<TemplateCoupon> detail(@PathVariable Long id) {
        return couponTemplateService.detail(id);
    }

    /**
     * 运营端列表（全部状态）
     */
    @GetMapping("/list")
    public Result<List<TemplateCoupon>> list() {
        return couponTemplateService.list();
    }

    /**
     * 用户端：查询可领取的模板
     * activityCode 传值时按活动过滤；不传时返回全部可领券
     */
    @GetMapping("/receivable")
    public Result<List<TemplateCoupon>> listReceivable(
            @RequestParam(required = false) String activityCode) {
        return couponTemplateService.listReceivable(activityCode);
    }
}
