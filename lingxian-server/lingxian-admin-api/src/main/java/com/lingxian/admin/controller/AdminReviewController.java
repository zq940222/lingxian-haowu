package com.lingxian.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.*;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "管理后台-评价管理", description = "订单评价管理相关接口")
public class AdminReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final ProductService productService;
    private final MerchantService merchantService;
    private final OrderService orderService;

    private static final String[] RATING_LABELS = {"", "非常差", "较差", "一般", "较好", "非常好"};

    @GetMapping
    @Operation(summary = "获取评价列表")
    public Result<PageResult<Review>> getList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "商品名称") @RequestParam(required = false) String productName,
            @Parameter(description = "用户昵称") @RequestParam(required = false) String userNickname,
            @Parameter(description = "商户ID") @RequestParam(required = false) Long merchantId,
            @Parameter(description = "评分") @RequestParam(required = false) Integer rating,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        log.info("获取评价列表: page={}, size={}, productName={}, merchantId={}, rating={}, status={}",
                page, size, productName, merchantId, rating, status);

        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();

        if (merchantId != null) {
            wrapper.eq(Review::getMerchantId, merchantId);
        }
        if (rating != null) {
            wrapper.eq(Review::getRating, rating);
        }
        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        }

        wrapper.orderByDesc(Review::getCreateTime);

        Page<Review> pageResult = reviewService.page(new Page<>(page, size), wrapper);

        // 填充关联信息
        for (Review review : pageResult.getRecords()) {
            fillReviewInfo(review);
        }

        // 根据商品名称和用户昵称进行后置过滤（因为这些字段不在数据库表中）
        if (StringUtils.hasText(productName) || StringUtils.hasText(userNickname)) {
            pageResult.getRecords().removeIf(review -> {
                if (StringUtils.hasText(productName) &&
                    (review.getProductName() == null || !review.getProductName().contains(productName))) {
                    return true;
                }
                if (StringUtils.hasText(userNickname) &&
                    (review.getUserNickname() == null || !review.getUserNickname().contains(userNickname))) {
                    return true;
                }
                return false;
            });
        }

        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getCurrent(),
                pageResult.getSize(), pageResult.getRecords()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取评价详情")
    public Result<Review> getDetail(@PathVariable Long id) {
        log.info("获取评价详情: id={}", id);

        Review review = reviewService.getById(id);
        if (review == null) {
            return Result.failed("评价不存在");
        }

        fillReviewInfo(review);
        return Result.success(review);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新评价状态（显示/隐藏）")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        log.info("更新评价状态: id={}, status={}", id, status);

        Review review = reviewService.getById(id);
        if (review == null) {
            return Result.failed("评价不存在");
        }

        review.setStatus(status);
        reviewService.updateById(review);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评价")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除评价: id={}", id);

        Review review = reviewService.getById(id);
        if (review == null) {
            return Result.failed("评价不存在");
        }

        reviewService.removeById(id);
        return Result.success();
    }

    @GetMapping("/stats")
    @Operation(summary = "获取评价统计")
    public Result<Map<String, Object>> getStats(
            @Parameter(description = "商户ID") @RequestParam(required = false) Long merchantId) {
        log.info("获取评价统计: merchantId={}", merchantId);

        LambdaQueryWrapper<Review> baseWrapper = new LambdaQueryWrapper<>();
        if (merchantId != null) {
            baseWrapper.eq(Review::getMerchantId, merchantId);
        }

        long totalCount = reviewService.count(baseWrapper);

        // 各评分数量
        long rating5 = reviewService.count(new LambdaQueryWrapper<Review>()
                .eq(merchantId != null, Review::getMerchantId, merchantId)
                .eq(Review::getRating, 5));
        long rating4 = reviewService.count(new LambdaQueryWrapper<Review>()
                .eq(merchantId != null, Review::getMerchantId, merchantId)
                .eq(Review::getRating, 4));
        long rating3 = reviewService.count(new LambdaQueryWrapper<Review>()
                .eq(merchantId != null, Review::getMerchantId, merchantId)
                .eq(Review::getRating, 3));
        long rating2 = reviewService.count(new LambdaQueryWrapper<Review>()
                .eq(merchantId != null, Review::getMerchantId, merchantId)
                .eq(Review::getRating, 2));
        long rating1 = reviewService.count(new LambdaQueryWrapper<Review>()
                .eq(merchantId != null, Review::getMerchantId, merchantId)
                .eq(Review::getRating, 1));

        // 好评率（4-5星）
        double goodRate = totalCount > 0 ? (double) (rating4 + rating5) / totalCount * 100 : 0;

        return Result.success(Map.of(
                "totalCount", totalCount,
                "rating5", rating5,
                "rating4", rating4,
                "rating3", rating3,
                "rating2", rating2,
                "rating1", rating1,
                "goodRate", String.format("%.1f", goodRate)
        ));
    }

    private void fillReviewInfo(Review review) {
        // 填充用户信息
        if (review.getUserId() != null) {
            User user = userService.getById(review.getUserId());
            if (user != null) {
                if (review.getIsAnonymous() != null && review.getIsAnonymous() == 1) {
                    review.setUserNickname("匿名用户");
                    review.setUserAvatar(null);
                } else {
                    review.setUserNickname(user.getNickname());
                    review.setUserAvatar(user.getAvatar());
                }
            }
        }

        // 填充商品信息
        if (review.getProductId() != null) {
            Product product = productService.getById(review.getProductId());
            if (product != null) {
                review.setProductName(product.getName());
                review.setProductImage(product.getImage());
            }
        }

        // 填充商户信息
        if (review.getMerchantId() != null) {
            Merchant merchant = merchantService.getById(review.getMerchantId());
            if (merchant != null) {
                review.setMerchantName(merchant.getName());
            }
        }

        // 填充订单编号
        if (review.getOrderId() != null) {
            Order order = orderService.getById(review.getOrderId());
            if (order != null) {
                review.setOrderNo(order.getOrderNo());
            }
        }
    }
}
