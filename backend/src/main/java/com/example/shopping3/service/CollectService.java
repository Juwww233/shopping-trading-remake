package com.example.shopping3.service;

import com.example.shopping3.entity.Collect;
import java.util.List;
import java.util.Map;

public interface CollectService {
    Collect addCollect(Integer userId, Integer goodsId);
    void removeCollect(Integer userId, Integer goodsId);
    List<Map<String, Object>> getUserCollects(Integer userId);
    boolean isCollected(Integer userId, Integer goodsId);
}