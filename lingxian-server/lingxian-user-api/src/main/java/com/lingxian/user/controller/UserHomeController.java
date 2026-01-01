package com.lingxian.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingxian.common.entity.Banner;
import com.lingxian.common.entity.Category;
import com.lingxian.common.entity.GroupActivity;
import com.lingxian.common.entity.Product;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.BannerService;
import com.lingxian.common.service.CategoryService;
import com.lingxian.common.service.GroupActivityService;
import com.lingxian.common.service.ProductService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping("/home")
    @Operation(summary = "获取首页数据")
    public Result<Map<String, Object>> getHomeData() {
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

        // 拼团活动 - 进行中的活动，前4个
        LocalDateTime now = LocalDateTime.now();
        List<GroupActivity> groupActivities = groupActivityService.list(new LambdaQueryWrapper<GroupActivity>()
                .eq(GroupActivity::getStatus, 1)
                .le(GroupActivity::getStartTime, now)
                .ge(GroupActivity::getEndTime, now)
                .orderByDesc(GroupActivity::getCreateTime)
                .last("LIMIT 4"));

        // 填充拼团活动的商品信息
        if (!groupActivities.isEmpty()) {
            List<Long> productIds = groupActivities.stream()
                    .map(GroupActivity::getProductId)
                    .collect(Collectors.toList());
            Map<Long, Product> productMap = productService.listByIds(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));

            groupActivities.forEach(activity -> {
                Product product = productMap.get(activity.getProductId());
                if (product != null) {
                    activity.setProductName(product.getName());
                    // 处理商品图片URL
                    activity.setProductImage(imageUrlUtil.generateUrl(product.getImage()));
                }
            });
        }
        data.put("groupActivities", groupActivities);

        return Result.success(data);
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
