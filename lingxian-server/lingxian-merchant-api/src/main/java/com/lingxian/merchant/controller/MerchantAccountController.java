package com.lingxian.merchant.controller;

import com.lingxian.common.entity.MerchantUser;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.MerchantUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/merchant/account")
@RequiredArgsConstructor
@Tag(name = "商户端-账号管理", description = "商户账号信息管理接口")
public class MerchantAccountController {

    private final MerchantUserService merchantUserService;

    @GetMapping
    @Operation(summary = "获取账号信息")
    public Result<Map<String, Object>> getAccountInfo(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null) {
            return Result.failed("用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("phone", merchantUser.getPhone());
        data.put("loginVerify", false); // 默认关闭登录验证
        data.put("hasPayPassword", false); // 默认未设置支付密码

        // 当前设备信息（模拟数据，实际应从登录设备表获取）
        List<Map<String, Object>> devices = new ArrayList<>();
        Map<String, Object> currentDevice = new HashMap<>();
        currentDevice.put("id", 1);
        currentDevice.put("type", "mobile");
        currentDevice.put("name", "微信小程序");
        currentDevice.put("lastLoginTime", merchantUser.getLastLoginTime() != null
                ? merchantUser.getLastLoginTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "");
        currentDevice.put("isCurrent", true);
        devices.add(currentDevice);

        data.put("devices", devices);

        return Result.success(data);
    }

    @PutMapping("/login-verify")
    @Operation(summary = "更新登录验证设置")
    public Result<Void> updateLoginVerify(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {

        log.info("更新登录验证设置: userId={}, body={}", userId, body);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        // TODO: 保存登录验证设置到数据库
        return Result.success();
    }

    @DeleteMapping("/devices/{deviceId}")
    @Operation(summary = "移除登录设备")
    public Result<Void> removeDevice(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long deviceId) {

        log.info("移除登录设备: userId={}, deviceId={}", userId, deviceId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        // TODO: 从数据库删除设备记录
        return Result.success();
    }
}
