package com.lingxian.merchant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingxian.common.entity.Community;
import com.lingxian.common.entity.MerchantCommunity;
import com.lingxian.common.entity.MerchantUser;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.CommunityService;
import com.lingxian.common.service.MerchantCommunityService;
import com.lingxian.common.service.MerchantUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商户配送小区管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/merchant/community")
@RequiredArgsConstructor
@Tag(name = "商户端-配送小区管理", description = "商户配送小区管理接口")
public class MerchantCommunityController {

    private final CommunityService communityService;
    private final MerchantCommunityService merchantCommunityService;
    private final MerchantUserService merchantUserService;

    @GetMapping("/list")
    @Operation(summary = "获取所有小区列表", description = "返回所有可用小区及当前商户的配送状态")
    public Result<List<Map<String, Object>>> getCommunityList(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {
        log.info("获取配送小区列表: userId={}", userId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Long merchantId = merchantUser.getMerchantId();

        // 查询所有启用的小区
        LambdaQueryWrapper<Community> communityQuery = new LambdaQueryWrapper<>();
        communityQuery.eq(Community::getStatus, 1)
                .orderByAsc(Community::getSort);
        List<Community> communities = communityService.list(communityQuery);

        // 查询商户已配置的小区
        LambdaQueryWrapper<MerchantCommunity> mcQuery = new LambdaQueryWrapper<>();
        mcQuery.eq(MerchantCommunity::getMerchantId, merchantId);
        List<MerchantCommunity> merchantCommunities = merchantCommunityService.list(mcQuery);

        // 构建商户小区配置映射
        Map<Long, MerchantCommunity> mcMap = merchantCommunities.stream()
                .collect(Collectors.toMap(MerchantCommunity::getCommunityId, mc -> mc));

        // 构建返回数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (Community community : communities) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", community.getId());
            item.put("name", community.getName());
            item.put("province", community.getProvince());
            item.put("city", community.getCity());
            item.put("district", community.getDistrict());
            item.put("address", community.getAddress());

            // 完整地址
            String fullAddress = "";
            if (community.getProvince() != null) fullAddress += community.getProvince();
            if (community.getCity() != null) fullAddress += community.getCity();
            if (community.getDistrict() != null) fullAddress += community.getDistrict();
            if (community.getAddress() != null) fullAddress += community.getAddress();
            item.put("fullAddress", fullAddress);

            MerchantCommunity mc = mcMap.get(community.getId());
            if (mc != null) {
                item.put("configId", mc.getId());
                item.put("enabled", mc.getEnabled() == 1);
                item.put("sort", mc.getSort());
                item.put("configured", true);
            } else {
                item.put("configId", null);
                item.put("enabled", false);
                item.put("sort", 0);
                item.put("configured", false);
            }

            result.add(item);
        }

        return Result.success(result);
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的配送小区", description = "只返回商户已配置的小区")
    public Result<List<Map<String, Object>>> getMyCommunities(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {
        log.info("获取我的配送小区: userId={}", userId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Long merchantId = merchantUser.getMerchantId();

        // 查询商户已配置的小区
        LambdaQueryWrapper<MerchantCommunity> mcQuery = new LambdaQueryWrapper<>();
        mcQuery.eq(MerchantCommunity::getMerchantId, merchantId)
                .orderByAsc(MerchantCommunity::getSort);
        List<MerchantCommunity> merchantCommunities = merchantCommunityService.list(mcQuery);

        if (merchantCommunities.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 获取小区信息
        List<Long> communityIds = merchantCommunities.stream()
                .map(MerchantCommunity::getCommunityId)
                .collect(Collectors.toList());
        List<Community> communities = communityService.listByIds(communityIds);
        Map<Long, Community> communityMap = communities.stream()
                .collect(Collectors.toMap(Community::getId, c -> c));

        // 构建返回数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (MerchantCommunity mc : merchantCommunities) {
            Community community = communityMap.get(mc.getCommunityId());
            if (community == null) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("configId", mc.getId());
            item.put("communityId", community.getId());
            item.put("name", community.getName());
            item.put("address", community.getAddress());
            item.put("enabled", mc.getEnabled() == 1);
            item.put("sort", mc.getSort());

            // 完整地址
            String fullAddress = "";
            if (community.getProvince() != null) fullAddress += community.getProvince();
            if (community.getCity() != null) fullAddress += community.getCity();
            if (community.getDistrict() != null) fullAddress += community.getDistrict();
            if (community.getAddress() != null) fullAddress += community.getAddress();
            item.put("fullAddress", fullAddress);

            result.add(item);
        }

        return Result.success(result);
    }

    @PostMapping("/add")
    @Operation(summary = "添加配送小区")
    public Result<Void> addCommunity(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        Long communityId = body.get("communityId") != null ? Long.parseLong(body.get("communityId").toString()) : null;
        log.info("添加配送小区: userId={}, communityId={}", userId, communityId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        if (communityId == null) {
            return Result.failed("小区ID不能为空");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Long merchantId = merchantUser.getMerchantId();

        // 检查小区是否存在
        Community community = communityService.getById(communityId);
        if (community == null || community.getStatus() != 1) {
            return Result.failed("小区不存在或已禁用");
        }

        // 检查是否已添加
        LambdaQueryWrapper<MerchantCommunity> checkQuery = new LambdaQueryWrapper<>();
        checkQuery.eq(MerchantCommunity::getMerchantId, merchantId)
                .eq(MerchantCommunity::getCommunityId, communityId);
        MerchantCommunity existing = merchantCommunityService.getOne(checkQuery);
        if (existing != null) {
            return Result.failed("该小区已添加");
        }

        // 获取当前最大排序号
        LambdaQueryWrapper<MerchantCommunity> sortQuery = new LambdaQueryWrapper<>();
        sortQuery.eq(MerchantCommunity::getMerchantId, merchantId)
                .orderByDesc(MerchantCommunity::getSort)
                .last("LIMIT 1");
        MerchantCommunity maxSort = merchantCommunityService.getOne(sortQuery);
        int newSort = maxSort != null ? maxSort.getSort() + 1 : 1;

        // 添加配送小区
        MerchantCommunity mc = new MerchantCommunity();
        mc.setMerchantId(merchantId);
        mc.setCommunityId(communityId);
        mc.setEnabled(1); // 默认开启
        mc.setSort(newSort);
        mc.setCreateTime(LocalDateTime.now());
        mc.setUpdateTime(LocalDateTime.now());
        mc.setDeleted(0);
        merchantCommunityService.save(mc);

        log.info("添加配送小区成功: merchantId={}, communityId={}", merchantId, communityId);
        return Result.success();
    }

    @DeleteMapping("/{configId}")
    @Operation(summary = "删除配送小区")
    public Result<Void> removeCommunity(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long configId) {
        log.info("删除配送小区: userId={}, configId={}", userId, configId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Long merchantId = merchantUser.getMerchantId();

        // 检查配置是否存在且属于当前商户
        MerchantCommunity mc = merchantCommunityService.getById(configId);
        if (mc == null || !mc.getMerchantId().equals(merchantId)) {
            return Result.failed("配送小区配置不存在");
        }

        merchantCommunityService.removeById(configId);
        log.info("删除配送小区成功: merchantId={}, configId={}", merchantId, configId);
        return Result.success();
    }

    @PutMapping("/{configId}/toggle")
    @Operation(summary = "切换配送小区开放状态")
    public Result<Void> toggleCommunity(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long configId,
            @RequestBody Map<String, Object> body) {
        Boolean enabled = (Boolean) body.get("enabled");
        log.info("切换配送小区状态: userId={}, configId={}, enabled={}", userId, configId, enabled);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        if (enabled == null) {
            return Result.failed("状态参数不能为空");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Long merchantId = merchantUser.getMerchantId();

        // 检查配置是否存在且属于当前商户
        MerchantCommunity mc = merchantCommunityService.getById(configId);
        if (mc == null || !mc.getMerchantId().equals(merchantId)) {
            return Result.failed("配送小区配置不存在");
        }

        mc.setEnabled(enabled ? 1 : 0);
        mc.setUpdateTime(LocalDateTime.now());
        merchantCommunityService.updateById(mc);

        log.info("切换配送小区状态成功: merchantId={}, configId={}, enabled={}", merchantId, configId, enabled);
        return Result.success();
    }

    @PutMapping("/batch-toggle")
    @Operation(summary = "批量切换配送小区开放状态")
    public Result<Void> batchToggle(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Number> configIds = (List<Number>) body.get("configIds");
        Boolean enabled = (Boolean) body.get("enabled");
        log.info("批量切换配送小区状态: userId={}, configIds={}, enabled={}", userId, configIds, enabled);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        if (configIds == null || configIds.isEmpty()) {
            return Result.failed("请选择要操作的小区");
        }

        if (enabled == null) {
            return Result.failed("状态参数不能为空");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Long merchantId = merchantUser.getMerchantId();
        List<Long> ids = configIds.stream().map(Number::longValue).collect(Collectors.toList());

        // 批量更新
        LocalDateTime now = LocalDateTime.now();
        for (Long configId : ids) {
            MerchantCommunity mc = merchantCommunityService.getById(configId);
            if (mc != null && mc.getMerchantId().equals(merchantId)) {
                mc.setEnabled(enabled ? 1 : 0);
                mc.setUpdateTime(now);
                merchantCommunityService.updateById(mc);
            }
        }

        log.info("批量切换配送小区状态成功: merchantId={}, count={}", merchantId, ids.size());
        return Result.success();
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取当前开放的配送小区")
    public Result<List<Map<String, Object>>> getEnabledCommunities(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {
        log.info("获取当前开放的配送小区: userId={}", userId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Long merchantId = merchantUser.getMerchantId();

        // 查询商户已开放的小区
        LambdaQueryWrapper<MerchantCommunity> mcQuery = new LambdaQueryWrapper<>();
        mcQuery.eq(MerchantCommunity::getMerchantId, merchantId)
                .eq(MerchantCommunity::getEnabled, 1)
                .orderByAsc(MerchantCommunity::getSort);
        List<MerchantCommunity> merchantCommunities = merchantCommunityService.list(mcQuery);

        if (merchantCommunities.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 获取小区信息
        List<Long> communityIds = merchantCommunities.stream()
                .map(MerchantCommunity::getCommunityId)
                .collect(Collectors.toList());
        List<Community> communities = communityService.listByIds(communityIds);
        Map<Long, Community> communityMap = communities.stream()
                .collect(Collectors.toMap(Community::getId, c -> c));

        // 构建返回数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (MerchantCommunity mc : merchantCommunities) {
            Community community = communityMap.get(mc.getCommunityId());
            if (community == null) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("configId", mc.getId());
            item.put("communityId", community.getId());
            item.put("name", community.getName());
            item.put("address", community.getAddress());

            result.add(item);
        }

        return Result.success(result);
    }
}
