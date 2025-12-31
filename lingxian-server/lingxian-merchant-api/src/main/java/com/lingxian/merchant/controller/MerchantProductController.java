package com.lingxian.merchant.controller;

import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/merchant/products")
@RequiredArgsConstructor
@Tag(name = "商户端-商品管理", description = "商户商品管理接口")
public class MerchantProductController {

    @GetMapping
    @Operation(summary = "获取商品列表")
    public Result<PageResult<Map<String, Object>>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        List<Map<String, Object>> products = new ArrayList<>();

        String[] names = {"新鲜蔬菜套餐", "精选水果拼盘", "有机鸡蛋", "进口牛肉", "海鲜礼盒",
                         "农家土鸡", "有机牛奶", "手工水饺", "现磨豆浆", "五谷杂粮"};
        String[] categories = {"蔬菜", "水果", "蛋奶", "肉类", "海鲜", "禽类", "蛋奶", "面点", "饮品", "粮油"};

        for (int i = 0; i < pageSize && i < names.length; i++) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", (page - 1) * pageSize + i + 1);
            product.put("name", names[i]);
            product.put("mainImage", "https://via.placeholder.com/200x200");
            product.put("categoryId", i + 1);
            product.put("categoryName", categories[i]);
            product.put("price", new BigDecimal("28.00").add(new BigDecimal(i * 5)));
            product.put("originalPrice", new BigDecimal("38.00").add(new BigDecimal(i * 5)));
            product.put("stock", 100 - i * 8);
            product.put("sales", 50 + i * 20);
            product.put("status", i % 5 == 0 ? 0 : 1); // 0下架 1上架
            product.put("unit", i % 2 == 0 ? "份" : "kg");
            products.add(product);
        }

        PageResult<Map<String, Object>> pageResult = PageResult.of(30L, (long) page, (long) pageSize, products);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        Map<String, Object> product = new HashMap<>();
        product.put("id", id);
        product.put("name", "新鲜蔬菜套餐");
        product.put("mainImage", "https://via.placeholder.com/400x400");
        product.put("images", Arrays.asList(
            "https://via.placeholder.com/400x400",
            "https://via.placeholder.com/400x400",
            "https://via.placeholder.com/400x400"
        ));
        product.put("categoryId", 1);
        product.put("categoryName", "蔬菜");
        product.put("price", new BigDecimal("28.00"));
        product.put("originalPrice", new BigDecimal("38.00"));
        product.put("stock", 100);
        product.put("sales", 156);
        product.put("status", 1);
        product.put("unit", "份");
        product.put("description", "精选当季新鲜蔬菜，包含多种时令蔬菜，营养均衡，新鲜配送到家");
        product.put("detail", "<p>产地：山东寿光</p><p>保质期：3天</p><p>储存方式：冷藏保存</p>");
        return Result.success(product);
    }

    @PostMapping
    @Operation(summary = "添加商品")
    public Result<Long> addProduct(@RequestBody Map<String, Object> body) {
        log.info("添加商品: {}", body);
        return Result.success(System.currentTimeMillis());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品")
    public Result<Void> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        log.info("更新商品: {}, {}", id, body);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        log.info("删除商品: {}", id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新商品状态")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        log.info("更新商品状态: {} -> {}", id, status);
        return Result.success();
    }

    @PutMapping("/{id}/stock")
    @Operation(summary = "更新商品库存")
    public Result<Void> updateProductStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer stock = body.get("stock");
        log.info("更新商品库存: {} -> {}", id, stock);
        return Result.success();
    }
}
