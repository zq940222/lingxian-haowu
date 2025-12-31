package com.lingxian.admin.controller;

import com.lingxian.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "管理后台-工作台", description = "管理后台工作台接口")
public class AdminDashboardController {

    @GetMapping
    @Operation(summary = "获取工作台数据")
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 今日统计
        Map<String, Object> todayStats = new HashMap<>();
        todayStats.put("orderCount", 156);
        todayStats.put("salesAmount", new BigDecimal("15680.50"));
        todayStats.put("newUsers", 28);
        todayStats.put("newMerchants", 3);
        data.put("todayStats", todayStats);

        // 平台数据
        Map<String, Object> platformStats = new HashMap<>();
        platformStats.put("totalUsers", 12580);
        platformStats.put("totalMerchants", 86);
        platformStats.put("totalProducts", 3560);
        platformStats.put("totalOrders", 58960);
        data.put("platformStats", platformStats);

        return Result.success(data);
    }
}
