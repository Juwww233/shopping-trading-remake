package com.example.shopping3.mapper;

import com.example.shopping3.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface GoodsMapper {
    List<Goods> selectGuessYouLike();

    List<Goods> selectSecondHandGoods();

    List<Goods> selectGoodsByCategory(String category);

    Goods selectById(Integer id);

    List<Goods> selectAllGoods();

    // 更新商品库存
    void updateStock(Integer goodsId, Integer count);
}