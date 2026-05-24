package com.example.shopping3.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class StockRedisUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;  

    private static final String STOCK_KEY_PREFIX = "goods:stock:";

    /**
     * 扣减库存 (Lua 脚本保证原子性)
     */
    public boolean decreaseStock(Integer goodsId, Integer buyCount) {
        String key = STOCK_KEY_PREFIX + goodsId;

        // ✅ 增强版脚本：更严格的 nil 和类型检查
        String script =
                "local stock = redis.call('GET', KEYS[1]); " +
                        "if not stock then return 0; end; " +
                        "if stock == false then return 0; end; " +
                        "if stock == '' then return 0; end; " +
                        "local stock_num = tonumber(stock); " +
                        "if not stock_num then return 0; end; " +
                        "if stock_num >= tonumber(ARGV[1]) then " +
                        "   redis.call('DECRBY', KEYS[1], ARGV[1]); " +
                        "   return 1; " +
                        "else " +
                        "   return 0; " +
                        "end";

        try {
            System.out.println(">>> [DEBUG] 准备执行库存扣减，Key: " + key);

            // ✅ 先手动检查 Key 是否存在，帮助调试
            Boolean exists = redisTemplate.hasKey(key);
            System.out.println(">>> [DEBUG] Key 是否存在: " + exists);
            if (exists) {
                String rawValue = redisTemplate.opsForValue().get(key);
                System.out.println(">>> [DEBUG] Key 的原始值: " + rawValue);
            }

            // ✅ 使用 DefaultRedisScript + StringRedisTemplate
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);

            Long result = redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(key),
                    String.valueOf(buyCount)
            );

            boolean success = (result != null && result == 1);
            System.out.println(">>> [DEBUG] 库存扣减结果: " + success + ", Redis 返回: " + result);
            return success;

        } catch (Exception e) {
            System.err.println(">>> [ERROR] Lua 脚本执行异常: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println(">>> [CAUSE] " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("Redis 扣减库存脚本执行失败", e);
        }
    }

    /**
     * 获取库存
     */
    public Integer getStock(Integer goodsId) {
        String key = STOCK_KEY_PREFIX + goodsId;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Integer.parseInt(value);
    }

    /**
     * 设置/更新库存 (用于启动预热)
     */
    public void setStock(Integer goodsId, Integer stock) {
        String key = STOCK_KEY_PREFIX + goodsId;
        // ✅ 强制存储为字符串
        redisTemplate.opsForValue().set(key, String.valueOf(stock));
        System.out.println("[Redis] 设置库存: " + key + " = " + stock + " (存储为字符串)");
    }

    /**
     * 回滚库存 (加回去)
     */
    public void increaseStock(Integer goodsId, Integer count) {
        String key = STOCK_KEY_PREFIX + goodsId;
        redisTemplate.opsForValue().increment(key, count);
    }
}