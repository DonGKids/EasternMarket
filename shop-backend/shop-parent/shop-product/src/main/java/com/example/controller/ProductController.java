package com.example.controller;

import com.example.common.result.Result;
import com.example.common.storage.StorageService;
import com.example.entity.Product;
import com.example.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * 商品 Controller（运营端 CRUD + 用户端查询 + 库存管理 + 图片上传）
 */
@RestController
@RequestMapping("/shop/product")
public class ProductController {

    /** 允许上传的商品图片扩展名（小写，不含点） */
    private static final Set<String> ALLOW_IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Autowired
    private ProductService productService;

    @Autowired
    private StorageService storageService;

    /**
     * 新增商品（运营端）
     */
    @PostMapping
    public Result<Void> add(@RequestBody Product product) {
        return productService.add(product);
    }

    /**
     * 修改商品（运营端）
     */
    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        return productService.update(product);
    }

    /**
     * 删除商品（运营端）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return productService.delete(id);
    }

    /**
     * 商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return productService.detail(id);
    }

    /**
     * 运营端列表（全部状态）
     */
    @GetMapping("/list")
    public Result<List<Product>> list() {
        return productService.list();
    }

    /**
     * 用户端：上架商品列表（支持分类筛选 + 关键词搜索）
     */
    @GetMapping("/onSale")
    public Result<List<Product>> listOnSale(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return productService.listOnSale(categoryId, keyword);
    }

    /**
     * 用户端：限时商品列表
     */
    @GetMapping("/flash")
    public Result<List<Product>> listFlash() {
        return productService.listFlash();
    }

    /**
     * 上下架切换（运营端）
     */
    @PutMapping("/{id}/toggleStatus")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        return productService.toggleStatus(id);
    }

    /**
     * 扣减库存（下单时调用）
     */
    @PostMapping("/{id}/decrStock")
    public Result<Void> decrStock(@PathVariable Long id) {
        return productService.decrStock(id);
    }

    /**
     * 恢复库存（取消订单时调用）
     */
    @PostMapping("/{id}/incrStock")
    public Result<Void> incrStock(@PathVariable Long id) {
        return productService.incrStock(id);
    }

    /**
     * 上传商品图片
     * 运营端新增/编辑商品时调用，前端用 multipart/form-data 上传，
     * 返回可直接访问的 URL，由前端回填到商品的 image 字段后提交保存。
     */
    @PostMapping("/uploadImage")
    public Result<Void> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            return Result.fail("文件名无效");
        }

        // 取扩展名并校验类型
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0 && dot < originalName.length() - 1) {
            ext = originalName.substring(dot + 1).toLowerCase();
        }
        if (ext.isEmpty() || !ALLOW_IMAGE_EXT.contains(ext)) {
            return Result.fail("仅支持 jpg/jpeg/png/gif/webp 格式");
        }

        try {
            String url = storageService.upload(file.getBytes(), ext, "product/");
            return Result.<Void>ok("上传成功").with("url", url);
        } catch (Exception e) {
            return Result.fail("上传失败：" + e.getMessage());
        }
    }
}
