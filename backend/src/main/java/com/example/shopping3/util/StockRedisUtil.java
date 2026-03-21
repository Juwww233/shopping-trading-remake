package com.example.shopping3.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class StockRedisUtil {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String STOCK_KEY_PREFIX = "goods:stock:";

    public void setStock(Integer goodsId, Integer stock) {
        redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + goodsId, stock);
    }

    public Integer getStock(Integer goodsId) {
        Object stockObj = redisTemplate.opsForValue().get(STOCK_KEY_PREFIX + goodsId);
        return stockObj == null ? 0 : Integer.parseInt(stockObj.toString());
    }

    public boolean decreaseStock(Integer goodsId, Integer buyCount) {
        String key = STOCK_KEY_PREFIX + goodsId;
        Long remain = redisTemplate.opsForValue().decrement(key, buyCount);
        if (remain != null && remain < 0) {
            redisTemplate.opsForValue().increment(key, buyCount);
            return false;
        }
        return true;
    }

    // 【新增】增加库存（用于回滚）
    public void increaseStock(Integer goodsId, Integer buyCount) {
        String key = STOCK_KEY_PREFIX + goodsId;
        redisTemplate.opsForValue().increment(key, buyCount);
    }
}