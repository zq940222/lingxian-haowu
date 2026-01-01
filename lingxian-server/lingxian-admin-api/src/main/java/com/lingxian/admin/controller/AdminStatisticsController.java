package com.lingxian.admin.controller;

import com.lingxian.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
@Tag(name = "管理后台-统计数据", description = "统计数据相关接口")
public class AdminStatisticsController {

    @GetMapping("/sales")
    @Operation(summary = "获取销售统计")
    public Result<Map<String, Object>> getSalesStats(
            @Parameter(description = "统计类型: day/week/month") @RequestParam(defaultValue = "day") String type,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        log.info("获取销售统计: type={}, startDate={}, endDate={}", type, startDate, endDate);

        Map<String, Object> stats = new HashMap<>();

        // 总览数据
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalSales", new BigDecimal("5896000.00"));
        overview.put("totalOrders", 58960);
        overview.put("avgOrderAmount", new BigDecimal("100.00"));
        overview.put("refundAmount", new BigDecimal("128000.00"));
        overview.put("netSales", new BigDecimal("5768000.00"));
        stats.put("overview", overview);

        // 对比数据
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("salesGrowth", 15.6); // 销售额增长率%
        comparison.put("ordersGrowth", 12.3);
        comparison.put("avgAmountGrowth", 2.8);
        stats.put("comparison", comparison);

        // 趋势数据
        List<Map<String, Object>> trends = new ArrayList<>();
        int days = "month".equals(type) ? 30 : ("week".equals(type) ? 7 : 7);
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", LocalDate.now().minusDays(i).toString());
            item.put("sales", new BigDecimal(10000 + (int)(Math.random() * 10000) + ".00"));
            item.put("orders", 100 + (int)(Math.random() * 100));
            item.put("refunds", (int)(Math.random() * 20));
            trends.add(item);
        }
        stats.put("trends", trends);

        // 分类销售
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        String[] categories = {"生鲜果蔬", "肉禽蛋品", "海鲜水产", "粮油调味", "休闲零食"};
        for (int i = 0; i < categories.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoryId", i + 1);
            item.put("categoryName", categories[i]);
            item.put("sales", new BigDecimal((5000 - i * 800) + ".00"));
            item.put("orders", 500 - i * 80);
            item.put("percentage", 30 - i * 5);
            categoryStats.add(item);
        }
        stats.put("categoryStats", categoryStats);

