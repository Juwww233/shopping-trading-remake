package com.example.shopping3.service;

import com.example.shopping3.entity.Goods;
import java.util.List;

public interface GoodsService {
    List<Goods> getGuessYouLike();
    List<Goods> getSecondHandGoods();
    // 新增：按分类查询商品
    List<Goods> getGoodsByCategory(String category);

    Goods getGoodsDetail(Integer id);

    void incrementReadCount(Integer id);

    Goods publishGoods(Goods goods);
    Goods updateGoods(Goods goods);
    void updateGoodsStatus(Integer id, String status);
    void updateGoodsSaleStatus(Integer id, String saleStatus);
    List<Goods> getSellerGoods(Integer userId);
    List<Goods> searchGoods(String keyword);
}