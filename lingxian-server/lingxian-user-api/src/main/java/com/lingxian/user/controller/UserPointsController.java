package com.lingxian.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.PointsRecord;
import com.lingxian.common.entity.User;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.PointsRecordService;
import com.lingxian.common.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户积分控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/points")
@RequiredArgsConstructor
@Tag(name = "用户端-积分", description = "积分签到管理接口")
public class UserPointsController {

    private final UserService userService;
    private final PointsRecordService pointsRecordService;
    private final StringRedisTemplate redisTemplate;

    // 签到积分
    private static final int SIGN_IN_POINTS = 10;
    // Redis签到key前缀
    private static final String SIGN_IN_KEY_PREFIX = "user:sign:";

    @PostMapping("/sign-in")
    @Operation(summary = "每日签到")
    public Result<Map<String, Object>> signIn(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("用户签到: userId={}", userId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.failed("用户不存在");
        }

        // 检查今日是否已签到
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String signKey = SIGN_IN_KEY_PREFIX + userId + ":" + today;

        Boolean hasSignedIn = redisTemplate.hasKey(signKey);
        if (Boolean.TRUE.equals(hasSignedIn)) {
            return Result.failed("今日已签到");
        }

        // 签到成功，记录到Redis
        redisTemplate.opsForValue().set(signKey, "1", 2, TimeUnit.DAYS);

        // 增加积分
        int newPoints = (user.getPoints() == null ? 0 : user.getPoints()) + SIGN_IN_POINTS;
        user.setPoints(newPoints);
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);

        // 记录积分变动
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setType(1); // 1-签到
        record.setPoints(SIGN_IN_POINTS);
        record.setBalance(newPoints);
        record.setDescription("每日签到");
        record.setCreateTime(LocalDateTime.now());
        pointsRecordService.save(record);

        Map<String, Object> result = new HashMap<>();
        result.put("points", SIGN_IN_POINTS);
        result.put("totalPoints", newPoints);

        return Result.success(result);
    }

    @GetMapping("/sign-status")
    @Operation(summary = "获取签到状态")
    public Result<Map<String, Object>> getSignStatus(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        log.info("获取签到状态: userId={}", userId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        // 检查今日是否已签到
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String signKey = SIGN_IN_KEY_PREFIX + userId + ":" + today;
        Boolean signedToday = Boolean.TRUE.equals(redisTemplate.hasKey(signKey));

        // 获取本月签到记录
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());

        // 从数据库查询本月签到记录
        LambdaQueryWrapper<PointsRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsRecord::getUserId, userId)
                .eq(PointsRecord::getType, 1) // 签到类型
                .ge(PointsRecord::getCreateTime, firstDayOfMonth.atStartOfDay())
                .le(PointsRecord::getCreateTime, lastDayOfMonth.plusDays(1).atStartOfDay())
                .orderByAsc(PointsRecord::getCreateTime);

        List<PointsRecord> records = pointsRecordService.list(queryWrapper);

        // 提取签到日期
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        List<String> signedDates = records.stream()
                .map(r -> r.getCreateTime().toLocalDate().format(formatter))
                .distinct()
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("signedToday", signedToday);
        result.put("signDays", signedDates.size());
        result.put("signedDates", signedDates);

        return Result.success(result);
    }

    @GetMapping("/records")
    @Operation(summary = "获取积分记录")
    public Result<Map<String, Object>> getRecords(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取积分记录: userId={}, page={}, pageSize={}", userId, page, pageSize);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        Page<PointsRecord> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<PointsRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointsRecord::getUserId, userId)
                .orderByDesc(PointsRecord::getCreateTime);

        Page<PointsRecord> pageResult = pointsRecordService.page(pageParam, queryWrapper);

        // 转换记录格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Map<String, Object>> recordList = pageResult.getRecords().stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", record.getId());
                    map.put("title", getPointsTitle(record.getType()));
                    map.put("points", record.getPoints());
                    map.put("balance", record.getBalance());
                    map.put("createTime", record.getCreateTime().format(formatter));
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", recordList);
        result.put("total", pageResult.getTotal());
        result.put("hasMore", pageResult.getCurrent() < pageResult.getPages());

        return Result.success(result);
    }

    /**
     * 根据积分类型获取标题
     */
    private String getPointsTitle(Integer type) {
        if (type == null) {
            return "其他";
        }
        switch (type) {
            case 1:
                return "每日签到";
            case 2:
                return "购物奖励";
            case 3:
                return "订单评价";
            case 4:
                return "邀请好友";
            case 5:
                return "积分兑换";
            case 6:
                return "积分抵扣";
            default:
                return "其他";
        }
    }
}
