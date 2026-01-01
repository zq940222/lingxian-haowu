package com.lingxian.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.GroupActivity;
import com.lingxian.common.entity.Product;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.GroupActivityService;
import com.lingxian.common.service.ProductService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user/products")
@RequiredArgsConstructor
@Tag(name = "用户端-商品", description = "商品相关接口")
public class UserProductController {

    private final ProductService productService;
    private final GroupActivityService groupActivityService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping("/recommend")
    @Operation(summary = "获取推荐商品")
    public Result<Map<String, Object>> getRecommendProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<Product> pageResult = productService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .eq(Product::getIsRecommend, 1)
                        .orderByDesc(Product::getSalesCount)
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

    @GetMapping
    @Operation(summary = "获取商品列表")
    public Result<Map<String, Object>> getProductList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "default") String sortType,
            @RequestParam(defaultValue = "asc") String priceOrder,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1);

        // 分类筛选
        if (categoryId != null) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Product::getName, keyword);
        }

        // 排序
        switch (sortType) {
            case "sales":
                queryWrapper.orderByDesc(Product::getSalesCount);
                break;
            case "price":
                if ("desc".equals(priceOrder)) {
                    queryWrapper.orderByDesc(Product::getPrice);
                } else {
                    queryWrapper.orderByAsc(Product::getPrice);
                }
                break;
            case "new":
                queryWrapper.orderByDesc(Product::getCreateTime);
                break;
            default:
                queryWrapper.orderByDesc(Product::getSort).orderByDesc(Product::getCreateTime);
        }

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

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null || product.getStatus() != 1) {
            return Result.failed("商品不存在或已下架");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", product.getId());
        result.put("name", product.getName());
        result.put("description", product.getDescription());
        result.put("price", product.getPrice());
        result.put("originalPrice", product.getOriginalPrice());
        // 处理图片URL
        result.put("mainImage", imageUrlUtil.generateUrl(product.getImage()));
        result.put("image", imageUrlUtil.generateUrl(product.getImage()));
        result.put("images", imageUrlUtil.generateUrlsFromJson(product.getImages()));
        result.put("detail", product.getDetail());
        result.put("sales", product.getSalesCount());
        result.put("salesCount", product.getSalesCount());
        result.put("stock", product.getStock());
        result.put("unit", product.getUnit());
        result.put("categoryId", product.getCategoryId());
        result.put("merchantId", product.getMerchantId());

        // 查询是否有进行中的拼团活动
        LocalDateTime now = LocalDateTime.now();
        GroupActivity groupActivity = groupActivityService.getOne(new LambdaQueryWrapper<GroupActivity>()
                .eq(GroupActivity::getProductId, id)
                .eq(GroupActivity::getStatus, 1)
                .le(GroupActivity::getStartTime, now)
                .ge(GroupActivity::getEndTime, now)
                .last("LIMIT 1"));

        if (groupActivity != null) {
            result.put("groupEnabled", true);
            result.put("groupPrice", groupActivity.getGroupPrice());
            result.put("groupSize", groupActivity.getGroupSize());
            result.put("groupActivityId", groupActivity.getId());
        } else {
            result.put("groupEnabled", false);
        }

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
