package com.lingxian.merchant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.Category;
import com.lingxian.common.entity.MerchantUser;
import com.lingxian.common.entity.Product;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.CategoryService;
import com.lingxian.common.service.MerchantUserService;
import com.lingxian.common.service.ProductService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/merchant/products")
@RequiredArgsConstructor
@Tag(name = "商户端-商品管理", description = "商户商品管理接口")
public class MerchantProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final MerchantUserService merchantUserService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping
    @Operation(summary = "获取商品列表")
    public Result<PageResult<Map<String, Object>>> getProductList(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Long merchantId = merchantUser.getMerchantId();

        // 构建查询条件（@TableLogic会自动处理deleted条件）
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getMerchantId, merchantId);

        if (categoryId != null) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            queryWrapper.eq(Product::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Product::getName, keyword);
        }
        queryWrapper.orderByDesc(Product::getSort).orderByDesc(Product::getCreateTime);

        // 分页查询
        Page<Product> pageParam = new Page<>(page, pageSize);
        Page<Product> pageResult = productService.page(pageParam, queryWrapper);

        // 获取所有分类用于映射
        Map<Long, String> categoryMap = categoryService.list().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        // 转换为返回格式
        List<Map<String, Object>> products = pageResult.getRecords().stream().map(product -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", product.getId());
            map.put("name", product.getName());
            map.put("mainImage", imageUrlUtil.generateUrl(product.getImage()));
            map.put("categoryId", product.getCategoryId());
            map.put("categoryName", product.getCategoryId() != null ? categoryMap.get(product.getCategoryId()) : "");
            map.put("price", product.getPrice());
            map.put("originalPrice", product.getOriginalPrice());
            map.put("stock", product.getStock() != null ? product.getStock() : 0);
            map.put("sales", product.getSalesCount() != null ? product.getSalesCount() : 0);
            map.put("status", product.getStatus() != null ? product.getStatus() : 0);
            map.put("unit", product.getUnit() != null ? product.getUnit() : "份");
            return map;
        }).collect(Collectors.toList());

        PageResult<Map<String, Object>> result = PageResult.of(
                pageResult.getTotal(),
                (long) page,
                (long) pageSize,
                products
        );
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Map<String, Object>> getProductDetail(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long id) {

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Product product = productService.getById(id);
        if (product == null) {
            return Result.failed("商品不存在");
        }

        // 验证商品属于当前商户
        if (!product.getMerchantId().equals(merchantUser.getMerchantId())) {
            return Result.failed("无权限查看此商品");
        }

        // 获取分类名称
        String categoryName = "";
        if (product.getCategoryId() != null) {
            Category category = categoryService.getById(product.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", product.getId());
        result.put("name", product.getName());
        result.put("mainImage", imageUrlUtil.generateUrl(product.getImage()));
        result.put("images", imageUrlUtil.parseAndGenerateUrls(product.getImages()));
        result.put("video", imageUrlUtil.generateUrl(product.getVideo()));
        result.put("categoryId", product.getCategoryId());
        result.put("categoryName", categoryName);
        result.put("price", product.getPrice());
        result.put("originalPrice", product.getOriginalPrice());
        result.put("costPrice", product.getCostPrice());
        result.put("stock", product.getStock() != null ? product.getStock() : 0);
        result.put("sales", product.getSalesCount() != null ? product.getSalesCount() : 0);
        result.put("status", product.getStatus() != null ? product.getStatus() : 0);
        result.put("unit", product.getUnit() != null ? product.getUnit() : "份");
        result.put("weight", product.getWeight());
        result.put("description", product.getDescription());
        result.put("detail", product.getDetail());
        result.put("isRecommend", product.getIsRecommend());
        result.put("isNew", product.getIsNew());
        result.put("isHot", product.getIsHot());
        result.put("sort", product.getSort());

        return Result.success(result);
    }

    @PostMapping
    @Operation(summary = "添加商品")
    public Result<Long> addProduct(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {

        log.info("添加商品: userId={}, body={}", userId, body);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Product product = new Product();
        product.setMerchantId(merchantUser.getMerchantId());
        product.setName((String) body.get("name"));
        product.setImage((String) body.get("mainImage"));

        // 处理多图
        Object imagesObj = body.get("images");
        if (imagesObj instanceof List) {
            try {
                product.setImages(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(imagesObj));
            } catch (Exception e) {
                log.warn("序列化图片列表失败", e);
            }
        }

        product.setVideo((String) body.get("video"));
        product.setCategoryId(body.get("categoryId") != null ? Long.valueOf(body.get("categoryId").toString()) : null);
        product.setPrice(body.get("price") != null ? new BigDecimal(body.get("price").toString()) : null);
        product.setOriginalPrice(body.get("originalPrice") != null ? new BigDecimal(body.get("originalPrice").toString()) : null);
        product.setCostPrice(body.get("costPrice") != null ? new BigDecimal(body.get("costPrice").toString()) : null);
        product.setStock(body.get("stock") != null ? Integer.valueOf(body.get("stock").toString()) : 0);
        product.setUnit((String) body.getOrDefault("unit", "份"));
        product.setWeight(body.get("weight") != null ? Integer.valueOf(body.get("weight").toString()) : null);
        product.setDescription((String) body.get("description"));
        product.setDetail((String) body.get("detail"));
        product.setStatus(body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : 1);
        product.setIsRecommend(body.get("isRecommend") != null ? Integer.valueOf(body.get("isRecommend").toString()) : 0);
        product.setIsNew(body.get("isNew") != null ? Integer.valueOf(body.get("isNew").toString()) : 0);
        product.setIsHot(body.get("isHot") != null ? Integer.valueOf(body.get("isHot").toString()) : 0);
        product.setSort(body.get("sort") != null ? Integer.valueOf(body.get("sort").toString()) : 0);

        LocalDateTime now = LocalDateTime.now();
        product.setCreateTime(now);
        product.setUpdateTime(now);
        product.setDeleted(0);

        productService.save(product);
        log.info("商品添加成功: productId={}", product.getId());

        return Result.success(product.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品")
    public Result<Void> updateProduct(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        log.info("更新商品: userId={}, productId={}, body={}", userId, id, body);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Product product = productService.getById(id);
        if (product == null) {
            return Result.failed("商品不存在");
        }

        // 验证商品属于当前商户
        if (!product.getMerchantId().equals(merchantUser.getMerchantId())) {
            return Result.failed("无权限修改此商品");
        }

        // 更新字段
        if (body.containsKey("name")) {
            product.setName((String) body.get("name"));
        }
        if (body.containsKey("mainImage")) {
            product.setImage((String) body.get("mainImage"));
        }
        if (body.containsKey("images")) {
            Object imagesObj = body.get("images");
            if (imagesObj instanceof List) {
                try {
                    product.setImages(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(imagesObj));
                } catch (Exception e) {
                    log.warn("序列化图片列表失败", e);
                }
            }
        }
        if (body.containsKey("video")) {
            product.setVideo((String) body.get("video"));
        }
        if (body.containsKey("categoryId")) {
            product.setCategoryId(body.get("categoryId") != null ? Long.valueOf(body.get("categoryId").toString()) : null);
        }
        if (body.containsKey("price")) {
            product.setPrice(body.get("price") != null ? new BigDecimal(body.get("price").toString()) : null);
        }
        if (body.containsKey("originalPrice")) {
            product.setOriginalPrice(body.get("originalPrice") != null ? new BigDecimal(body.get("originalPrice").toString()) : null);
        }
        if (body.containsKey("costPrice")) {
            product.setCostPrice(body.get("costPrice") != null ? new BigDecimal(body.get("costPrice").toString()) : null);
        }
        if (body.containsKey("stock")) {
            product.setStock(body.get("stock") != null ? Integer.valueOf(body.get("stock").toString()) : 0);
        }
        if (body.containsKey("unit")) {
            product.setUnit((String) body.get("unit"));
        }
        if (body.containsKey("weight")) {
            product.setWeight(body.get("weight") != null ? Integer.valueOf(body.get("weight").toString()) : null);
        }
        if (body.containsKey("description")) {
            product.setDescription((String) body.get("description"));
        }
        if (body.containsKey("detail")) {
            product.setDetail((String) body.get("detail"));
        }
        if (body.containsKey("status")) {
            product.setStatus(body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : 1);
        }
        if (body.containsKey("isRecommend")) {
            product.setIsRecommend(body.get("isRecommend") != null ? Integer.valueOf(body.get("isRecommend").toString()) : 0);
        }
        if (body.containsKey("isNew")) {
            product.setIsNew(body.get("isNew") != null ? Integer.valueOf(body.get("isNew").toString()) : 0);
        }
        if (body.containsKey("isHot")) {
            product.setIsHot(body.get("isHot") != null ? Integer.valueOf(body.get("isHot").toString()) : 0);
        }
        if (body.containsKey("sort")) {
            product.setSort(body.get("sort") != null ? Integer.valueOf(body.get("sort").toString()) : 0);
        }

        product.setUpdateTime(LocalDateTime.now());
        productService.updateById(product);

        log.info("商品更新成功: productId={}", id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<Void> deleteProduct(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long id) {

        log.info("删除商品: userId={}, productId={}", userId, id);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Product product = productService.getById(id);
        if (product == null) {
            return Result.failed("商品不存在");
        }

        // 验证商品属于当前商户
        if (!product.getMerchantId().equals(merchantUser.getMerchantId())) {
            return Result.failed("无权限删除此商品");
        }

        // 软删除（使用MyBatis-Plus的removeById会自动处理逻辑删除）
        productService.removeById(id);

        log.info("商品删除成功: productId={}", id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新商品状态")
    public Result<Void> updateProductStatus(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {

        Integer status = body.get("status");
        log.info("更新商品状态: userId={}, productId={}, status={}", userId, id, status);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Product product = productService.getById(id);
        if (product == null) {
            return Result.failed("商品不存在");
        }

        // 验证商品属于当前商户
        if (!product.getMerchantId().equals(merchantUser.getMerchantId())) {
            return Result.failed("无权限修改此商品");
        }

        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productService.updateById(product);

        log.info("商品状态更新成功: productId={}, status={}", id, status);
        return Result.success();
    }

    @PutMapping("/{id}/stock")
    @Operation(summary = "更新商品库存")
    public Result<Void> updateProductStock(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {

        Integer stock = body.get("stock");
        log.info("更新商品库存: userId={}, productId={}, stock={}", userId, id, stock);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Product product = productService.getById(id);
        if (product == null) {
            return Result.failed("商品不存在");
        }

        // 验证商品属于当前商户
        if (!product.getMerchantId().equals(merchantUser.getMerchantId())) {
            return Result.failed("无权限修改此商品");
        }

        product.setStock(stock);
        product.setUpdateTime(LocalDateTime.now());
        productService.updateById(product);

        log.info("商品库存更新成功: productId={}, stock={}", id, stock);
        return Result.success();
    }

    @GetMapping("/categories")
    @Operation(summary = "获取商品分类列表")
    public Result<List<Map<String, Object>>> getCategories() {
        List<Category> categories = categoryService.list(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort)
        );

        List<Map<String, Object>> result = categories.stream().map(category -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", category.getId());
            map.put("name", category.getName());
            map.put("parentId", category.getParentId());
            map.put("icon", imageUrlUtil.generateUrl(category.getIcon()));
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }
}
