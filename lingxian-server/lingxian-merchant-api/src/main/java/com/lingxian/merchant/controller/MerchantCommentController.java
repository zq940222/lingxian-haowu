package com.lingxian.merchant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.*;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.*;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 商户端评价管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/merchant/comments")
@RequiredArgsConstructor
@Tag(name = "商户端-评价管理", description = "商户评价管理接口")
public class MerchantCommentController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProductCommentService productCommentService;
    private final MerchantUserService merchantUserService;
    private final UserService userService;
    private final ProductService productService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping
    @Operation(summary = "获取评价列表")
    public Result<PageResult<Map<String, Object>>> getCommentList(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String ratingType,
            @RequestParam(required = false) Integer unreplied) {

        Long merchantId = getMerchantId(userId);
        if (merchantId == null) {
            return Result.failed("请先完成商户入驻");
        }

        // 构建查询条件
        LambdaQueryWrapper<ProductComment> wrapper = new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getMerchantId, merchantId)
                .eq(ProductComment::getDeleted, 0)
                .orderByDesc(ProductComment::getCreateTime);

        // 评分筛选
        if ("good".equals(ratingType)) {
            wrapper.ge(ProductComment::getRating, 4); // 好评: 4-5星
        } else if ("medium".equals(ratingType)) {
            wrapper.eq(ProductComment::getRating, 3); // 中评: 3星
        } else if ("bad".equals(ratingType)) {
            wrapper.le(ProductComment::getRating, 2); // 差评: 1-2星
        }

        // 待回复筛选
        if (unreplied != null && unreplied == 1) {
            wrapper.isNull(ProductComment::getReplyContent);
        }

        // 分页查询
        Page<ProductComment> pageData = productCommentService.page(new Page<>(page, pageSize), wrapper);

        // 获取用户和商品信息
        List<Map<String, Object>> records = new ArrayList<>();
        for (ProductComment comment : pageData.getRecords()) {
            Map<String, Object> map = buildCommentMap(comment);
            records.add(map);
        }

        PageResult<Map<String, Object>> pageResult = PageResult.of(
                pageData.getTotal(),
                pageData.getCurrent(),
                pageData.getSize(),
                records);

        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取评价详情")
    public Result<Map<String, Object>> getCommentDetail(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long id) {

        Long merchantId = getMerchantId(userId);
        if (merchantId == null) {
            return Result.failed("请先完成商户入驻");
        }

        ProductComment comment = productCommentService.getById(id);
        if (comment == null || !comment.getMerchantId().equals(merchantId)) {
            return Result.failed("评价不存在");
        }

        return Result.success(buildCommentMap(comment));
    }

    @PutMapping("/{id}/reply")
    @Operation(summary = "回复评价")
    public Result<Void> replyComment(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Long merchantId = getMerchantId(userId);
        if (merchantId == null) {
            return Result.failed("请先完成商户入驻");
        }

        ProductComment comment = productCommentService.getById(id);
        if (comment == null || !comment.getMerchantId().equals(merchantId)) {
            return Result.failed("评价不存在");
        }

        // 检查是否已回复
        if (comment.getReplyContent() != null && !comment.getReplyContent().trim().isEmpty()) {
            return Result.failed("该评价已回复，不能重复回复");
        }

        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.failed("回复内容不能为空");
        }

        // 更新回复
        LambdaUpdateWrapper<ProductComment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductComment::getId, id)
                .set(ProductComment::getReplyContent, content.trim())
                .set(ProductComment::getReplyTime, LocalDateTime.now())
                .set(ProductComment::getUpdateTime, LocalDateTime.now());

        productCommentService.update(updateWrapper);

        return Result.success();
    }

    @GetMapping("/stats")
    @Operation(summary = "获取评价统计")
    public Result<Map<String, Object>> getCommentStats(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {

        Long merchantId = getMerchantId(userId);
        if (merchantId == null) {
            return Result.failed("请先完成商户入驻");
        }

        // 查询总评价数
        long totalCount = productCommentService.count(
                new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getMerchantId, merchantId)
                        .eq(ProductComment::getDeleted, 0)
        );

        // 查询好评数 (4-5星)
        long goodCount = productCommentService.count(
                new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getMerchantId, merchantId)
                        .eq(ProductComment::getDeleted, 0)
                        .ge(ProductComment::getRating, 4)
        );

        // 查询待回复数
        long unrepliedCount = productCommentService.count(
                new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getMerchantId, merchantId)
                        .eq(ProductComment::getDeleted, 0)
                        .isNull(ProductComment::getReplyContent)
        );

        // 计算平均评分
        List<ProductComment> allComments = productCommentService.list(
                new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getMerchantId, merchantId)
                        .eq(ProductComment::getDeleted, 0)
                        .select(ProductComment::getRating)
        );

        BigDecimal avgRating = BigDecimal.valueOf(5.0);
        if (!allComments.isEmpty()) {
            double sum = allComments.stream()
                    .mapToInt(c -> c.getRating() != null ? c.getRating() : 5)
                    .sum();
            avgRating = BigDecimal.valueOf(sum / allComments.size())
                    .setScale(1, RoundingMode.HALF_UP);
        }

        // 计算好评率
        String goodRate = "100%";
        if (totalCount > 0) {
            BigDecimal rate = BigDecimal.valueOf(goodCount * 100.0 / totalCount)
                    .setScale(0, RoundingMode.HALF_UP);
            goodRate = rate + "%";
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("avgRating", avgRating.toString());
        stats.put("goodRate", goodRate);
        stats.put("unrepliedCount", unrepliedCount);

        return Result.success(stats);
    }

    /**
     * 构建评价响应数据
     */
    private Map<String, Object> buildCommentMap(ProductComment comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("userId", comment.getUserId());
        map.put("productId", comment.getProductId());
        map.put("orderId", comment.getOrderId());
        map.put("content", comment.getContent());
        map.put("rating", comment.getRating());
        map.put("images", comment.getImages());
        map.put("isAnonymous", comment.getIsAnonymous());
        map.put("replyContent", comment.getReplyContent());
        map.put("replyTime", comment.getReplyTime() != null ?
                comment.getReplyTime().format(FORMATTER) : null);
        map.put("createTime", comment.getCreateTime() != null ?
                comment.getCreateTime().format(FORMATTER) : null);

        // 获取用户信息
        if (comment.getUserId() != null) {
            User user = userService.getById(comment.getUserId());
            if (user != null) {
                map.put("userName", user.getNickname());
                map.put("userAvatar", imageUrlUtil.generateUrl(user.getAvatar()));
            }
        }

        // 获取商品信息
        if (comment.getProductId() != null) {
            Product product = productService.getById(comment.getProductId());
            if (product != null) {
                map.put("productName", product.getName());
                map.put("productImage", imageUrlUtil.generateUrl(product.getImage()));
            }
        }

        return map;
    }

    /**
     * 获取商户ID
     */
    private Long getMerchantId(Long userId) {
        if (userId == null) {
            return null;
        }
        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null) {
            return null;
        }
        return merchantUser.getMerchantId();
    }
}
