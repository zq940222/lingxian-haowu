package com.lingxian.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingxian.common.entity.Banner;
import com.lingxian.common.entity.Category;
import com.lingxian.common.entity.GroupActivity;
import com.lingxian.common.entity.Merchant;
import com.lingxian.common.entity.Product;
import com.lingxian.common.result.Result;
import com.lingxian.common.entity.MerchantCommunity;
import com.lingxian.common.service.BannerService;
import com.lingxian.common.service.CategoryService;
import com.lingxian.common.service.GroupActivityService;
import com.lingxian.common.service.MerchantCommunityService;
import com.lingxian.common.service.MerchantService;
import com.lingxian.common.service.ProductService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户端-首页", description = "首页相关接口")
public class UserHomeController {

    private final BannerService bannerService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final GroupActivityService groupActivityService;
    private final MerchantService merchantService;
    private final MerchantCommunityService merchantCommunityService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping("/home")
    @Operation(summary = "获取首页数据")
    public Result<Map<String, Object>> getHomeData(
            @RequestParam(required = false) Long communityId) {
        Map<String, Object> data = new HashMap<>();

        // 轮播图 - 首页位置(position=1)，启用状态(status=1)
        List<Banner> banners = bannerService.list(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getPosition, 1)
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSort));
        // 处理轮播图图片URL
        banners.forEach(banner -> banner.setImageUrl(imageUrlUtil.generateUrl(banner.getImageUrl())));
        data.put("banners", banners);

        // 分类 - 只取一级分类(parentId=0)，启用状态，前8个
        List<Category> categories = categoryService.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, 0L)
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
                .last("LIMIT 8"));
        // 处理分类图标URL
        categories.forEach(category -> {
            category.setIcon(imageUrlUtil.generateUrl(category.getIcon()));
            category.setImage(imageUrlUtil.generateUrl(category.getImage()));
        });
        data.put("categories", categories);

        // 获取可用的商户ID列表（营业中且可配送到指定小区）
        List<Long> availableMerchantIds = getAvailableMerchantIds(communityId);

        // 拼团活动 - 进行中的活动，前4个（只显示可用商户的商品）
        LocalDateTime now = LocalDateTime.now();
        List<GroupActivity> groupActivities = new ArrayList<>();

        if (!availableMerchantIds.isEmpty()) {
            groupActivities = groupActivityService.list(new LambdaQueryWrapper<GroupActivity>()
                    .eq(GroupActivity::getStatus, 1)
                    .le(GroupActivity::getStartTime, now)
                    .ge(GroupActivity::getEndTime, now)
                    .orderByDesc(GroupActivity::getCreateTime)
                    .last("LIMIT 20")); // 先取多一些，后面过滤

            // 填充拼团活动的商品信息，并过滤掉不可用商户的商品
            if (!groupActivities.isEmpty()) {
                List<Long> productIds = groupActivities.stream()
                        .map(GroupActivity::getProductId)
                        .collect(Collectors.toList());
                Map<Long, Product> productMap = productService.listByIds(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

                groupActivities = groupActivities.stream()
                        .filter(activity -> {
                            Product product = productMap.get(activity.getProductId());
                            // 过滤：商品存在且商户可用
                            return product != null && availableMerchantIds.contains(product.getMerchantId());
                        })
                        .limit(4) // 取前4个
                        .peek(activity -> {
                            Product product = productMap.get(activity.getProductId());
                            activity.setProductName(product.getName());
                            // 处理商品图片URL
                            activity.setProductImage(imageUrlUtil.generateUrl(product.getImage()));
                        })
                        .collect(Collectors.toList());
            }
        }
        data.put("groupActivities", groupActivities);

        return Result.success(data);
    }

    /**
     * 获取可用的商户ID列表（营业中且可配送到指定小区）
     * @param communityId 小区ID，如果为null则返回所有营业中的商户
     */
    private List<Long> getAvailableMerchantIds(Long communityId) {
        // 先获取营业中的商户ID
        List<Long> openMerchantIds = merchantService.list(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getStatus, 1)
                .select(Merchant::getId))
                .stream()
                .map(Merchant::getId)
                .collect(Collectors.toList());

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

    @GetMapping("/banners")
    @Operation(summary = "获取轮播图")
    public Result<List<Banner>> getBanners() {
        List<Banner> banners = bannerService.list(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getPosition, 1)
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSort));
        // 处理轮播图图片URL
        banners.forEach(banner -> banner.setImageUrl(imageUrlUtil.generateUrl(banner.getImageUrl())));
        return Result.success(banners);
    }
}
