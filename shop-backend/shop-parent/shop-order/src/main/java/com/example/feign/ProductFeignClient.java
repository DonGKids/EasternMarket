package com.example.feign;

import com.example.common.result.Result;
import com.example.entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 调用 shop-product 服务的 Feign 客户端
 * 用于下单时查询商品快照、扣减库存；取消订单时恢复库存
 */
@FeignClient(name = "shop-product", path = "/shop/product")
public interface ProductFeignClient {

    /**
     * 商品详情（下单时取名称/图片/价格快照）
     */
    @GetMapping("/{id}")
    Result<Product> detail(@PathVariable("id") Long id);

    /**
     * 扣减库存（下单时调用，乐观锁）
     */
    @PostMapping("/{id}/decrStock")
    Result<Void> decrStock(@PathVariable("id") Long id);

    /**
     * 恢复库存（取消订单时调用）
     */
    @PostMapping("/{id}/incrStock")
    Result<Void> incrStock(@PathVariable("id") Long id);
}
