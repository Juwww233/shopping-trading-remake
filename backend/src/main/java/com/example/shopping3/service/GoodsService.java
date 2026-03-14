package com.example.shopping3.service;

import com.example.shopping3.entity.Goods;
import java.util.List;

public interface GoodsService {
    List<Goods> getGuessYouLike();
    List<Goods> getSecondHandGoods();
    // 新增：按分类查询商品
    List<Goods> getGoodsByCategory(String category);

    Goods getGoodsDetail(Integer id);

}