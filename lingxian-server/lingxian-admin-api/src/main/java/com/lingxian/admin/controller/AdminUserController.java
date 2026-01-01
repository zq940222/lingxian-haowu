package com.lingxian.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.PointsRecord;
import com.lingxian.common.entity.User;
import com.lingxian.common.entity.UserAddress;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.PointsRecordService;
import com.lingxian.common.service.UserAddressService;
import com.lingxian.common.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "管理后台-用户管理", description = "用户管理相关接口")
public class AdminUserController {

    private final UserService userService;
    private final UserAddressService userAddressService;
    private final PointsRecordService pointsRecordService;

    @GetMapping
    @Operation(summary = "获取用户列表")
    public Result<PageResult<User>> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户昵称") @RequestParam(required = false) String nickname,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        log.info("获取用户列表: page={}, size={}, nickname={}, phone={}, status={}", page, size, nickname, phone, status);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(nickname)) {
            wrapper.like(User::getNickname, nickname);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(User::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> pageResult = userService.page(new Page<>(page, size), wrapper);

        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        log.info("获取用户详情: id={}", id);

        User user = userService.getById(id);
        if (user == null) {
            return Result.failed("用户不存在");
        }

        // 获取收货地址列表
        List<UserAddress> addresses = userAddressService.list(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, id)
                        .orderByDesc(UserAddress::getIsDefault)
                        .orderByDesc(UserAddress::getCreateTime)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("openid", user.getOpenid());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("phone", user.getPhone());
        result.put("gender", user.getGender());
        result.put("points", user.getPoints());
        result.put("balance", user.getBalance());
        result.put("status", user.getStatus());
        result.put("orderCount", user.getOrderCount());
        result.put("totalAmount", user.getTotalAmount());
        result.put("createTime", user.getCreateTime());
        result.put("lastLoginTime", user.getLastLoginTime());
        result.put("addresses", addresses);

        return Result.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("更新用户信息: id={}, user={}", id, user);

        User existing = userService.getById(id);
        if (existing == null) {
            return Result.failed("用户不存在");
        }

        user.setId(id);
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        log.info("更新用户状态: id={}, status={}", id, status);

        User user = userService.getById(id);
        if (user == null) {
            return Result.failed("用户不存在");
        }

        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }

    @GetMapping("/{userId}/points")
    @Operation(summary = "获取用户积分记录")
    public Result<PageResult<PointsRecord>> getUserPointsRecords(
            @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "类型") @RequestParam(required = false) Integer type) {
        log.info("获取用户积分记录: userId={}, page={}, size={}, type={}", userId, page, size, type);

        LambdaQueryWrapper<PointsRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRecord::getUserId, userId);
        if (type != null) {
            wrapper.eq(PointsRecord::getType, type);
        }
        wrapper.orderByDesc(PointsRecord::getCreateTime);

        Page<PointsRecord> pageResult = pointsRecordService.page(new Page<>(page, size), wrapper);

        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords()));
    }
}
