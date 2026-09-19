package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.common.result.Result;
import com.example.entity.Cart;
import com.example.entity.Product;
import com.example.feign.ProductFeignClient;
import com.example.mapper.CartMapper;
import com.example.vo.CartItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车 Service
 *
 * 关键设计：
 * 1. 购物车仅存储 user_id + product_id + quantity，商品快照通过 Feign 实时拉取，
 *    避免商品价格/上下架变更后购物车显示陈旧信息。
 * 2. 加入购物车使用 user_id + product_id 唯一约束：已存在则累加数量，不存在则新增。
 * 3. 商品已下架或删除时，list 接口会跳过该商品（不报错，避免阻塞用户）。
 * 4. quantity 下限为 1，updateQty 接口允许直接设置绝对值，便于前端 +/− 按钮统一处理。
 */
@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 加入购物车（已存在则累加数量）
     */
    public Result<Void> add(Long userId, Long productId, Integer quantity) {
        if (userId == null || productId == null) {
            return Result.fail("参数不能为空");
        }
        if (quantity == null || quantity < 1) {
            quantity = 1;
        }

        // 校验商品是否存在
        Result<Product> productResult = productFeignClient.detail(productId);
        if (productResult == null || !productResult.isSuccess() || productResult.getData() == null) {
            return Result.fail("商品不存在");
        }

        // 查询是否已在购物车
        Cart exist = cartMapper.selectOne(
                new QueryWrapper<Cart>()
                        .eq("user_id", userId)
                        .eq("product_id", productId));
        LocalDateTime now = LocalDateTime.now();
        if (exist != null) {
            // 累加数量
            cartMapper.update(null,
                    new UpdateWrapper<Cart>()
                            .eq("id", exist.getId())
                            .set("quantity", exist.getQuantity() + quantity)
                            .set("update_time", now));
            return Result.ok("已加入购物车");
        }

        // 新增条目
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setCreateTime(now);
        cart.setUpdateTime(now);
        cartMapper.insert(cart);
        return Result.ok("已加入购物车");
    }

    /**
     * 修改数量（直接设置绝对值）
     * quantity 必须 >= 1，若需删除请调用 delete 接口
     */
    public Result<Void> updateQty(Long userId, Long productId, Integer quantity) {
        if (userId == null || productId == null) {
            return Result.fail("参数不能为空");
        }
        if (quantity == null || quantity < 1) {
            return Result.fail("数量必须大于 0");
        }

        int affected = cartMapper.update(null,
                new UpdateWrapper<Cart>()
                        .eq("user_id", userId)
                        .eq("product_id", productId)
                        .set("quantity", quantity)
                        .set("update_time", LocalDateTime.now()));
        if (affected == 0) {
            return Result.fail("购物车中无此商品");
        }
        return Result.ok("ok");
    }

    /**
     * 删除单个购物车条目（按商品 ID）
     */
    public Result<Void> delete(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return Result.fail("参数不能为空");
        }
        cartMapper.delete(
                new QueryWrapper<Cart>()
                        .eq("user_id", userId)
                        .eq("product_id", productId));
        return Result.ok("ok");
    }

    /**
     * 批量删除（下单成功后清空已下单商品）
     */
    public Result<Void> deleteBatch(Long userId, List<Long> productIds) {
        if (userId == null || productIds == null || productIds.isEmpty()) {
            return Result.ok("ok");
        }
        cartMapper.delete(
                new QueryWrapper<Cart>()
                        .eq("user_id", userId)
                        .in("product_id", productIds));
        return Result.ok("ok");
    }

    /**
     * 清空购物车
     */
    public Result<Void> clear(Long userId) {
        if (userId == null) {
            return Result.fail("参数不能为空");
        }
        cartMapper.delete(
                new QueryWrapper<Cart>().eq("user_id", userId));
        return Result.ok("ok");
    }

    /**
     * 查询购物车列表（含商品快照）
     * 商品已下架或删除的条目会被过滤，但不影响其他条目展示
     */
    public Result<List<CartItemVO>> list(Long userId) {
        if (userId == null) {
            return Result.fail("参数不能为空");
        }
        List<Cart> carts = cartMapper.selectList(
                new QueryWrapper<Cart>()
                        .eq("user_id", userId)
                        .orderByDesc("update_time"));

        List<CartItemVO> result = new ArrayList<>();
        for (Cart c : carts) {
            try {
                Result<Product> pr = productFeignClient.detail(c.getProductId());
                if (pr == null || !pr.isSuccess() || pr.getData() == null) {
                    // 商品不存在，跳过（保留 DB 条目，避免误删）
                    continue;
                }
                Product p = pr.getData();
                // 已下架的也不展示
                if (p.getStatus() != null && p.getStatus() == 0) {
                    continue;
                }
                result.add(toVO(c, p));
            } catch (Exception e) {
                // Feign 调用失败时跳过该条，保证整体列表可用
                continue;
            }
        }
        return Result.ok("ok", result);
    }

    private CartItemVO toVO(Cart c, Product p) {
        CartItemVO vo = new CartItemVO();
        vo.setId(c.getId());
        vo.setProductId(c.getProductId());
        vo.setQuantity(c.getQuantity());
        vo.setName(p.getName());
        vo.setDesc(p.getDescription());
        vo.setPrice(p.getPrice());
        vo.setOriginalPrice(p.getOriginalPrice());
        vo.setBadge(p.getBadge());
        vo.setIsFlash(p.getIsFlash());
        vo.setImage(p.getImageUrl());
        vo.setStock(p.getStock());
        vo.setSales(p.getSales());
        return vo;
    }
}
