package com.example.shopping3.service.impl;

import com.example.shopping3.entity.Category;
import com.example.shopping3.mapper.CategoryMapper;
import com.example.shopping3.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private static final String CACHE_KEY = "category:all";
    private static final long CACHE_TTL_MINUTES = 30;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Category> getAllCategories() {
        try {
            @SuppressWarnings("unchecked")
            List<Category> cached = (List<Category>) redisTemplate.opsForValue().get(CACHE_KEY);
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis 连接失败，降级查询数据库");
        }

        List<Category> categories = categoryMapper.selectAll();
        if (categories != null && !categories.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(CACHE_KEY, categories, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                logger.warn("分类缓存写入失败", e);
            }
        }
        return categories;
    }

    @Override
    public Category getById(Integer id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public Category addCategory(Category category) {
        categoryMapper.insert(category);
        redisTemplate.delete(CACHE_KEY);
        return category;
    }

    @Override
    public Category updateCategory(Category category) {
        categoryMapper.update(category);
        redisTemplate.delete(CACHE_KEY);
        return category;
    }

    @Override
    public void deleteCategory(Integer id) {
        categoryMapper.delete(id);
        redisTemplate.delete(CACHE_KEY);
    }
}