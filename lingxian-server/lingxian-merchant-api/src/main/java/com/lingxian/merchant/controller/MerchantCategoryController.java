package com.lingxian.merchant.controller;

import com.lingxian.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/merchant/categories")
@RequiredArgsConstructor
@Tag(name = "商户端-分类管理", description = "商户商品分类管理接口")
public class MerchantCategoryController {

    @GetMapping
    @Operation(summary = "获取分类列表")
    public Result<List<Map<String, Object>>> getCategoryList() {
        List<Map<String, Object>> categories = new ArrayList<>();

        String[] names = {"蔬菜", "水果", "肉类", "海鲜", "蛋奶", "粮油", "零食", "饮料"};

        for (int i = 0; i < names.length; i++) {
            Map<String, Object> category = new HashMap<>();
            category.put("id", i + 1);
            category.put("name", names[i]);
            category.put("icon", "https://via.placeholder.com/60x60");
            category.put("sort", i + 1);
            category.put("productCount", 10 + i * 3);
            category.put("status", 1);
            categories.add(category);
        }

        return Result.success(categories);
    }

    @PostMapping
    @Operation(summary = "添加分类")
    public Result<Long> addCategory(@RequestBody Map<String, Object> body) {
        log.info("添加分类: {}", body);
        return Result.success(System.currentTimeMillis());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        log.info("更新分类: {}, {}", id, body);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        log.info("删除分类: {}", id);
        return Result.success();
    }
}
