package com.example.shopping3.mapper;

import com.example.shopping3.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface GoodsMapper {
    // 猜你喜欢（随机商品）
    List<Goods> selectGuessYouLike();

    // 二手好物（二手物品）
    List<Goods> selectSecondHandGoods();

    // 新增：按分类查询商品
    List<Goods> selectGoodsByCategory(String category);

    Goods selectById(Integer id);
    List<Goods> selectAll();
}