        return Result.success(stats);
    }

    @GetMapping("/orders")
    @Operation(summary = "获取订单统计")
    public Result<Map<String, Object>> getOrderStats(
            @Parameter(description = "统计类型: day/week/month") @RequestParam(defaultValue = "day") String type,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        log.info("获取订单统计: type={}, startDate={}, endDate={}", type, startDate, endDate);

        Map<String, Object> stats = new HashMap<>();

        // 总览数据
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalOrders", 58960);
        overview.put("pendingPayment", 120);
        overview.put("pendingDelivery", 350);
        overview.put("pendingReceive", 580);
        overview.put("completed", 55000);
        overview.put("cancelled", 2910);
        overview.put("refunding", 150);
        stats.put("overview", overview);

        // 趋势数据
        List<Map<String, Object>> trends = new ArrayList<>();
        int days = "month".equals(type) ? 30 : ("week".equals(type) ? 7 : 7);
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", LocalDate.now().minusDays(i).toString());
            item.put("total", 150 + (int)(Math.random() * 50));
            item.put("completed", 130 + (int)(Math.random() * 40));
            item.put("cancelled", (int)(Math.random() * 20));
            item.put("refunded", (int)(Math.random() * 10));
            trends.add(item);
        }
        stats.put("trends", trends);

        // 订单类型分布
        List<Map<String, Object>> typeStats = new ArrayList<>();
        Map<String, Object> normal = new HashMap<>();
        normal.put("type", "normal");
        normal.put("typeName", "普通订单");
        normal.put("count", 48000);
        normal.put("percentage", 81.4);
        typeStats.add(normal);
        Map<String, Object> group = new HashMap<>();
        group.put("type", "group");
        group.put("typeName", "拼团订单");
        group.put("count", 10960);
        group.put("percentage", 18.6);
        typeStats.add(group);
        stats.put("typeStats", typeStats);

        // 支付方式分布
        List<Map<String, Object>> payStats = new ArrayList<>();
        Map<String, Object> wechat = new HashMap<>();
        wechat.put("type", "wechat");
        wechat.put("typeName", "微信支付");
        wechat.put("count", 50000);
        wechat.put("percentage", 84.8);
        payStats.add(wechat);
        Map<String, Object> balance = new HashMap<>();
        balance.put("type", "balance");
        balance.put("typeName", "余额支付");
        balance.put("count", 8960);
        balance.put("percentage", 15.2);
        payStats.add(balance);
        stats.put("payStats", payStats);

        return Result.success(stats);
    }

    @GetMapping("/users")
    @Operation(summary = "获取用户统计")
    public Result<Map<String, Object>> getUserStats(
            @Parameter(description = "统计类型: day/week/month") @RequestParam(defaultValue = "day") String type,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        log.info("获取用户统计: type={}, startDate={}, endDate={}", type, startDate, endDate);

        Map<String, Object> stats = new HashMap<>();

        // 总览数据
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalUsers", 12580);
        overview.put("newUsersToday", 28);
        overview.put("activeUsersToday", 3560);
        overview.put("orderingUsers", 8500); // 下单用户数
        overview.put("repurchaseRate", 65.8); // 复购率%
        stats.put("overview", overview);

        // 用户增长趋势
        List<Map<String, Object>> growthTrends = new ArrayList<>();
        int days = "month".equals(type) ? 30 : ("week".equals(type) ? 7 : 7);
        int baseUsers = 12000;
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", LocalDate.now().minusDays(i).toString());
            item.put("newUsers", 20 + (int)(Math.random() * 20));
            item.put("activeUsers", 3000 + (int)(Math.random() * 1000));
            item.put("totalUsers", baseUsers + (days - i) * 20);
            growthTrends.add(item);
        }
        stats.put("growthTrends", growthTrends);

        // 用户分布
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("male", 4500);
        distribution.put("female", 7200);
        distribution.put("unknown", 880);
        stats.put("genderDistribution", distribution);

        // 用户活跃度分布
        List<Map<String, Object>> activityStats = new ArrayList<>();
        Map<String, Object> high = new HashMap<>();
        high.put("level", "high");
        high.put("levelName", "高活跃");
        high.put("count", 3500);
        high.put("percentage", 27.8);
        activityStats.add(high);
        Map<String, Object> medium = new HashMap<>();
        medium.put("level", "medium");
        medium.put("levelName", "中活跃");
        medium.put("count", 5000);
        medium.put("percentage", 39.7);
        activityStats.add(medium);
        Map<String, Object> low = new HashMap<>();
        low.put("level", "low");
        low.put("levelName", "低活跃");
        low.put("count", 4080);
        low.put("percentage", 32.5);
        activityStats.add(low);
        stats.put("activityStats", activityStats);

        return Result.success(stats);
    }

    @GetMapping("/merchants")
    @Operation(summary = "获取商户统计")
    public Result<Map<String, Object>> getMerchantStats(
            @Parameter(description = "统计类型: day/week/month") @RequestParam(defaultValue = "day") String type,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        log.info("获取商户统计: type={}, startDate={}, endDate={}", type, startDate, endDate);

        Map<String, Object> stats = new HashMap<>();

        // 总览数据
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalMerchants", 86);
        overview.put("activeMerchants", 78);
        overview.put("newMerchantsToday", 3);
        overview.put("pendingVerify", 5);
        overview.put("totalProducts", 3560);
        overview.put("avgProductsPerMerchant", 41);
        stats.put("overview", overview);

        // 商户销售排行
        List<Map<String, Object>> salesRanking = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("rank", i);
            item.put("merchantId", i);
            item.put("merchantName", "优鲜商家" + i);
            item.put("sales", new BigDecimal((100000 - i * 8000) + ".00"));
            item.put("orders", 1000 - i * 80);
            item.put("products", 50 - i * 3);
            salesRanking.add(item);
        }
        stats.put("salesRanking", salesRanking);

        // 商户分类分布
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        String[] categories = {"生鲜果蔬", "肉禽蛋品", "海鲜水产", "粮油调味", "休闲零食"};
        int[] counts = {25, 18, 15, 12, 16};
        for (int i = 0; i < categories.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoryId", i + 1);
            item.put("categoryName", categories[i]);
            item.put("count", counts[i]);
            item.put("percentage", counts[i] * 100.0 / 86);
            categoryStats.add(item);
        }
        stats.put("categoryStats", categoryStats);

        // 商户增长趋势
        List<Map<String, Object>> growthTrends = new ArrayList<>();
        int days = "month".equals(type) ? 30 : ("week".equals(type) ? 7 : 7);
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", LocalDate.now().minusDays(i).toString());
            item.put("newMerchants", (int)(Math.random() * 3));
            item.put("totalMerchants", 80 + (days - i));
            growthTrends.add(item);
        }
        stats.put("growthTrends", growthTrends);

        return Result.success(stats);
    }
}
