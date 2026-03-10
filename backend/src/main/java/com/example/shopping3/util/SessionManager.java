package com.example.shopping3.util;

import com.example.shopping3.entity.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理器：存储sessionId和用户的关联（开发环境用内存Map，生产环境建议用Redis）
 */
public class SessionManager {
    // 线程安全的Map，key=sessionId，value=用户信息
    private static final Map<String, User> SESSION_MAP = new ConcurrentHashMap<>();

    // 存储sessionId和用户
    public static void setSession(String sessionId, User user) {
        SESSION_MAP.put(sessionId, user);
    }

    // 根据sessionId获取用户
    public static User getUserBySessionId(String sessionId) {
        return SESSION_MAP.get(sessionId);
    }

    // 移除session
    public static void removeSession(String sessionId) {
        SESSION_MAP.remove(sessionId);
    }

    // 检查sessionId是否有效
    public static boolean isSessionValid(String sessionId) {
        return sessionId != null && SESSION_MAP.containsKey(sessionId);
    }
}