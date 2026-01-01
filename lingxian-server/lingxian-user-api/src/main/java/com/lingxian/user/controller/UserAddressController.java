package com.lingxian.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lingxian.common.entity.UserAddress;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户收货地址控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/addresses")
@RequiredArgsConstructor
@Tag(name = "用户端-收货地址", description = "用户收货地址管理接口")
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    @Operation(summary = "获取地址列表")
    public Result<List<Map<String, Object>>> getAddressList(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("获取地址列表: userId={}", userId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getDeleted, 0)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getUpdateTime);

        List<UserAddress> addresses = userAddressService.list(queryWrapper);

        List<Map<String, Object>> result = addresses.stream()
                .map(this::convertAddress)
                .collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取地址详情")
    public Result<Map<String, Object>> getAddressDetail(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        log.info("获取地址详情: userId={}, addressId={}", userId, id);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        UserAddress address = userAddressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.failed("地址不存在");
        }

        return Result.success(convertAddress(address));
    }

    @GetMapping("/default")
    @Operation(summary = "获取默认地址")
    public Result<Map<String, Object>> getDefaultAddress(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("获取默认地址: userId={}", userId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .eq(UserAddress::getDeleted, 0)
                .last("LIMIT 1");

        UserAddress address = userAddressService.getOne(queryWrapper);

        if (address == null) {
            // 如果没有默认地址，返回最近更新的地址
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserAddress::getUserId, userId)
                    .eq(UserAddress::getDeleted, 0)
                    .orderByDesc(UserAddress::getUpdateTime)
                    .last("LIMIT 1");
            address = userAddressService.getOne(queryWrapper);
        }

        if (address == null) {
            return Result.success(null);
        }

        return Result.success(convertAddress(address));
    }

    @PostMapping
    @Operation(summary = "添加地址")
    public Result<Map<String, Object>> addAddress(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        log.info("添加地址: userId={}, body={}", userId, body);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setName((String) body.get("name"));
        address.setPhone((String) body.get("phone"));
        address.setProvince((String) body.get("province"));
        address.setCity((String) body.get("city"));
        address.setDistrict((String) body.get("district"));
        address.setDetail((String) body.get("detail"));

        Boolean isDefault = (Boolean) body.get("isDefault");
        address.setIsDefault(Boolean.TRUE.equals(isDefault) ? 1 : 0);

        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        address.setDeleted(0);

        // 如果设为默认，取消其他默认地址
        if (address.getIsDefault() == 1) {
            cancelOtherDefault(userId);
        }

        userAddressService.save(address);

        Map<String, Object> result = new HashMap<>();
        result.put("id", address.getId());

        return Result.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新地址")
    public Result<Void> updateAddress(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        log.info("更新地址: userId={}, addressId={}, body={}", userId, id, body);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        UserAddress address = userAddressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.failed("地址不存在");
        }

        if (body.containsKey("name")) {
            address.setName((String) body.get("name"));
        }
        if (body.containsKey("phone")) {
            address.setPhone((String) body.get("phone"));
        }
        if (body.containsKey("province")) {
            address.setProvince((String) body.get("province"));
        }
        if (body.containsKey("city")) {
            address.setCity((String) body.get("city"));
        }
        if (body.containsKey("district")) {
            address.setDistrict((String) body.get("district"));
        }
        if (body.containsKey("detail")) {
            address.setDetail((String) body.get("detail"));
        }
        if (body.containsKey("isDefault")) {
            Boolean isDefault = (Boolean) body.get("isDefault");
            address.setIsDefault(Boolean.TRUE.equals(isDefault) ? 1 : 0);

            if (address.getIsDefault() == 1) {
                cancelOtherDefault(userId);
            }
        }

        address.setUpdateTime(LocalDateTime.now());
        userAddressService.updateById(address);

        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址")
    public Result<Void> deleteAddress(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        log.info("删除地址: userId={}, addressId={}", userId, id);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        UserAddress address = userAddressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.failed("地址不存在");
        }

        address.setDeleted(1);
        address.setUpdateTime(LocalDateTime.now());
        userAddressService.updateById(address);

        return Result.success();
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "设为默认地址")
    public Result<Void> setDefaultAddress(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        log.info("设为默认地址: userId={}, addressId={}", userId, id);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        UserAddress address = userAddressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.failed("地址不存在");
        }

        // 取消其他默认地址
        cancelOtherDefault(userId);

        // 设为默认
        address.setIsDefault(1);
        address.setUpdateTime(LocalDateTime.now());
        userAddressService.updateById(address);

        return Result.success();
    }

    /**
     * 取消用户其他默认地址
     */
    private void cancelOtherDefault(Long userId) {
        LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .set(UserAddress::getIsDefault, 0)
                .set(UserAddress::getUpdateTime, LocalDateTime.now());
        userAddressService.update(updateWrapper);
    }

    /**
     * 转换地址为响应格式
     */
    private Map<String, Object> convertAddress(UserAddress address) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", address.getId());
        map.put("name", address.getName());
        map.put("phone", address.getPhone());
        map.put("province", address.getProvince());
        map.put("city", address.getCity());
        map.put("district", address.getDistrict());
        map.put("detail", address.getDetail());
        map.put("isDefault", address.getIsDefault() == 1);
        map.put("fullAddress", address.getProvince() + address.getCity() +
                address.getDistrict() + address.getDetail());
        return map;
    }
}
