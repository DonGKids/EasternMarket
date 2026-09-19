package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.common.result.Result;
import com.example.entity.Category;
import com.example.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类 Service（运营端 CRUD + 用户端查询）
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增分类
     */
    public Result<Void> add(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return Result.fail("分类名称不能为空");
        }

        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        LocalDateTime now = LocalDateTime.now();
        category.setCreateTime(now);
        category.setUpdateTime(now);
        categoryMapper.insert(category);

        return Result.ok("新增成功");
    }

    /**
     * 修改分类
     */
    public Result<Void> update(Category category) {
        if (category.getId() == null) {
            return Result.fail("id 不能为空");
        }
        Category exist = categoryMapper.selectById(category.getId());
        if (exist == null) {
            return Result.fail("分类不存在");
        }

        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(category);
        return Result.ok("修改成功");
    }

    /**
     * 删除分类
     */
    public Result<Void> delete(Long id) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            return Result.fail("分类不存在");
        }
        categoryMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    /**
     * 运营端列表（全部状态）
     */
    public Result<List<Category>> list() {
        List<Category> list = categoryMapper.selectList(
                new QueryWrapper<Category>().orderByAsc("sort"));
        return Result.ok("ok", list);
    }

    /**
     * 用户端：启用分类列表（仅 status=1）
     */
    public Result<List<Category>> listEnabled() {
        List<Category> list = categoryMapper.selectList(
                new QueryWrapper<Category>()
                        .eq("status", 1)
                        .orderByAsc("sort"));
        return Result.ok("ok", list);
    }

    /**
     * 启用/禁用切换
     */
    public Result<Void> toggleStatus(Long id) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            return Result.fail("分类不存在");
        }
        int newStatus = exist.getStatus() == 1 ? 0 : 1;
        int affected = categoryMapper.update(null,
                new UpdateWrapper<Category>()
                        .eq("id", id)
                        .set("status", newStatus)
                        .set("update_time", LocalDateTime.now()));
        if (affected == 0) {
            return Result.fail("操作失败");
        }
        return Result.ok(newStatus == 1 ? "已启用" : "已禁用");
    }
}
