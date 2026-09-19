package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.common.result.Result;
import com.example.entity.TemplateCoupon;
import com.example.mapper.CouponTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券模板 Service（运营端：增删改查 + 状态管理）
 */
@Service
public class CouponTemplateService {

    @Autowired
    private CouponTemplateMapper couponTemplateMapper;

    /**
     * 新增模板
     * 默认状态由 validStart 决定：若已到开始时间则置为进行中，否则未开始
     */
    public Result<Void> add(TemplateCoupon template) {
        if (template.getName() == null || template.getName().isBlank()) {
            return Result.fail("券名称不能为空");
        }
        if (template.getType() == null) {
            return Result.fail("券类型不能为空");
        }
        if (template.getValidStart() == null || template.getValidEnd() == null) {
            return Result.fail("领取起止时间不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        if (template.getValidStart().isAfter(now)) {
            template.setStatus(0); // 未开始
        } else if (template.getValidEnd().isBefore(now)) {
            template.setStatus(2); // 已结束
        } else {
            template.setStatus(1); // 进行中
        }

        template.setReceivedCount(0);
        template.setCreateTime(now);
        template.setUpdateTime(now);
        couponTemplateMapper.insert(template);

        return Result.ok("新增成功");
    }

    /**
     * 修改模板（已有人领取时仅允许改名称、说明等非关键字段）
     */
    public Result<Void> update(TemplateCoupon template) {
        if (template.getId() == null) {
            return Result.fail("id 不能为空");
        }
        TemplateCoupon exist = couponTemplateMapper.selectById(template.getId());
        if (exist == null) {
            return Result.fail("模板不存在");
        }
        if (exist.getReceivedCount() > 0) {
            return Result.fail("已有用户领取，不允许修改关键字段");
        }

        template.setUpdateTime(LocalDateTime.now());
        couponTemplateMapper.updateById(template);
        return Result.ok("修改成功");
    }

    /**
     * 删除模板（仅允许删除未被领取的模板）
     */
    public Result<Void> delete(Long id) {
        TemplateCoupon exist = couponTemplateMapper.selectById(id);
        if (exist == null) {
            return Result.fail("模板不存在");
        }
        if (exist.getReceivedCount() > 0) {
            return Result.fail("已有用户领取，不允许删除");
        }
        couponTemplateMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    /**
     * 详情
     */
    public Result<TemplateCoupon> detail(Long id) {
        TemplateCoupon template = couponTemplateMapper.selectById(id);
        if (template == null) {
            return Result.fail("模板不存在");
        }
        return Result.ok("ok", template);
    }

    /**
     * 列表查询（运营端：全部状态）
     */
    public Result<List<TemplateCoupon>> list() {
        List<TemplateCoupon> list = couponTemplateMapper.selectList(
                new QueryWrapper<TemplateCoupon>().orderByDesc("create_time"));
        return Result.ok("ok", list);
    }

    /**
     * 查询可领取的模板（用户端：状态=进行中 且 在领取时间内）
     * activityCode 传值时按活动过滤；传 null 时返回全部可领券
     */
    public Result<List<TemplateCoupon>> listReceivable(String activityCode) {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<TemplateCoupon> wrapper = new QueryWrapper<TemplateCoupon>()
                .eq("status", 1)
                .le("valid_start", now)
                .ge("valid_end", now)
                .orderByDesc("create_time");
        if (activityCode != null && !activityCode.isBlank()) {
            wrapper.eq("activity_code", activityCode);
        }
        List<TemplateCoupon> list = couponTemplateMapper.selectList(wrapper);
        return Result.ok("ok", list);
    }

    /**
     * 刷新模板状态（定时任务调用：未开始→进行中，进行中→已结束）
     */
    public void refreshStatus() {
        LocalDateTime now = LocalDateTime.now();
        // 未开始 -> 进行中
        couponTemplateMapper.update(null,
                new UpdateWrapper<TemplateCoupon>()
                        .eq("status", 0)
                        .le("valid_start", now)
                        .gt("valid_end", now)
                        .set("status", 1)
                        .set("update_time", now));
        // 进行中 -> 已结束
        couponTemplateMapper.update(null,
                new UpdateWrapper<TemplateCoupon>()
                        .eq("status", 1)
                        .lt("valid_end", now)
                        .set("status", 2)
                        .set("update_time", now));
    }
}
