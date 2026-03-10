package com.example.shopping3.service.impl;

import com.example.shopping3.entity.Goods;
import com.example.shopping3.mapper.GoodsMapper;
import com.example.shopping3.service.GoodsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class GoodsServiceImpl implements GoodsService {

    // 添加日志记录器
    private static final Logger logger = LoggerFactory.getLogger(GoodsServiceImpl.class);

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_GUESS_LIKE = "goods:guessLike";
    private static final String CACHE_KEY_SECOND_HAND = "goods:secondHand";
    private static final String CACHE_KEY_CATEGORY = "goods:category:";
    private static final long CACHE_EXPIRE_TIME = 10;

    @Override
    public List<Goods> getGuessYouLike() {
        // 1. 先尝试查 Redis 缓存
        List<Goods> goodsList = getFromCache(CACHE_KEY_GUESS_LIKE);

        // 2. 缓存有数据，直接返回
        if (goodsList != null && !goodsList.isEmpty()) {
            logger.info("缓存命中：{}", CACHE_KEY_GUESS_LIKE);
            return goodsList;
        }

        // 3. 缓存没有或 Redis 不可用，查数据库
        logger.info("缓存未命中，查询数据库：{}", CACHE_KEY_GUESS_LIKE);
        goodsList = goodsMapper.selectGuessYouLike();

        // 4. 写入 Redis 缓存（如果 Redis 可用）
        saveToCache(CACHE_KEY_GUESS_LIKE, goodsList);

        return goodsList;
    }

    @Override
    public List<Goods> getSecondHandGoods() {
        // 1. 先尝试查 Redis 缓存
        List<Goods> goodsList = getFromCache(CACHE_KEY_SECOND_HAND);

        if (goodsList != null && !goodsList.isEmpty()) {
            logger.info("缓存命中：{}", CACHE_KEY_SECOND_HAND);
            return goodsList;
        }

        // 2. 缓存没有或 Redis 不可用，查数据库
        logger.info("缓存未命中，查询数据库：{}", CACHE_KEY_SECOND_HAND);
        goodsList = goodsMapper.selectSecondHandGoods();

        // 3. 写入 Redis 缓存（如果 Redis 可用）
        saveToCache(CACHE_KEY_SECOND_HAND, goodsList);

        return goodsList;
    }

    @Override
    public List<Goods> getGoodsByCategory(String category) {
        String cacheKey = CACHE_KEY_CATEGORY + category;

        // 1. 先尝试查 Redis 缓存
        List<Goods> goodsList = getFromCache(cacheKey);

        if (goodsList != null && !goodsList.isEmpty()) {
            logger.info("缓存命中：{}", cacheKey);
            return goodsList;
        }

        // 2. 缓存没有或 Redis 不可用，查数据库
        logger.info("缓存未命中，查询数据库：{}", cacheKey);
        goodsList = goodsMapper.selectGoodsByCategory(category);

        // 3. 写入 Redis 缓存（如果 Redis 可用）
        saveToCache(cacheKey, goodsList);

        return goodsList;
    }

    /**
     * 从缓存获取数据（带异常处理）
     * @param key 缓存键
     * @return 缓存数据，如果 Redis 不可用则返回 null
     */
    @SuppressWarnings("unchecked")
    private List<Goods> getFromCache(String key) {
        try {
            return (List<Goods>) redisTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException e) {
            // Redis 不可用，记录警告日志，返回 null 让程序查数据库
            logger.warn("Redis 连接失败，降级查询数据库。key: {}", key, e);
            return null;
        } catch (Exception e) {
            // 其他 Redis 相关异常也捕获
            logger.warn("Redis 操作异常，降级查询数据库。key: {}", key, e);
            return null;
        }
    }

    /**
     * 保存数据到缓存（带异常处理）
     * @param key 缓存键
     * @param value 缓存值
     */
    private void saveToCache(String key, List<Goods> value) {
        if (value == null || value.isEmpty()) {
            return; // 空数据不缓存
        }

        try {
            redisTemplate.opsForValue().set(key, value, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            logger.debug("缓存写入成功：{}", key);
        } catch (RedisConnectionFailureException e) {
            // Redis 不可用，记录警告日志，但不影响主流程
            logger.warn("Redis 连接失败，缓存写入跳过。key: {}", key, e);
        } catch (Exception e) {
            logger.warn("Redis 操作异常，缓存写入跳过。key: {}", key, e);
        }
    }
}
