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

    // 原有列表缓存 Key
    private static final String CACHE_KEY_GUESS_LIKE = "goods:guessLike";
    private static final String CACHE_KEY_SECOND_HAND = "goods:secondHand";
    private static final String CACHE_KEY_CATEGORY = "goods:category:";

    // 新增：详情页缓存 Key 前缀
    private static final String CACHE_KEY_DETAIL = "goods:detail:";

    // 缓存过期时间 (分钟)
    private static final long CACHE_EXPIRE_TIME_LIST = 10; // 列表缓存时间短一些
    private static final long CACHE_EXPIRE_TIME_DETAIL = 30; // 详情缓存时间长一些，减少数据库压力

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

    @Override
    public Goods getGoodsDetail(Integer id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CACHE_KEY_DETAIL + id;

        // 1. 先尝试查 Redis 缓存 (单个对象)
        Goods goods = getSingleFromCache(cacheKey);

        if (goods != null) {
            logger.info("缓存命中详情：{}", cacheKey);
            return goods;
        }

        // 2. 缓存没有或 Redis 不可用，查数据库
        logger.info("缓存未命中，查询数据库详情：{}", cacheKey);
        goods = goodsMapper.selectById(id);

        // 3. 写入 Redis 缓存（如果数据存在且 Redis 可用）
        if (goods != null) {
            saveSingleToCache(cacheKey, goods);

            // 可选：在此处异步调用 mapper 增加浏览量 read_count
            // goodsMapper.incrementReadCount(id);
            // 注意：如果更新了数据库字段，严格来说应该删除或更新该商品的缓存，
            // 但为了性能，通常允许短时间内数据不一致，等待缓存过期自动刷新。
        }

        return goods;
    }

    /**
     * 从缓存获取列表数据（带异常处理）- 原有逻辑
     */
    @SuppressWarnings("unchecked")
    private List<Goods> getFromCache(String key) {
        try {
            return (List<Goods>) redisTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis 连接失败，降级查询数据库。key: {}", key, e);
            return null;
        } catch (Exception e) {
            logger.warn("Redis 操作异常，降级查询数据库。key: {}", key, e);
            return null;
        }
    }

    /**
     * 保存列表数据到缓存（带异常处理）- 原有逻辑
     */
    private void saveToCache(String key, List<Goods> value) {
        if (value == null || value.isEmpty()) {
            return; // 空数据不缓存
        }

        try {
            redisTemplate.opsForValue().set(key, value, CACHE_EXPIRE_TIME_LIST, TimeUnit.MINUTES);
            logger.debug("缓存写入成功：{}", key);
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis 连接失败，缓存写入跳过。key: {}", key, e);
        } catch (Exception e) {
            logger.warn("Redis 操作异常，缓存写入跳过。key: {}", key, e);
        }
    }

    /**
     * 【新增】从缓存获取单个对象数据（带异常处理）
     */
    @SuppressWarnings("unchecked")
    private Goods getSingleFromCache(String key) {
        try {
            return (Goods) redisTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis 连接失败，降级查询数据库详情。key: {}", key, e);
            return null;
        } catch (Exception e) {
            logger.warn("Redis 操作异常，降级查询数据库详情。key: {}", key, e);
            return null;
        }
    }

    /**
     * 【新增】保存单个对象数据到缓存（带异常处理）
     */
    private void saveSingleToCache(String key, Goods value) {
        if (value == null) {
            return; // 空数据不缓存
        }

        try {
            // 详情页面缓存时间稍长，减少频繁访问数据库
            redisTemplate.opsForValue().set(key, value, CACHE_EXPIRE_TIME_DETAIL, TimeUnit.MINUTES);
            logger.debug("详情缓存写入成功：{}", key);
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis 连接失败，详情缓存写入跳过。key: {}", key, e);
        } catch (Exception e) {
            logger.warn("Redis 操作异常，详情缓存写入跳过。key: {}", key, e);
        }
    }
}