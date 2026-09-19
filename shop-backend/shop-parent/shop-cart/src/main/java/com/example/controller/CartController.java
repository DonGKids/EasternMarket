package com.example.controller;

import com.example.common.result.Result;
import com.example.service.CartService;
import com.example.vo.CartItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车 Controller
 * 路由前缀 /shop/cart，前端通过 Vite 代理直连本服务（端口 7075）
 *
 * 接口列表：
 *   POST   /shop/cart                  加入购物车（已存在则累加）
 *   PUT    /shop/cart                  修改数量（绝对值）
 *   DELETE /shop/cart                  删除单个商品
 *   POST   /shop/cart/batchDelete      批量删除（下单后清空已下单商品）
 *   POST   /shop/cart/clear            清空购物车
 *   GET    /shop/cart                  查询购物车（含商品快照）
 */
@RestController
@RequestMapping("/shop/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 加入购物车（已存在则累加数量）
     */
    @PostMapping
    public Result<Void> add(@RequestParam Long userId,
                            @RequestParam Long productId,
                            @RequestParam(required = false, defaultValue = "1") Integer quantity) {
        return cartService.add(userId, productId, quantity);
    }

    /**
     * 修改数量（直接设置绝对值，quantity >= 1）
     */
    @PutMapping
    public Result<Void> updateQty(@RequestParam Long userId,
                                  @RequestParam Long productId,
                                  @RequestParam Integer quantity) {
        return cartService.updateQty(userId, productId, quantity);
    }

    /**
     * 删除单个购物车条目
     */
    @DeleteMapping
    public Result<Void> delete(@RequestParam Long userId,
                               @RequestParam Long productId) {
        return cartService.delete(userId, productId);
    }

    /**
     * 批量删除（下单成功后清空已下单商品）
     * 请求体：JSON 数组 [productId1, productId2, ...]
     */
    @PostMapping("/batchDelete")
    public Result<Void> batchDelete(@RequestParam Long userId,
                                    @RequestBody List<Long> productIds) {
        return cartService.deleteBatch(userId, productIds);
    }

    /**
     * 清空购物车
     */
    @PostMapping("/clear")
    public Result<Void> clear(@RequestParam Long userId) {
        return cartService.clear(userId);
    }

    /**
     * 查询购物车列表（含商品快照，已下架/已删除商品自动过滤）
     */
    @GetMapping
    public Result<List<CartItemVO>> list(@RequestParam Long userId) {
        return cartService.list(userId);
    }
}
