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
@RequestMapping("/user/merchants")
@RequiredArgsConstructor
@Tag(name = "用户端-商户", description = "商户相关接口")
public class UserMerchantController {

    private final MerchantService merchantService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping("/{id}")
    @Operation(summary = "获取商户详情")
    public Result<Map<String, Object>> getMerchantDetail(@PathVariable Long id) {
        log.info("获取商户详情: id={}", id);

        Merchant merchant = merchantService.getById(id);
        if (merchant == null) {
            return Result.failed("商户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", merchant.getId());
        result.put("name", merchant.getName());
        result.put("logo", imageUrlUtil.generateUrl(merchant.getLogo()));
        result.put("banner", imageUrlUtil.generateUrl(merchant.getBanner()));
        result.put("description", merchant.getDescription());
        result.put("contactPhone", merchant.getContactPhone());
        result.put("province", merchant.getProvince());
        result.put("city", merchant.getCity());
        result.put("district", merchant.getDistrict());
        result.put("address", merchant.getAddress());
        result.put("longitude", merchant.getLongitude());
        result.put("latitude", merchant.getLatitude());
        result.put("rating", merchant.getRating());
        result.put("status", merchant.getStatus());
        result.put("statusText", merchant.getStatus() == 1 ? "营业中" : "休息中");

        // 获取商户的商品分类
        List<Long> categoryIds = productService.list(new LambdaQueryWrapper<Product>()
                        .eq(Product::getMerchantId, id)
                        .eq(Product::getStatus, 1)
                        .select(Product::getCategoryId))
                .stream()
                .map(Product::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        List<Map<String, Object>> categories = new ArrayList<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categoryList = categoryService.listByIds(categoryIds);
            categories = categoryList.stream().map(cat -> {
                Map<String, Object> catMap = new HashMap<>();
                catMap.put("id", cat.getId());
                catMap.put("name", cat.getName());
                return catMap;
            }).collect(Collectors.toList());
        }
        result.put("categories", categories);

        return Result.success(result);
    }

    @GetMapping("/{id}/products")
    @Operation(summary = "获取商户商品列表")
    public Result<Map<String, Object>> getMerchantProducts(
            @PathVariable Long id,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取商户商品列表: merchantId={}, categoryId={}, page={}, pageSize={}", id, categoryId, page, pageSize);

        // 检查商户是否存在
        Merchant merchant = merchantService.getById(id);
        if (merchant == null) {
            return Result.failed("商户不存在");
        }

        // 检查商户是否营业中
        if (merchant.getStatus() != 1) {
            Map<String, Object> result = new HashMap<>();
            result.put("records", new ArrayList<>());
            result.put("total", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("merchantStatus", merchant.getStatus());
            result.put("message", "商户已休息");
            return Result.success(result);
        }

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, id)
                .eq(Product::getStatus, 1);

        if (categoryId != null) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }

        queryWrapper.orderByDesc(Product::getSort)
                .orderByDesc(Product::getCreateTime);

        Page<Product> pageResult = productService.page(new Page<>(page, pageSize), queryWrapper);

        // 处理商品图片URL
        pageResult.getRecords().forEach(this::processProductImageUrls);

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    @GetMapping("/nearby")
    @Operation(summary = "获取附近商户")
    public Result<Map<String, Object>> getNearbyMerchants(
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double latitude,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取附近商户: longitude={}, latitude={}, page={}, pageSize={}", longitude, latitude, page, pageSize);

        // 只查询营业中的商户
        LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getStatus, 1)
                .orderByDesc(Merchant::getRating)
                .orderByDesc(Merchant::getCreateTime);

        Page<Merchant> pageResult = merchantService.page(new Page<>(page, pageSize), queryWrapper);

        // 处理商户图片URL
        List<Map<String, Object>> merchantList = pageResult.getRecords().stream().map(merchant -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", merchant.getId());
            map.put("name", merchant.getName());
            map.put("logo", imageUrlUtil.generateUrl(merchant.getLogo()));
            map.put("description", merchant.getDescription());
            map.put("rating", merchant.getRating());
            map.put("address", merchant.getAddress());
            map.put("province", merchant.getProvince());
            map.put("city", merchant.getCity());
            map.put("district", merchant.getDistrict());
            map.put("status", merchant.getStatus());
            // TODO: 计算距离
            map.put("distance", null);
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", merchantList);
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    /**
     * 处理商品图片URL
     */
    private void processProductImageUrls(Product product) {
        product.setImage(imageUrlUtil.generateUrl(product.getImage()));
        product.setImages(imageUrlUtil.generateUrlsFromJson(product.getImages()));
        product.setVideo(imageUrlUtil.generateUrl(product.getVideo()));
    }
}
