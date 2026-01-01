package com.lingxian.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingxian.common.entity.Merchant;
import com.lingxian.common.entity.Order;
import com.lingxian.common.entity.User;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "管理后台-工作台", description = "管理后台工作台接口")
public class AdminDashboardController {

    private final UserService userService;
    private final MerchantService merchantService;
    private final ProductService productService;
    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "获取工作台数据")
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 今日时间范围
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 今日统计
        Map<String, Object> todayStats = new HashMap<>();

        // 今日订单数
        long todayOrderCount = orderService.count(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .le(Order::getCreateTime, todayEnd));
        todayStats.put("orderCount", todayOrderCount);

        // 今日销售额
        List<Order> todayOrders = orderService.list(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .le(Order::getCreateTime, todayEnd)
                .gt(Order::getStatus, 0)); // 已付款
        BigDecimal todaySalesAmount = todayOrders.stream()
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        todayStats.put("salesAmount", todaySalesAmount);

        // 今日新增用户
        long todayNewUsers = userService.count(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, todayStart)
                .le(User::getCreateTime, todayEnd));
        todayStats.put("newUsers", todayNewUsers);

        // 今日新增商户
        long todayNewMerchants = merchantService.count(new LambdaQueryWrapper<Merchant>()
                .ge(Merchant::getCreateTime, todayStart)
                .le(Merchant::getCreateTime, todayEnd));
        todayStats.put("newMerchants", todayNewMerchants);

        data.put("todayStats", todayStats);

        // 平台数据
        Map<String, Object> platformStats = new HashMap<>();
        platformStats.put("totalUsers", userService.count());
        platformStats.put("totalMerchants", merchantService.count());
        platformStats.put("totalProducts", productService.count());
        platformStats.put("totalOrders", orderService.count());
        data.put("platformStats", platformStats);

        return Result.success(data);
    }
}
