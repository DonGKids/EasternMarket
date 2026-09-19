package com.example.feign;

import com.example.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 调用 shop-coupon 服务的 Feign 客户端
 * 用于注册成功后自动发放新人券等场景
 */
@FeignClient(name = "shop-coupon", path = "/shop/coupon/user")
public interface CouponFeignClient {

    /**
     * 领取优惠券
     */
    @PostMapping("/receive")
    Result<Void> receive(@RequestParam("userId") Long userId,
                         @RequestParam("templateId") Long templateId);
}
