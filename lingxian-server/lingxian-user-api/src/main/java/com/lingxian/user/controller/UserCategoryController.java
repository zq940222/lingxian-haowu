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
@RequestMapping("/user/categories")
@RequiredArgsConstructor
@Tag(name = "用户端-分类", description = "分类相关接口")
public class UserCategoryController {

    @GetMapping
    @Operation(summary = "获取分类列表")
    public Result<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();
        String[] names = {"蔬菜", "水果", "肉类", "海鲜", "蛋奶", "粮油", "零食", "饮料"};

        for (int i = 0; i < names.length; i++) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("id", i + 1);
            cat.put("name", names[i]);
            cat.put("icon", "https://via.placeholder.com/80x80");
            cat.put("sort", i);
            categories.add(cat);
        }

        return Result.success(categories);
    }

    @GetMapping("/{categoryId}/products")
    @Operation(summary = "获取分类下商品")
    public Result<Map<String, Object>> getCategoryProducts(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> products = new ArrayList<>();

        for (int i = 1; i <= pageSize; i++) {
            int id = (page - 1) * pageSize + i;
            Map<String, Object> product = new HashMap<>();
            product.put("id", id);
            product.put("name", "分类" + categoryId + "-商品" + id);
            product.put("price", 9.9 + id % 10);
            product.put("originalPrice", 19.9 + id % 10);
            product.put("mainImage", "https://via.placeholder.com/200x200");
            product.put("sales", 100 + id * 10);
            products.add(product);
        }

        result.put("list", products);
        result.put("total", 30);
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }
}
