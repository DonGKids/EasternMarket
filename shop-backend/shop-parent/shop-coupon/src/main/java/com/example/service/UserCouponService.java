package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.common.result.Result;
import com.example.entity.TemplateCoupon;
import com.example.entity.UserCoupon;
import com.example.mapper.CouponTemplateMapper;
import com.example.mapper.UserCouponMapper;
import com.example.vo.UserCouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户优惠券 Service（用户端：领取、查询、核销、释放）
 *
 * 关键设计（参考历史经验）：
 * 1. 锁券/用券分阶段：创建订单仅记录关联，支付成功后才置为已使用，取消订单需释放。
 * 2. 金额用 BigDecimal，禁用 double。
 * 3. 状态码：0 未使用，1 已使用，2 已过期。
 */
@Service
public class UserCouponService {

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponTemplateMapper couponTemplateMapper;

    /**
     * 领取优惠券
     * 1. 校验模板状态（进行中 + 在领取时间内）
     * 2. 校验每人限领
     * 3. 乐观锁扣减库存
     * 4. 落库 user_coupon 并计算 expireTime
     */
    @Transactional
    public Result<Void> receive(Long userId, Long templateId) {
        if (userId == null || templateId == null) {
            return Result.fail("参数不能为空");
        }

        TemplateCoupon template = couponTemplateMapper.selectById(templateId);
        if (template == null) {
            return Result.fail("优惠券不存在");
        }
        if (template.getStatus() != 1) {
            return Result.fail("优惠券不在领取期内");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(template.getValidStart()) || now.isAfter(template.getValidEnd())) {
            return Result.fail("优惠券不在领取时间内");
        }

        // 校验每人限领
        Long received = userCouponMapper.selectCount(
                new QueryWrapper<UserCoupon>()
                        .eq("user_id", userId)
                        .eq("template_id", templateId));
        if (template.getPerLimit() != null && template.getPerLimit() > 0
                && received >= template.getPerLimit()) {
            return Result.fail("已超过每人限领数量");
        }

        // 乐观锁扣减库存
        int affected = couponTemplateMapper.incrReceived(templateId);
        if (affected == 0) {
            return Result.fail("优惠券已被领完");
        }

        // 落库用户券
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setTemplateId(templateId);
        userCoupon.setStatus(0);
        userCoupon.setReceiveTime(now);
        userCoupon.setCreateTime(now);
        // 根据模板 validDays 计算过期时间
        if (template.getValidDays() != null && template.getValidDays() > 0) {
            userCoupon.setExpireTime(now.plusDays(template.getValidDays()));
        } else {
            // 未配置有效天数，默认用模板领取结束时间
            userCoupon.setExpireTime(template.getValidEnd());
        }
        userCouponMapper.insert(userCoupon);

        return Result.ok("领取成功");
    }

    /**
     * 我的优惠券列表（按状态筛选）
     * status: null 全部，0 未使用，1 已使用，2 已过期
     * 返回 VO，平铺模板字段（name/type/faceValue/discount/threshold/expireTime）
     */
    public Result<List<UserCouponVO>> myCoupons(Long userId, Integer status) {
        QueryWrapper<UserCoupon> wrapper = new QueryWrapper<UserCoupon>()
                .eq("user_id", userId)
                .orderByDesc("create_time");
        if (status != null) {
            wrapper.eq("status", status);
        }
        List<UserCoupon> list = userCouponMapper.selectList(wrapper);

        List<UserCouponVO> result = list.stream().map(uc -> {
            TemplateCoupon t = couponTemplateMapper.selectById(uc.getTemplateId());
            UserCouponVO vo = new UserCouponVO();
            vo.setId(uc.getId());
            vo.setTemplateId(uc.getTemplateId());
            vo.setExpireTime(uc.getExpireTime());
            if (t != null) {
                vo.setName(t.getName());
                vo.setType(t.getType());
                vo.setFaceValue(t.getFaceValue());
                vo.setDiscount(t.getDiscount());
                vo.setThreshold(t.getThreshold());
            }
            return vo;
        }).toList();

        return Result.ok("ok", result);
    }

