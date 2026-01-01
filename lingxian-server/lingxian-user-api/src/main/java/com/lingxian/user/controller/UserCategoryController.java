package com.lingxian.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.Category;
import com.lingxian.common.entity.Merchant;
import com.lingxian.common.entity.Product;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.CategoryService;
import com.lingxian.common.service.MerchantService;
import com.lingxian.common.service.ProductService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user/categories")
@RequiredArgsConstructor
@Tag(name = "用户端-分类", description = "分类相关接口")
public class UserCategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final MerchantService merchantService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping
    @Operation(summary = "获取分类列表")
    public Result<List<Category>> getCategories() {
        // 获取分类树形结构
        List<Category> categories = categoryService.getTreeList();
        // 递归处理分类图片URL
        processCategoryImageUrls(categories);
        return Result.success(categories);
    }

    @GetMapping("/{categoryId}/products")
    @Operation(summary = "获取分类下商品")
    public Result<Map<String, Object>> getCategoryProducts(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        // 获取营业中的商户ID列表
        List<Long> openMerchantIds = getOpenMerchantIds();

        // 如果没有营业中的商户，返回空结果
        if (openMerchantIds.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("records", new ArrayList<>());
            result.put("total", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            return Result.success(result);
        }

        Page<Product> pageResult = productService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId, categoryId)
                        .eq(Product::getStatus, 1)
                        .in(Product::getMerchantId, openMerchantIds)
                        .orderByDesc(Product::getSort)
                        .orderByDesc(Product::getCreateTime)
        );

        // 处理商品图片URL
        pageResult.getRecords().forEach(this::processProductImageUrls);

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    /**
     * 递归处理分类图片URL
     */
    private void processCategoryImageUrls(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        for (Category category : categories) {
            category.setIcon(imageUrlUtil.generateUrl(category.getIcon()));
            category.setImage(imageUrlUtil.generateUrl(category.getImage()));
            // 递归处理子分类
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                processCategoryImageUrls(category.getChildren());
            }
        }
    }

    /**
     * 处理商品图片URL
     */
    private void processProductImageUrls(Product product) {
        product.setImage(imageUrlUtil.generateUrl(product.getImage()));
        product.setImages(imageUrlUtil.generateUrlsFromJson(product.getImages()));
        product.setVideo(imageUrlUtil.generateUrl(product.getVideo()));
    }

    /**
     * 获取营业中的商户ID列表
     */
    private List<Long> getOpenMerchantIds() {
        List<Merchant> openMerchants = merchantService.list(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getStatus, 1)
                .select(Merchant::getId));
        return openMerchants.stream()
                .map(Merchant::getId)
                .collect(Collectors.toList());
    }
}
