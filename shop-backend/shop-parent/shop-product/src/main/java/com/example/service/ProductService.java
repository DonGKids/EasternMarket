package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.common.result.Result;
import com.example.entity.Product;
import com.example.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品 Service（运营端 CRUD + 用户端查询 + 库存管理）
 */
@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 新增商品
     */
    public Result<Void> add(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            return Result.fail("商品名称不能为空");
        }
        if (product.getPrice() == null) {
            return Result.fail("商品价格不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        if (product.getStock() == null) {
            product.setStock(0);
        }
        if (product.getSales() == null) {
            product.setSales(0);
        }
        if (product.getStatus() == null) {
            product.setStatus(1); // 默认上架
        }
        if (product.getIsFlash() == null) {
            product.setIsFlash(0);
        }
        product.setCreateTime(now);
        product.setUpdateTime(now);
        productMapper.insert(product);

        return Result.ok("新增成功");
    }

    /**
     * 修改商品
     */
    public Result<Void> update(Product product) {
        if (product.getId() == null) {
            return Result.fail("id 不能为空");
        }
        Product exist = productMapper.selectById(product.getId());
        if (exist == null) {
            return Result.fail("商品不存在");
        }

        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        return Result.ok("修改成功");
    }

    /**
     * 删除商品
     */
    public Result<Void> delete(Long id) {
        Product exist = productMapper.selectById(id);
        if (exist == null) {
            return Result.fail("商品不存在");
        }
        productMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    /**
     * 商品详情
     */
    public Result<Product> detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        return Result.ok("ok", product);
    }

    /**
     * 运营端列表（全部状态）
     */
    public Result<List<Product>> list() {
        List<Product> list = productMapper.selectList(
                new QueryWrapper<Product>().orderByDesc("create_time"));
        return Result.ok("ok", list);
    }

    /**
     * 用户端：上架商品列表（仅 status=1），支持按分类筛选和关键词搜索
     */
    public Result<List<Product>> listOnSale(Long categoryId, String keyword) {
        QueryWrapper<Product> wrapper = new QueryWrapper<Product>()
                .eq("status", 1)
                .orderByDesc("is_flash")
                .orderByDesc("sales");
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like("name", keyword).or().like("description", keyword));
        }
        List<Product> list = productMapper.selectList(wrapper);
        return Result.ok("ok", list);
    }

    /**
     * 用户端：限时商品列表
     */
    public Result<List<Product>> listFlash() {
        List<Product> list = productMapper.selectList(
                new QueryWrapper<Product>()
                        .eq("status", 1)
                        .eq("is_flash", 1)
                        .orderByDesc("sales"));
        return Result.ok("ok", list);
    }

    /**
     * 上下架切换（运营端）
     */
    public Result<Void> toggleStatus(Long id) {
        Product exist = productMapper.selectById(id);
        if (exist == null) {
            return Result.fail("商品不存在");
        }
        int newStatus = exist.getStatus() == 1 ? 0 : 1;
        int affected = productMapper.update(null,
                new UpdateWrapper<Product>()
                        .eq("id", id)
                        .set("status", newStatus)
                        .set("update_time", LocalDateTime.now()));
        if (affected == 0) {
            return Result.fail("操作失败，商品状态已变更");
        }
        return Result.ok(newStatus == 1 ? "已上架" : "已下架");
    }

    /**
     * 扣减库存（下单时调用，乐观锁）
     */
    public Result<Void> decrStock(Long productId) {
        int affected = productMapper.decrStock(productId);
        if (affected == 0) {
            return Result.fail("库存不足");
        }
        return Result.ok("扣减成功");
    }

    /**
     * 恢复库存（取消订单时调用）
     */
    public Result<Void> incrStock(Long productId) {
        int affected = productMapper.incrStock(productId);
        if (affected == 0) {
            return Result.fail("恢复库存失败");
        }
        return Result.ok("恢复成功");
    }
}