    /**
     * 查询当前可用的优惠券（下单时调用）
     * 条件：未使用 + 未过期 + 满足门槛
     * 返回 VO，后端算好 discountAmount，前端直接展示
     *
     * 金额计算规则：
     * 1 满减：满足门槛后减 faceValue
     * 2 折扣：优惠 = orderAmount * (1 - discount)
     * 3 立减：直接减 faceValue（无门槛）
     */
    public Result<List<UserCouponVO>> available(Long userId, BigDecimal orderAmount) {
        LocalDateTime now = LocalDateTime.now();
        List<UserCoupon> list = userCouponMapper.selectList(
                new QueryWrapper<UserCoupon>()
                        .eq("user_id", userId)
                        .eq("status", 0)
                        .gt("expire_time", now)
                        .orderByAsc("expire_time"));

        // 关联模板 + 校验门槛 + 计算优惠金额
        List<UserCouponVO> result = list.stream()
                .map(uc -> {
                    TemplateCoupon t = couponTemplateMapper.selectById(uc.getTemplateId());
                    if (t == null) return null;

                    // 门槛校验
                    if (t.getThreshold() != null && t.getThreshold().compareTo(BigDecimal.ZERO) > 0
                            && orderAmount.compareTo(t.getThreshold()) < 0) {
                        return null;
                    }

                    // 组装 VO
                    UserCouponVO vo = new UserCouponVO();
                    vo.setId(uc.getId());
                    vo.setTemplateId(uc.getTemplateId());
                    vo.setName(t.getName());
                    vo.setType(t.getType());
                    vo.setFaceValue(t.getFaceValue());
                    vo.setDiscount(t.getDiscount());
                    vo.setThreshold(t.getThreshold());
                    vo.setExpireTime(uc.getExpireTime());

                    // 计算优惠金额
                    vo.setDiscountAmount(calcDiscountAmount(t, orderAmount));
                    return vo;
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        return Result.ok("ok", result);
    }

    /**
     * 按券类型计算优惠金额
     * 1 满减：faceValue
     * 2 折扣：orderAmount * (1 - discount)
     * 3 立减：faceValue
     */
    private BigDecimal calcDiscountAmount(TemplateCoupon t, BigDecimal orderAmount) {
        if (t.getType() == null) return BigDecimal.ZERO;
        switch (t.getType()) {
            case 1: // 满减
            case 3: // 立减
                return t.getFaceValue() == null ? BigDecimal.ZERO : t.getFaceValue();
            case 2: // 折扣
                if (t.getDiscount() == null) return BigDecimal.ZERO;
                // 优惠金额 = 订单金额 × (1 - 折扣率)
                return orderAmount.multiply(BigDecimal.ONE.subtract(t.getDiscount()))
                        .setScale(2, RoundingMode.HALF_UP);
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * 核销优惠券（支付成功后调用）
     * 仅允许把「未使用 + 未过期」的券改为已使用
     */
    public Result<Void> use(Long userCouponId, String orderId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null) {
            return Result.fail("优惠券不存在");
        }
        if (uc.getStatus() != 0) {
            return Result.fail("优惠券不可用");
        }
        if (uc.getExpireTime().isBefore(LocalDateTime.now())) {
            // 兜底：已过期但状态未更新
            userCouponMapper.update(null,
                    new UpdateWrapper<UserCoupon>()
                            .eq("id", userCouponId)
                            .set("status", 2));
            return Result.fail("优惠券已过期");
        }

        int affected = userCouponMapper.update(null,
                new UpdateWrapper<UserCoupon>()
                        .eq("id", userCouponId)
                        .eq("status", 0) // 乐观锁：仅未使用才能核销
                        .set("status", 1)
                        .set("used_time", LocalDateTime.now())
                        .set("order_id", orderId));
        if (affected == 0) {
            return Result.fail("核销失败，优惠券可能已被使用");
        }
        return Result.ok("核销成功");
    }

    /**
     * 释放优惠券（取消订单时调用）
     * 仅允许把「已使用」的券回退为未使用
     */
    @Transactional
    public Result<Void> release(Long userCouponId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null) {
            return Result.fail("优惠券不存在");
        }
        if (uc.getStatus() != 1) {
            return Result.fail("优惠券非已使用状态，无需释放");
        }

        int affected = userCouponMapper.update(null,
                new UpdateWrapper<UserCoupon>()
                        .eq("id", userCouponId)
                        .eq("status", 1)
                        .set("status", 0)
                        .set("used_time", null)
                        .set("order_id", null));
        if (affected == 0) {
            return Result.fail("释放失败，优惠券状态已变更");
        }
        return Result.ok("释放成功");
    }

    /**
     * 批量过期处理（定时任务调用）
     * 把未使用但已过期的券置为已过期
     */
    public void expireOverdue() {
        userCouponMapper.update(null,
                new UpdateWrapper<UserCoupon>()
                        .eq("status", 0)
                        .lt("expire_time", LocalDateTime.now())
                        .set("status", 2));
    }
}
