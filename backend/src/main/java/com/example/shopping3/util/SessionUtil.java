package com.example.shopping3.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class SessionUtil {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SESSION_PREFIX = "session:";
    private static final long EXPIRE_TIME = 30; // 30分钟过期

    // 保存 Session
    public void setSession(String sessionId, Object user) {
        redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, user, EXPIRE_TIME, TimeUnit.MINUTES);
    }

    // 获取 Session
    public Object getSession(String sessionId) {
        return redisTemplate.opsForValue().get(SESSION_PREFIX + sessionId);
    }

    // 删除 Session (登出)
    public void removeSession(String sessionId) {
        redisTemplate.delete(SESSION_PREFIX + sessionId);
    }

    // 刷新过期时间 (每次请求自动续期)
    public void refreshSession(String sessionId) {
        redisTemplate.expire(SESSION_PREFIX + sessionId, EXPIRE_TIME, TimeUnit.MINUTES);
    }
}