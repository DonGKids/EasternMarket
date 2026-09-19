package com.example.controller;

import com.example.common.result.Result;
import com.example.entity.Category;
import com.example.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类 Controller（运营端 CRUD + 用户端查询）
 */
@RestController
@RequestMapping("/shop/product/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类（运营端）
     */
    @PostMapping
    public Result<Void> add(@RequestBody Category category) {
        return categoryService.add(category);
    }

    /**
     * 修改分类（运营端）
     */
    @PutMapping
    public Result<Void> update(@RequestBody Category category) {
        return categoryService.update(category);
    }

    /**
     * 删除分类（运营端）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return categoryService.delete(id);
    }

    /**
     * 运营端列表（全部状态）
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return categoryService.list();
    }

    /**
     * 用户端：启用分类列表
     */
    @GetMapping("/enabled")
    public Result<List<Category>> listEnabled() {
        return categoryService.listEnabled();
    }

    /**
     * 启用/禁用切换（运营端）
     */
    @PutMapping("/{id}/toggleStatus")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        return categoryService.toggleStatus(id);
    }
}
