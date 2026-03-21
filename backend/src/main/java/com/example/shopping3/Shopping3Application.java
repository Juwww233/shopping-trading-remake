package com.example.shopping3;

import com.example.shopping3.entity.Goods;
import com.example.shopping3.mapper.GoodsMapper;
import com.example.shopping3.util.StockRedisUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@SpringBootApplication
@MapperScan("com.example.shopping3.mapper")
public class Shopping3Application {

    public static void main(String[] args) {
        SpringApplication.run(Shopping3Application.class, args);
    }

    @Component
    static class StockPreheatRunner implements CommandLineRunner {

        @Autowired
        private GoodsMapper goodsMapper;

        @Autowired
        private StockRedisUtil stockRedisUtil;

        @Autowired
        private RedisTemplate<String, Object> redisTemplate;

        @Override
        public void run(String... args) throws Exception {
            // ✅ 使用 RedisCallback 执行 SCRIPT FLUSH
            System.out.println(">>> [系统维护] 正在清除旧的 Lua 脚本缓存...");
            redisTemplate.execute((RedisConnection connection) -> {
                connection.scriptFlush();
                return null;
            });
            System.out.println(">>> [系统维护] 清除完成！");

            System.out.println(">>> 🚀 开始执行库存预热 (Stock Preheat)...");
            try {
                List<Goods> allGoods = goodsMapper.selectAllGoods();

                if (allGoods == null || allGoods.isEmpty()) {
                    System.out.println(">>> ⚠️ 数据库中暂无商品数据，跳过预热。");
                    return;
                }

                int successCount = 0;
                for (Goods goods : allGoods) {
                    if (goods.getId() == null) continue;
                    Integer stock = goods.getStock();
                    if (stock == null) stock = 0;
                    stockRedisUtil.setStock(goods.getId(), stock);
                    successCount++;
                }

                System.out.println(">>> ✅ 库存预热完成！");
                System.out.println("   - 处理商品总数：" + allGoods.size());
                System.out.println("   - 成功同步 Redis：" + successCount);

            } catch (Exception e) {
                System.err.println(">>> ❌ 库存预热失败！");
                e.printStackTrace();
            }
        }
    }
}