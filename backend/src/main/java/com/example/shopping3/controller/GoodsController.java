package com.example.shopping3.controller;

import com.example.shopping3.common.Result;
import com.example.shopping3.entity.Goods;
import com.example.shopping3.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    // 猜你喜欢
    @GetMapping("/guessYouLike")
    public Result<List<Goods>> getGuessYouLike() {
        try {
            // 核心业务逻辑
            List<Goods> goodsList = goodsService.getGuessYouLike();
            return Result.success(goodsList);
        } catch (Exception e) {
            // 捕获所有异常，返回友好错误信息（包含具体异常原因，便于排查）
            String errorMsg = "获取猜你喜欢商品失败：" + e.getMessage();
            // 打印异常堆栈（后端日志排查用，生产环境建议用日志框架如SLF4J）
            e.printStackTrace();
            return Result.error(errorMsg);
        }
    }

    // 二手专区
    @GetMapping("/secondHand")
    public Result<List<Goods>> getSecondHandGoods() {
        try {
            List<Goods> goodsList = goodsService.getSecondHandGoods();
            return Result.success(goodsList);
        } catch (Exception e) {
            String errorMsg = "获取二手专区商品失败：" + e.getMessage();
            e.printStackTrace();
            return Result.error(errorMsg);
        }
    }

    // 按分类查询商品
    @GetMapping("/category")
    public Result<List<Goods>> getGoodsByCategory(@RequestParam String category) {
        // 1. 先做参数校验（前置拦截，避免无效的数据库查询）
        if (category == null || category.trim().isEmpty()) {
            return Result.error("分类名称不能为空，请选择有效分类！");
        }

        try {
            List<Goods> goodsList = goodsService.getGoodsByCategory(category);
            return Result.success(goodsList);
        } catch (Exception e) {
            String errorMsg = "获取[" + category + "]分类商品失败：" + e.getMessage();
            e.printStackTrace();
            return Result.error(errorMsg);
        }
    }
}
/*package com.example.shopping3.controller;

import com.example.shopping3.common.Result;
import com.example.shopping3.entity.Goods;
import com.example.shopping3.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    // 猜你喜欢
    @GetMapping("/guessYouLike")
    public Result<List<Goods>> getGuessYouLike() {
        List<Goods> goodsList = goodsService.getGuessYouLike();
        return Result.success(goodsList);
    }

    // 二手专区
    @GetMapping("/secondHand")
    public Result<List<Goods>> getSecondHandGoods() {
        List<Goods> goodsList = goodsService.getSecondHandGoods();
        return Result.success(goodsList);
    }

    // 新增：按分类查询商品
    @GetMapping("/category")
    public Result<List<Goods>> getGoodsByCategory(@RequestParam String category) {
        List<Goods> goodsList = goodsService.getGoodsByCategory(category);
        return Result.success(goodsList);
    }
}*/