package com.lingxian.admin.controller;

import com.lingxian.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
@Tag(name = "管理后台-认证", description = "管理后台认证登录接口")
public class AdminAuthController {

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        log.info("管理员登录: {}", username);

        // TODO: 实际验证逻辑
        // 模拟登录成功
        Map<String, Object> data = new HashMap<>();
        data.put("token", "mock_admin_token_" + System.currentTimeMillis());

        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("id", 1);
        adminInfo.put("username", username);
        adminInfo.put("realName", "超级管理员");
        adminInfo.put("avatar", "");
        adminInfo.put("roles", Arrays.asList("admin"));
        adminInfo.put("permissions", Arrays.asList("*"));
        data.put("userInfo", adminInfo);

        return Result.success(data);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout() {
        log.info("管理员退出登录");
        return Result.success();
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前管理员信息")
    public Result<Map<String, Object>> getAdminInfo() {
        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("id", 1);
        adminInfo.put("username", "admin");
        adminInfo.put("realName", "超级管理员");
        adminInfo.put("avatar", "");
        adminInfo.put("roles", Arrays.asList("admin"));
        adminInfo.put("permissions", Arrays.asList("*"));
        return Result.success(adminInfo);
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> updatePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        log.info("管理员修改密码");
        // TODO: 实际修改密码逻辑
        return Result.success();
    }
}
