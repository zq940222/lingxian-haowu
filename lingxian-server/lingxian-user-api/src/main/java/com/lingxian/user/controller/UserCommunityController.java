package com.lingxian.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingxian.common.entity.Community;
import com.lingxian.common.entity.MerchantCommunity;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.CommunityService;
import com.lingxian.common.service.MerchantCommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户端小区控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/community")
@RequiredArgsConstructor
@Tag(name = "用户端-小区", description = "小区相关接口")
public class UserCommunityController {

    private final CommunityService communityService;
    private final MerchantCommunityService merchantCommunityService;

    @GetMapping("/available")
    @Operation(summary = "获取当前可配送的小区列表", description = "返回至少有一个商户开放配送的小区")
    public Result<List<Map<String, Object>>> getAvailableCommunities() {
        log.info("获取当前可配送的小区列表");

        // 查询所有开放配送的商户-小区关联（enabled=1）
        LambdaQueryWrapper<MerchantCommunity> mcQuery = new LambdaQueryWrapper<>();
        mcQuery.eq(MerchantCommunity::getEnabled, 1);
        List<MerchantCommunity> enabledMCs = merchantCommunityService.list(mcQuery);

        if (enabledMCs.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 获取所有开放配送的小区ID（去重）
        Set<Long> enabledCommunityIds = enabledMCs.stream()
                .map(MerchantCommunity::getCommunityId)
                .collect(Collectors.toSet());

        // 查询这些小区的详细信息
        List<Community> communities = communityService.listByIds(enabledCommunityIds);

        // 过滤掉禁用的小区并按排序号排序
        communities = communities.stream()
                .filter(c -> c.getStatus() == 1)
                .sorted(Comparator.comparingInt(Community::getSort))
                .collect(Collectors.toList());

        // 统计每个小区有多少商户可配送
        Map<Long, Long> merchantCountMap = enabledMCs.stream()
                .collect(Collectors.groupingBy(MerchantCommunity::getCommunityId, Collectors.counting()));

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
            if (community.getDistrict() != null) fullAddress += community.getDistrict();
            if (community.getAddress() != null) fullAddress += community.getAddress();
            item.put("fullAddress", fullAddress);

            item.put("longitude", community.getLongitude());
            item.put("latitude", community.getLatitude());
            item.put("merchantCount", merchantCountMap.getOrDefault(community.getId(), 0L));

            result.add(item);
        }

        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取小区详情")
    public Result<Map<String, Object>> getCommunityDetail(@PathVariable Long id) {
        log.info("获取小区详情: id={}", id);

        Community community = communityService.getById(id);
        if (community == null || community.getStatus() != 1) {
            return Result.failed("小区不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", community.getId());
        result.put("name", community.getName());
        result.put("province", community.getProvince());
        result.put("city", community.getCity());
        result.put("district", community.getDistrict());
        result.put("address", community.getAddress());
        result.put("longitude", community.getLongitude());
        result.put("latitude", community.getLatitude());

        // 完整地址
        String fullAddress = "";
        if (community.getProvince() != null) fullAddress += community.getProvince();
        if (community.getCity() != null) fullAddress += community.getCity();
        if (community.getDistrict() != null) fullAddress += community.getDistrict();
        if (community.getAddress() != null) fullAddress += community.getAddress();
        result.put("fullAddress", fullAddress);

        // 查询该小区有多少商户可配送
        LambdaQueryWrapper<MerchantCommunity> mcQuery = new LambdaQueryWrapper<>();
        mcQuery.eq(MerchantCommunity::getCommunityId, id)
                .eq(MerchantCommunity::getEnabled, 1);
        long merchantCount = merchantCommunityService.count(mcQuery);
        result.put("merchantCount", merchantCount);

        return Result.success(result);
    }

    @GetMapping("/{id}/merchants")
    @Operation(summary = "获取小区可配送的商户ID列表")
    public Result<List<Long>> getCommunityMerchants(@PathVariable Long id) {
        log.info("获取小区可配送的商户: communityId={}", id);

        // 查询该小区开放配送的商户
        LambdaQueryWrapper<MerchantCommunity> mcQuery = new LambdaQueryWrapper<>();
        mcQuery.eq(MerchantCommunity::getCommunityId, id)
                .eq(MerchantCommunity::getEnabled, 1);
        List<MerchantCommunity> merchantCommunities = merchantCommunityService.list(mcQuery);

        List<Long> merchantIds = merchantCommunities.stream()
                .map(MerchantCommunity::getMerchantId)
                .collect(Collectors.toList());

        return Result.success(merchantIds);
    }
}
