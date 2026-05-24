package com.example.shopping3.mapper;

import com.example.shopping3.entity.Collect;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface CollectMapper {
    int insert(Collect collect);
    int deleteByUserAndGoods(Integer userId, Integer goodsId);
    List<Map<String, Object>> selectByUserId(Integer userId);
    Collect selectByUserAndGoods(Integer userId, Integer goodsId);
}