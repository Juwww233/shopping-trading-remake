package com.example.shopping3.service;

import com.example.shopping3.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getById(Integer id);
    Category addCategory(Category category);
    Category updateCategory(Category category);
    void deleteCategory(Integer id);
}