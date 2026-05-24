package com.example.shopping3.mapper;

import com.example.shopping3.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

    void incrementReadCount(Integer id);

    int insert(Goods goods);
    int update(Goods goods);
    int updateStatus(@Param("id") Integer id, @Param("status") String status);
    int updateSaleStatus(@Param("id") Integer id, @Param("saleStatus") String saleStatus);
    List<Goods> selectByUserId(Integer userId);
    List<Goods> selectByKeyword(String keyword);
}