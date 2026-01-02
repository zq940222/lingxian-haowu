package com.lingxian.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.GroupActivity;
import com.lingxian.common.entity.Merchant;
import com.lingxian.common.entity.Product;
import com.lingxian.common.entity.ProductComment;
import com.lingxian.common.entity.User;
import com.lingxian.common.result.Result;
import com.lingxian.common.entity.MerchantCommunity;
import com.lingxian.common.service.GroupActivityService;
import com.lingxian.common.service.MerchantCommunityService;
import com.lingxian.common.service.MerchantService;
import com.lingxian.common.service.ProductCommentService;
import com.lingxian.common.service.ProductService;
import com.lingxian.common.service.UserService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user/products")
@RequiredArgsConstructor
@Tag(name = "用户端-商品", description = "商品相关接口")
public class UserProductController {

    private final ProductService productService;
    private final GroupActivityService groupActivityService;
    private final MerchantService merchantService;
    private final MerchantCommunityService merchantCommunityService;
    private final ProductCommentService commentService;
    private final UserService userService;
    private final ImageUrlUtil imageUrlUtil;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @GetMapping("/recommend")
    @Operation(summary = "获取推荐商品")
    public Result<Map<String, Object>> getRecommendProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long communityId) {

        // 获取营业中的商户ID列表（如果指定了小区，还要过滤该小区可配送的商户）
        List<Long> openMerchantIds = getAvailableMerchantIds(communityId);

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .eq(Product::getIsRecommend, 1);

        // 只展示营业中商户的商品
        if (!openMerchantIds.isEmpty()) {
            queryWrapper.in(Product::getMerchantId, openMerchantIds);
        } else {
            // 没有营业中的商户，返回空结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", new ArrayList<>());
            result.put("total", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            return Result.success(result);
        }

        queryWrapper.orderByDesc(Product::getSalesCount)
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

    @GetMapping
    @Operation(summary = "获取商品列表")
    public Result<Map<String, Object>> getProductList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "default") String sortType,
            @RequestParam(defaultValue = "asc") String priceOrder,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        // 获取营业中的商户ID列表（如果指定了小区，还要过滤该小区可配送的商户）
        List<Long> openMerchantIds = getAvailableMerchantIds(communityId);

        // 如果没有营业中的商户，返回空结果
        if (openMerchantIds.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("records", new ArrayList<>());
            result.put("total", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            return Result.success(result);
        }

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .in(Product::getMerchantId, openMerchantIds);

        // 指定商户筛选
        if (merchantId != null) {
            // 检查指定商户是否营业中
            if (!openMerchantIds.contains(merchantId)) {
                Map<String, Object> result = new HashMap<>();
                result.put("records", new ArrayList<>());
                result.put("total", 0);
                result.put("page", page);
                result.put("pageSize", pageSize);
                return Result.success(result);
            }
            queryWrapper.eq(Product::getMerchantId, merchantId);
        }

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

        // 检查商户是否营业中
        if (product.getMerchantId() != null) {
            Merchant merchant = merchantService.getById(product.getMerchantId());
            if (merchant == null || merchant.getStatus() != 1) {
                return Result.failed("商户已休息，暂时无法查看该商品");
            }
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

        // 查询商户信息
        if (product.getMerchantId() != null) {
            Merchant merchant = merchantService.getById(product.getMerchantId());
            if (merchant != null) {
                Map<String, Object> merchantInfo = new HashMap<>();
                merchantInfo.put("id", merchant.getId());
                merchantInfo.put("name", merchant.getName());
                merchantInfo.put("logo", imageUrlUtil.generateUrl(merchant.getLogo()));
                merchantInfo.put("description", merchant.getDescription());
                merchantInfo.put("rating", merchant.getRating());
                merchantInfo.put("address", merchant.getAddress());
                merchantInfo.put("province", merchant.getProvince());
                merchantInfo.put("city", merchant.getCity());
                merchantInfo.put("district", merchant.getDistrict());
                merchantInfo.put("status", merchant.getStatus());
                result.put("merchant", merchantInfo);
            }
        }

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

        // 查询评价统计
        long totalComments = commentService.count(new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getProductId, id)
                .eq(ProductComment::getStatus, 1)
                .eq(ProductComment::getDeleted, 0));

        // 好评数（4-5星）
        long goodComments = commentService.count(new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getProductId, id)
                .eq(ProductComment::getStatus, 1)
                .eq(ProductComment::getDeleted, 0)
                .ge(ProductComment::getRating, 4));

        // 计算好评率
        int goodRate = totalComments > 0 ? (int) (goodComments * 100 / totalComments) : 100;

        // 计算平均评分
        BigDecimal avgRating = BigDecimal.valueOf(5.0);
        if (totalComments > 0) {
            List<ProductComment> allComments = commentService.list(new LambdaQueryWrapper<ProductComment>()
                    .eq(ProductComment::getProductId, id)
                    .eq(ProductComment::getStatus, 1)
                    .eq(ProductComment::getDeleted, 0)
                    .select(ProductComment::getRating));
            double avg = allComments.stream()
                    .mapToInt(ProductComment::getRating)
                    .average()
                    .orElse(5.0);
            avgRating = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
        }

        Map<String, Object> commentStats = new HashMap<>();
        commentStats.put("total", totalComments);
        commentStats.put("goodCount", goodComments);
        commentStats.put("goodRate", goodRate);
        commentStats.put("avgRating", avgRating);
        result.put("commentStats", commentStats);

        // 查询最新3条评价
        List<ProductComment> latestComments = commentService.list(new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getProductId, id)
                .eq(ProductComment::getStatus, 1)
                .eq(ProductComment::getDeleted, 0)
                .orderByDesc(ProductComment::getCreateTime)
                .last("LIMIT 3"));

        List<Map<String, Object>> commentList = convertComments(latestComments);
        result.put("comments", commentList);

        return Result.success(result);
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "获取商品评价列表")
    public Result<Map<String, Object>> getProductComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "all") String type) {

        LambdaQueryWrapper<ProductComment> queryWrapper = new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getProductId, id)
                .eq(ProductComment::getStatus, 1)
                .eq(ProductComment::getDeleted, 0);

        // 按类型筛选
        switch (type) {
            case "good":
                queryWrapper.ge(ProductComment::getRating, 4);
                break;
            case "medium":
                queryWrapper.eq(ProductComment::getRating, 3);
                break;
            case "bad":
                queryWrapper.le(ProductComment::getRating, 2);
                break;
            case "image":
                queryWrapper.isNotNull(ProductComment::getImages)
                        .ne(ProductComment::getImages, "")
                        .ne(ProductComment::getImages, "[]");
                break;
        }

        queryWrapper.orderByDesc(ProductComment::getCreateTime);

        Page<ProductComment> pageResult = commentService.page(new Page<>(page, pageSize), queryWrapper);

        List<Map<String, Object>> commentList = convertComments(pageResult.getRecords());

        Map<String, Object> result = new HashMap<>();
        result.put("records", commentList);
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    /**
     * 转换评价列表
     */
    private List<Map<String, Object>> convertComments(List<ProductComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量获取用户信息
        List<Long> userIds = comments.stream()
                .map(ProductComment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userService.listByIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductComment comment : comments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", comment.getId());
            item.put("content", comment.getContent());
            item.put("rating", comment.getRating());
            item.put("images", imageUrlUtil.generateUrlsFromJson(comment.getImages()));
            item.put("createTime", comment.getCreateTime() != null ?
                    comment.getCreateTime().format(DATE_FORMATTER) : "");
            item.put("replyContent", comment.getReplyContent());
            item.put("replyTime", comment.getReplyTime() != null ?
                    comment.getReplyTime().format(DATE_FORMATTER) : "");

            // 用户信息
            User user = userMap.get(comment.getUserId());
            if (comment.getIsAnonymous() == 1 || user == null) {
                item.put("userName", "匿名用户");
                item.put("userAvatar", "");
            } else {
                String nickname = user.getNickname();
                if (nickname != null && nickname.length() > 1) {
                    nickname = nickname.charAt(0) + "***" + nickname.charAt(nickname.length() - 1);
                }
                item.put("userName", nickname);
                item.put("userAvatar", user.getAvatar());
            }

            result.add(item);
        }
        return result;
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

    /**
     * 获取可用的商户ID列表（营业中且可配送到指定小区）
     * @param communityId 小区ID，如果为null则返回所有营业中的商户
     */
    private List<Long> getAvailableMerchantIds(Long communityId) {
        // 先获取营业中的商户ID
        List<Long> openMerchantIds = getOpenMerchantIds();

        if (openMerchantIds.isEmpty()) {
            return openMerchantIds;
        }

        // 如果指定了小区，还要过滤该小区可配送的商户
        if (communityId != null) {
            // 查询该小区开放配送的商户ID
            List<MerchantCommunity> merchantCommunities = merchantCommunityService.list(
                    new LambdaQueryWrapper<MerchantCommunity>()
                            .eq(MerchantCommunity::getCommunityId, communityId)
                            .eq(MerchantCommunity::getEnabled, 1)
            );

            if (merchantCommunities.isEmpty()) {
                return new ArrayList<>();
            }

            List<Long> communityMerchantIds = merchantCommunities.stream()
                    .map(MerchantCommunity::getMerchantId)
                    .collect(Collectors.toList());

            // 取交集：营业中且可配送到该小区的商户
            openMerchantIds = openMerchantIds.stream()
                    .filter(communityMerchantIds::contains)
                    .collect(Collectors.toList());
        }

        return openMerchantIds;
    }
}
