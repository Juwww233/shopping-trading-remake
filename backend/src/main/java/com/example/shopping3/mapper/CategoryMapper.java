package com.example.shopping3.mapper;

import com.example.shopping3.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CategoryMapper {
    List<Category> selectAll();
    Category selectById(Integer id);
    Category selectByName(String name);
    int insert(Category category);
    int update(Category category);
    int delete(Integer id);
}