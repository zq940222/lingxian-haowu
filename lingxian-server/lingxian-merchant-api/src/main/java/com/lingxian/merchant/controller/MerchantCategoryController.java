package com.lingxian.merchant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingxian.common.entity.Category;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.CategoryService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/merchant/categories")
@RequiredArgsConstructor
@Tag(name = "商户端-分类管理", description = "商户商品分类管理接口")
public class MerchantCategoryController {

    private final CategoryService categoryService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping
    @Operation(summary = "获取分类列表（扁平结构）")
    public Result<List<Map<String, Object>>> getCategoryList() {
        // 查询所有启用的分类（@TableLogic会自动过滤已删除的）
        List<Category> categories = categoryService.list(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort)
                        .orderByAsc(Category::getId)
        );

        List<Map<String, Object>> result = categories.stream().map(category -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", category.getId());
            map.put("name", category.getName());
            map.put("parentId", category.getParentId());
            map.put("icon", imageUrlUtil.generateUrl(category.getIcon()));
            map.put("image", imageUrlUtil.generateUrl(category.getImage()));
            map.put("level", category.getLevel());
            map.put("sort", category.getSort());
            map.put("status", category.getStatus());
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/tree")
    @Operation(summary = "获取分类树形结构")
    public Result<List<Map<String, Object>>> getCategoryTree() {
        // 查询所有启用的分类
        List<Category> categories = categoryService.list(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort)
                        .orderByAsc(Category::getId)
        );

        // 分离一级和二级分类
        List<Category> parentCategories = categories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());

        Map<Long, List<Category>> childrenMap = categories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .collect(Collectors.groupingBy(Category::getParentId));

        // 构建树形结构
        List<Map<String, Object>> result = parentCategories.stream().map(parent -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", parent.getId());
            map.put("name", parent.getName());
            map.put("icon", imageUrlUtil.generateUrl(parent.getIcon()));
            map.put("image", imageUrlUtil.generateUrl(parent.getImage()));

            // 获取子分类
            List<Category> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
            List<Map<String, Object>> childList = children.stream().map(child -> {
                Map<String, Object> childMap = new HashMap<>();
                childMap.put("id", child.getId());
                childMap.put("name", child.getName());
                childMap.put("icon", imageUrlUtil.generateUrl(child.getIcon()));
                childMap.put("image", imageUrlUtil.generateUrl(child.getImage()));
                return childMap;
            }).collect(Collectors.toList());

            map.put("children", childList);
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @PostMapping
    @Operation(summary = "添加分类")
    public Result<Long> addCategory(@RequestBody Map<String, Object> body) {
        log.info("添加分类: {}", body);
        // 商户端暂不支持添加分类，由管理后台统一管理
        return Result.failed("暂不支持添加分类，请联系管理员");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        log.info("更新分类: {}, {}", id, body);
        // 商户端暂不支持更新分类，由管理后台统一管理
        return Result.failed("暂不支持更新分类，请联系管理员");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        log.info("删除分类: {}", id);
        // 商户端暂不支持删除分类，由管理后台统一管理
        return Result.failed("暂不支持删除分类，请联系管理员");
    }
}
