package com.example.feign;

import com.example.common.result.Result;
import com.example.entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 调用 shop-product 服务的 Feign 客户端
 * 用于查询购物车中商品的快照信息（名称、图片、价格、库存等）
 */
@FeignClient(name = "shop-product", path = "/shop/product")
public interface ProductFeignClient {

    /**
     * 商品详情
     */
    @GetMapping("/{id}")
    Result<Product> detail(@PathVariable("id") Long id);
}
