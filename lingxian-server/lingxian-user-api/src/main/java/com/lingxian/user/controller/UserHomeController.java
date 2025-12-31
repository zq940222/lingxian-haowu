package com.lingxian.user.controller;

import com.lingxian.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户端-首页", description = "首页相关接口")
public class UserHomeController {

    @GetMapping("/home")
    @Operation(summary = "获取首页数据")
    public Result<Map<String, Object>> getHomeData() {
        Map<String, Object> data = new HashMap<>();

        // 轮播图 (临时mock数据)
        List<Map<String, Object>> banners = new ArrayList<>();
        Map<String, Object> banner1 = new HashMap<>();
        banner1.put("id", 1);
        banner1.put("image", "https://via.placeholder.com/750x300");
        banner1.put("link", "");
        banners.add(banner1);
        data.put("banners", banners);

        // 分类
        List<Map<String, Object>> categories = new ArrayList<>();
        String[] categoryNames = {"蔬菜", "水果", "肉类", "海鲜", "蛋奶", "粮油", "零食", "饮料"};
        for (int i = 0; i < categoryNames.length; i++) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("id", i + 1);
            cat.put("name", categoryNames[i]);
            cat.put("icon", "https://via.placeholder.com/80x80");
            categories.add(cat);
        }
        data.put("categories", categories);

        // 推荐商品
        List<Map<String, Object>> products = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", i);
            product.put("name", "商品" + i);
            product.put("price", 9.9 + i);
            product.put("originalPrice", 19.9 + i);
            product.put("mainImage", "https://via.placeholder.com/200x200");
            product.put("sales", 100 + i * 10);
            products.add(product);
        }
        data.put("recommendProducts", products);

        return Result.success(data);
    }

    @GetMapping("/banners")
    @Operation(summary = "获取轮播图")
    public Result<List<Map<String, Object>>> getBanners() {
        List<Map<String, Object>> banners = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> banner = new HashMap<>();
            banner.put("id", i);
            banner.put("image", "https://via.placeholder.com/750x300");
            banner.put("link", "");
            banners.add(banner);
        }
        return Result.success(banners);
    }
}
