package com.lingxian.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.Category;
import com.lingxian.common.entity.Merchant;
import com.lingxian.common.entity.Product;
import com.lingxian.common.entity.ProductSku;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.CategoryService;
import com.lingxian.common.service.MerchantService;
import com.lingxian.common.service.ProductService;
import com.lingxian.common.service.ProductSkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Tag(name = "管理后台-商品管理", description = "商品管理相关接口")
public class AdminProductController {

    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final CategoryService categoryService;
    private final MerchantService merchantService;

    @GetMapping
    @Operation(summary = "获取商品列表")
    public Result<PageResult<Product>> getProductList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "商品名称") @RequestParam(required = false) String name,
            @Parameter(description = "商户ID") @RequestParam(required = false) Long merchantId,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        log.info("获取商品列表: page={}, size={}, name={}, merchantId={}, categoryId={}, status={}",
                page, size, name, merchantId, categoryId, status);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Product::getName, name);
        }
        if (merchantId != null) {
            wrapper.eq(Product::getMerchantId, merchantId);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> pageResult = productService.page(new Page<>(page, size), wrapper);

        // 填充分类名称和商户名称
        for (Product product : pageResult.getRecords()) {
            if (product.getCategoryId() != null) {
                Category category = categoryService.getById(product.getCategoryId());
                if (category != null) {
                    product.setCategoryName(category.getName());
                }
            }
            if (product.getMerchantId() != null) {
                Merchant merchant = merchantService.getById(product.getMerchantId());
                if (merchant != null) {
                    product.setMerchantName(merchant.getName());
                }
            }
        }

        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        log.info("获取商品详情: id={}", id);

        Product product = productService.getById(id);
        if (product == null) {
            return Result.failed("商品不存在");
        }

        // 填充分类名称
        if (product.getCategoryId() != null) {
            Category category = categoryService.getById(product.getCategoryId());
            if (category != null) {
                product.setCategoryName(category.getName());
            }
        }

        // 填充商户名称
        if (product.getMerchantId() != null) {
            Merchant merchant = merchantService.getById(product.getMerchantId());
            if (merchant != null) {
                product.setMerchantName(merchant.getName());
            }
        }

        // 获取SKU列表
        List<ProductSku> skus = productSkuService.list(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, id)
                        .orderByAsc(ProductSku::getSort)
        );
        product.setSkus(skus);

        return Result.success(product);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新商品状态")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        log.info("更新商品状态: id={}, status={}", id, status);

        Product product = productService.getById(id);
        if (product == null) {
            return Result.failed("商品不存在");
        }

        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productService.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        log.info("删除商品: id={}", id);

        Product product = productService.getById(id);
        if (product == null) {
            return Result.failed("商品不存在");
        }

        // 删除SKU
        productSkuService.remove(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, id));
        // 删除商品
        productService.removeById(id);
        return Result.success();
    }
}
