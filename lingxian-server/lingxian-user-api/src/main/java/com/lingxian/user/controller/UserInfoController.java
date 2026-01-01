package com.lingxian.user.controller;

import com.lingxian.common.entity.User;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.UserService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户端-用户信息", description = "用户信息管理接口")
public class UserInfoController {

    private final UserService userService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping("/info")
    @Operation(summary = "获取用户信息")
    public Result<Map<String, Object>> getUserInfo(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("获取用户信息: userId={}", userId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.failed("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("nickname", user.getNickname());
        result.put("avatar", imageUrlUtil.generateUrl(user.getAvatar()));
        result.put("phone", user.getPhone());
        result.put("gender", user.getGender());
        result.put("birthday", user.getBirthday());
        result.put("points", user.getPoints());
        result.put("balance", user.getBalance());
        result.put("status", user.getStatus());

        return Result.success(result);
    }

    @PutMapping("/info")
    @Operation(summary = "更新用户信息")
    public Result<Void> updateUserInfo(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        log.info("更新用户信息: userId={}, body={}", userId, body);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.failed("用户不存在");
        }

        // 更新允许修改的字段
        if (body.containsKey("nickname")) {
            user.setNickname((String) body.get("nickname"));
        }
        if (body.containsKey("avatar")) {
            user.setAvatar((String) body.get("avatar"));
        }
        if (body.containsKey("gender")) {
            user.setGender(Integer.parseInt(body.get("gender").toString()));
        }
        if (body.containsKey("birthday")) {
            String birthdayStr = (String) body.get("birthday");
            if (birthdayStr != null && !birthdayStr.isEmpty()) {
                user.setBirthday(java.time.LocalDate.parse(birthdayStr));
            }
        }

        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);

        return Result.success();
    }
}
