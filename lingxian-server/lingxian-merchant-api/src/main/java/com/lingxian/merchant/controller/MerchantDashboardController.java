package com.lingxian.merchant.controller;

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
@RequestMapping("/merchant/dashboard")
@RequiredArgsConstructor
@Tag(name = "商户端-工作台", description = "商户工作台接口")
public class MerchantDashboardController {

    @GetMapping
    @Operation(summary = "获取工作台数据")
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 今日统计
        Map<String, Object> todayStats = new HashMap<>();
        todayStats.put("orderCount", 28);
        todayStats.put("salesAmount", new BigDecimal("2680.50"));
        todayStats.put("newCustomers", 5);
        todayStats.put("avgOrderAmount", new BigDecimal("95.73"));
        data.put("todayStats", todayStats);

        // 待处理
        Map<String, Object> pending = new HashMap<>();
        pending.put("pendingOrders", 3);
        pending.put("pendingDelivery", 5);
        pending.put("lowStock", 2);
        pending.put("refundApply", 1);
        data.put("pending", pending);

        // 近7天趋势
        List<Map<String, Object>> trends = new ArrayList<>();
        String[] days = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        Random random = new Random();
        for (String day : days) {
            Map<String, Object> trend = new HashMap<>();
            trend.put("date", day);
            trend.put("orderCount", 20 + random.nextInt(20));
            trend.put("salesAmount", 1500 + random.nextInt(2000));
            trends.add(trend);
        }
        data.put("trends", trends);

        // 热销商品
        List<Map<String, Object>> hotProducts = new ArrayList<>();
        String[] productNames = {"新鲜蔬菜套餐", "精选水果拼盘", "有机鸡蛋", "进口牛肉", "海鲜礼盒"};
        for (int i = 0; i < 5; i++) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", i + 1);
            product.put("name", productNames[i]);
            product.put("image", "https://via.placeholder.com/80x80");
            product.put("sales", 100 - i * 15);
            product.put("amount", new BigDecimal(2800 - i * 400));
            hotProducts.add(product);
        }
        data.put("hotProducts", hotProducts);

        return Result.success(data);
    }

    @GetMapping("/today")
    @Operation(summary = "获取今日统计")
    public Result<Map<String, Object>> getTodayStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("orderCount", 28);
        stats.put("salesAmount", new BigDecimal("2680.50"));
        stats.put("newCustomers", 5);
        stats.put("avgOrderAmount", new BigDecimal("95.73"));
        stats.put("compareYesterday", new BigDecimal("12.5")); // 较昨日增长百分比
        return Result.success(stats);
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待处理数量")
    public Result<Map<String, Object>> getPendingCount() {
        Map<String, Object> pending = new HashMap<>();
        pending.put("pendingOrders", 3);
        pending.put("pendingDelivery", 5);
        pending.put("lowStock", 2);
        pending.put("refundApply", 1);
        return Result.success(pending);
    }
}
