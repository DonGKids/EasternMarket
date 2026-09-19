package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户优惠券 Mapper
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
}
