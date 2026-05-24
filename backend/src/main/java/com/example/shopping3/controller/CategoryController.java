package com.example.shopping3.controller;

import com.example.shopping3.annotation.NoAuth;
import com.example.shopping3.common.Result;
import com.example.shopping3.entity.Category;
import com.example.shopping3.service.CategoryService;
import com.example.shopping3.service.impl.AdminServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AdminServiceImpl adminService;

    @NoAuth
    @GetMapping("/all")
    public Result<List<Category>> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取分类列表失败：" + e.getMessage());
        }
    }

    @NoAuth
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Integer id) {
        try {
            Category category = categoryService.getById(id);
            if (category == null) {
                return Result.error("分类不存在");
            }
            return Result.success(category);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取分类失败：" + e.getMessage());
        }
    }

    private boolean checkAdmin(String sessionId) {
        return adminService.getAdminBySession(sessionId) != null;
    }

    @PostMapping
    public Result<Category> addCategory(@RequestBody Category category,
                                         @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (!checkAdmin(sessionId)) {
            return Result.error("无权操作");
        }
        try {
            Category saved = categoryService.addCategory(category);
            return Result.success(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增分类失败：" + e.getMessage());
        }
    }

    @PutMapping
    public Result<Category> updateCategory(@RequestBody Category category,
                                            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (!checkAdmin(sessionId)) {
            return Result.error("无权操作");
        }
        try {
            Category updated = categoryService.updateCategory(category);
            return Result.success(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新分类失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Integer id,
                                          @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        if (!checkAdmin(sessionId)) {
            return Result.error("无权操作");
        }
        try {
            categoryService.deleteCategory(id);
            return Result.success("删除分类成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除分类失败：" + e.getMessage());
        }
    }
}