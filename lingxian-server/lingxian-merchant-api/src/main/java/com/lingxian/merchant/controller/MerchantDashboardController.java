package com.lingxian.merchant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingxian.common.entity.*;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.*;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/merchant/dashboard")
@RequiredArgsConstructor
@Tag(name = "商户端-工作台", description = "商户工作台接口")
public class MerchantDashboardController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final RefundService refundService;
    private final MerchantUserService merchantUserService;
    private final MerchantService merchantService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping
    @Operation(summary = "获取工作台数据")
    public Result<Map<String, Object>> getDashboardData(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {

        Long merchantId = getMerchantId(userId);
        if (merchantId == null) {
            return Result.failed("请先完成商户入驻");
        }

        Map<String, Object> data = new HashMap<>();

        // 今日统计
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Map<String, Object> todayStats = getTodayStatsData(merchantId, todayStart, todayEnd);
        data.put("todayStats", todayStats);

        // 待处理数量
        Map<String, Object> pending = getPendingData(merchantId);
        data.put("pending", pending);

        // 近7天趋势
        List<Map<String, Object>> trends = get7DaysTrends(merchantId);
        data.put("trends", trends);

        // 热销商品
        List<Map<String, Object>> hotProducts = getHotProducts(merchantId);
        data.put("hotProducts", hotProducts);

        return Result.success(data);
    }

    @GetMapping("/today")
    @Operation(summary = "获取今日统计")
    public Result<Map<String, Object>> getTodayStats(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {

        Long merchantId = getMerchantId(userId);
        if (merchantId == null) {
            return Result.failed("请先完成商户入驻");
        }

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime yesterdayStart = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime yesterdayEnd = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX);

        Map<String, Object> todayStats = getTodayStatsData(merchantId, todayStart, todayEnd);

        // 昨日销售额用于计算环比
        List<Order> yesterdayOrders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId)
                .ge(Order::getStatus, 2) // 已支付
                .ge(Order::getCreateTime, yesterdayStart)
                .le(Order::getCreateTime, yesterdayEnd));

        BigDecimal yesterdaySales = yesterdayOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal todaySales = (BigDecimal) todayStats.get("salesAmount");
        BigDecimal compareYesterday = BigDecimal.ZERO;
        if (yesterdaySales.compareTo(BigDecimal.ZERO) > 0) {
            compareYesterday = todaySales.subtract(yesterdaySales)
                    .divide(yesterdaySales, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        todayStats.put("compareYesterday", compareYesterday);

        return Result.success(todayStats);
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待处理数量")
    public Result<Map<String, Object>> getPendingCount(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {

        Long merchantId = getMerchantId(userId);
        if (merchantId == null) {
            return Result.failed("请先完成商户入驻");
        }

        Map<String, Object> pending = getPendingData(merchantId);
        return Result.success(pending);
    }

    /**
     * 获取商户ID
     */
    private Long getMerchantId(Long userId) {
        if (userId == null) {
            return null;
        }
        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return null;
        }
        return merchantUser.getMerchantId();
    }

    /**
     * 获取今日统计数据
     */
    private Map<String, Object> getTodayStatsData(Long merchantId, LocalDateTime todayStart, LocalDateTime todayEnd) {
        Map<String, Object> stats = new HashMap<>();

        // 今日订单（已支付及之后状态）
        List<Order> todayOrders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId)
                .ge(Order::getStatus, 2) // 已支付
                .ge(Order::getCreateTime, todayStart)
                .le(Order::getCreateTime, todayEnd));

        int orderCount = todayOrders.size();
        BigDecimal salesAmount = todayOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 今日新客户数（根据用户首次下单时间）
        Set<Long> todayUserIds = todayOrders.stream()
                .map(Order::getUserId)
                .collect(Collectors.toSet());
        int newCustomers = 0;
        for (Long uid : todayUserIds) {
            Order firstOrder = orderService.getOne(new LambdaQueryWrapper<Order>()
                    .eq(Order::getMerchantId, merchantId)
                    .eq(Order::getUserId, uid)
                    .ge(Order::getStatus, 2)
                    .orderByAsc(Order::getCreateTime)
                    .last("LIMIT 1"));
            if (firstOrder != null &&
                    !firstOrder.getCreateTime().isBefore(todayStart) &&
                    !firstOrder.getCreateTime().isAfter(todayEnd)) {
                newCustomers++;
            }
        }

        // 平均客单价
        BigDecimal avgOrderAmount = orderCount > 0
                ? salesAmount.divide(new BigDecimal(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        stats.put("orderCount", orderCount);
        stats.put("salesAmount", salesAmount.setScale(2, RoundingMode.HALF_UP));
        stats.put("newCustomers", newCustomers);
        stats.put("avgOrderAmount", avgOrderAmount);

        return stats;
    }

    /**
     * 获取待处理数量
     */
    private Map<String, Object> getPendingData(Long merchantId) {
        Map<String, Object> pending = new HashMap<>();

        // 待接单数量（状态2：已支付待接单）
        long pendingOrders = orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId)
                .eq(Order::getStatus, 2));

        // 待配送数量（状态3：已接单待配送）
        long pendingDelivery = orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId)
                .eq(Order::getStatus, 3));

        // 库存不足商品数量（库存 <= 10）
        long lowStock = productService.count(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .eq(Product::getStatus, 1) // 上架中
                .le(Product::getStock, 10));

        // 待处理退款数量（状态1：待审核）
        long refundApply = refundService.count(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getMerchantId, merchantId)
                .eq(Refund::getStatus, 1));

        pending.put("pendingOrders", pendingOrders);
        pending.put("pendingDelivery", pendingDelivery);
        pending.put("lowStock", lowStock);
        pending.put("refundApply", refundApply);

        return pending;
    }

    /**
     * 获取近7天趋势
     */
    private List<Map<String, Object>> get7DaysTrends(Long merchantId) {
        List<Map<String, Object>> trends = new ArrayList<>();
        String[] weekdays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            List<Order> dayOrders = orderService.list(new LambdaQueryWrapper<Order>()
                    .eq(Order::getMerchantId, merchantId)
                    .ge(Order::getStatus, 2)
                    .ge(Order::getCreateTime, dayStart)
                    .le(Order::getCreateTime, dayEnd));

            int orderCount = dayOrders.size();
            BigDecimal salesAmount = dayOrders.stream()
                    .map(Order::getPayAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> trend = new HashMap<>();
            trend.put("date", weekdays[date.getDayOfWeek().getValue() % 7]);
            trend.put("orderCount", orderCount);
            trend.put("salesAmount", salesAmount.setScale(2, RoundingMode.HALF_UP));
            trends.add(trend);
        }

        return trends;
    }

    /**
     * 获取热销商品
     */
    private List<Map<String, Object>> getHotProducts(Long merchantId) {
        // 获取商户的所有商品，按销量排序
        List<Product> products = productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .eq(Product::getStatus, 1) // 上架中
                .orderByDesc(Product::getSalesCount)
                .last("LIMIT 5"));

        List<Map<String, Object>> hotProducts = new ArrayList<>();
        for (Product product : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", product.getId());
            item.put("name", product.getName());
            item.put("image", imageUrlUtil.generateUrl(product.getImage()));
            item.put("sales", product.getSalesCount());
            // 计算销售金额（简化为销量 * 价格）
            BigDecimal amount = product.getPrice().multiply(new BigDecimal(product.getSalesCount()));
            item.put("amount", amount.setScale(2, RoundingMode.HALF_UP));
            hotProducts.add(item);
        }

        return hotProducts;
    }
}
