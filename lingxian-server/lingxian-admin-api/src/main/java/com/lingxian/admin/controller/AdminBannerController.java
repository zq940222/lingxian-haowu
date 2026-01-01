package com.lingxian.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.Banner;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
@Tag(name = "管理后台-轮播图管理", description = "轮播图管理相关接口")
public class AdminBannerController {

    private final BannerService bannerService;

    @GetMapping
    @Operation(summary = "获取轮播图列表")
    public Result<PageResult<Banner>> getBannerList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "位置") @RequestParam(required = false) Integer position,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        log.info("获取轮播图列表: page={}, size={}, position={}, status={}", page, size, position, status);

        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        if (position != null) {
            wrapper.eq(Banner::getPosition, position);
        }
        if (status != null) {
            wrapper.eq(Banner::getStatus, status);
        }
        wrapper.orderByAsc(Banner::getSort).orderByDesc(Banner::getCreateTime);

        Page<Banner> pageResult = bannerService.page(new Page<>(page, size), wrapper);

        return Result.success(PageResult.of(
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getRecords()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取轮播图详情")
    public Result<Banner> getBannerDetail(@PathVariable Long id) {
        log.info("获取轮播图详情: id={}", id);
        Banner banner = bannerService.getById(id);
        if (banner == null) {
            return Result.failed("轮播图不存在");
        }
        return Result.success(banner);
    }

    @PostMapping
    @Operation(summary = "创建轮播图")
    public Result<Banner> createBanner(@RequestBody Banner banner) {
        log.info("创建轮播图: banner={}", banner);

        // 设置默认值
        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        if (banner.getPosition() == null) {
            banner.setPosition(1);
        }

        bannerService.save(banner);
        return Result.success(banner);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新轮播图")
    public Result<Void> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        log.info("更新轮播图: id={}, banner={}", id, banner);

        Banner existBanner = bannerService.getById(id);
        if (existBanner == null) {
            return Result.failed("轮播图不存在");
        }

        banner.setId(id);
        bannerService.updateById(banner);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除轮播图")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        log.info("删除轮播图: id={}", id);

        Banner banner = bannerService.getById(id);
        if (banner == null) {
            return Result.failed("轮播图不存在");
        }

        bannerService.removeById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新轮播图状态")
    public Result<Void> updateBannerStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        log.info("更新轮播图状态: id={}, body={}", id, body);

        Banner banner = bannerService.getById(id);
        if (banner == null) {
            return Result.failed("轮播图不存在");
        }

        Integer status = body.get("status");
        if (status != null) {
            banner.setStatus(status);
            bannerService.updateById(banner);
        }

        return Result.success();
    }

    @PutMapping("/sort")
    @Operation(summary = "批量更新轮播图排序")
    public Result<Void> updateBannerSort(@RequestBody List<Map<String, Object>> body) {
        log.info("更新轮播图排序: body={}", body);

        for (Map<String, Object> item : body) {
            Long id = Long.valueOf(item.get("id").toString());
            Integer sort = Integer.valueOf(item.get("sort").toString());

            Banner banner = new Banner();
            banner.setId(id);
            banner.setSort(sort);
            bannerService.updateById(banner);
        }

        return Result.success();
    }
}
