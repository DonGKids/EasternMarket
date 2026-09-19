package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.TemplateCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 优惠券模板 Mapper
 * 乐观锁扣减库存：received_count < total_count 才允许 +1
 */
@Mapper
public interface CouponTemplateMapper extends BaseMapper<TemplateCoupon> {

    /**
     * 原子扣减库存（total_count=0 表示不限量，直接 +1）
     * 返回受影响行数：1 成功，0 失败（已领完）
     */
    @Update("UPDATE template_coupon SET received_count = received_count + 1 " +
            "WHERE id = #{templateId} AND (total_count = 0 OR received_count < total_count)")
    int incrReceived(@Param("templateId") Long templateId);
}
