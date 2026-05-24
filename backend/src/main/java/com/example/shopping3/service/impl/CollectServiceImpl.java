package com.example.shopping3.service.impl;

import com.example.shopping3.entity.Collect;
import com.example.shopping3.mapper.CollectMapper;
import com.example.shopping3.service.CollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CollectServiceImpl implements CollectService {

    @Autowired
    private CollectMapper collectMapper;

    @Override
    public Collect addCollect(Integer userId, Integer goodsId) {
        Collect existing = collectMapper.selectByUserAndGoods(userId, goodsId);
        if (existing != null) {
            return existing;
        }
        Collect collect = new Collect();
        collect.setUserId(userId);
        collect.setGoodsId(goodsId);
        collectMapper.insert(collect);
        return collect;
    }

    @Override
    public void removeCollect(Integer userId, Integer goodsId) {
        collectMapper.deleteByUserAndGoods(userId, goodsId);
    }

    @Override
    public List<Map<String, Object>> getUserCollects(Integer userId) {
        return collectMapper.selectByUserId(userId);
    }

    @Override
    public boolean isCollected(Integer userId, Integer goodsId) {
        return collectMapper.selectByUserAndGoods(userId, goodsId) != null;
    }
}