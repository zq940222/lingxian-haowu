package com.lingxian.user.controller;

import com.lingxian.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user/products")
@RequiredArgsConstructor
@Tag(name = "用户端-商品", description = "商品相关接口")
public class UserProductController {

    @GetMapping("/recommend")
    @Operation(summary = "获取推荐商品")
    public Result<Map<String, Object>> getRecommendProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> products = new ArrayList<>();

        // 临时 mock 数据
        for (int i = 1; i <= pageSize; i++) {
            int id = (page - 1) * pageSize + i;
            Map<String, Object> product = new HashMap<>();
            product.put("id", id);
            product.put("name", "推荐商品" + id);
            product.put("subtitle", "新鲜直达，品质保证");
            product.put("price", 9.9 + id % 10);
            product.put("originalPrice", 19.9 + id % 10);
            product.put("mainImage", "https://via.placeholder.com/200x200");
            product.put("sales", 100 + id * 10);
            product.put("stock", 999);
            products.add(product);
        }

        result.put("list", products);
        result.put("total", 100);
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    @GetMapping
    @Operation(summary = "获取商品列表")
    public Result<Map<String, Object>> getProductList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "default") String sortType,
            @RequestParam(defaultValue = "asc") String priceOrder,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> products = new ArrayList<>();

        // 临时 mock 数据
        for (int i = 1; i <= pageSize; i++) {
            int id = (page - 1) * pageSize + i;
            Map<String, Object> product = new HashMap<>();
            product.put("id", id);
            product.put("name", keyword != null ? keyword + "商品" + id : "商品" + id);
            product.put("subtitle", "优质好货");
            product.put("price", 9.9 + id % 20);
            product.put("originalPrice", 19.9 + id % 20);
            product.put("mainImage", "https://via.placeholder.com/200x200");
            product.put("sales", 50 + id * 5);
            product.put("stock", 999);
            products.add(product);
        }

        result.put("list", products);
        result.put("total", 50);
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        Map<String, Object> product = new HashMap<>();
        product.put("id", id);
        product.put("name", "商品" + id);
        product.put("subtitle", "新鲜直达，品质保证");
        product.put("price", 19.9);
        product.put("originalPrice", 29.9);
        product.put("groupPrice", 15.9);
        product.put("groupEnabled", true);
        product.put("mainImage", "https://via.placeholder.com/400x400");
        product.put("images", "[\"https://via.placeholder.com/400x400\",\"https://via.placeholder.com/400x400\"]");
        product.put("description", "<p>商品详情描述</p>");
        product.put("sales", 500);
        product.put("stock", 999);
        product.put("unit", "份");
        product.put("spec", "500g");

        return Result.success(product);
    }
}
