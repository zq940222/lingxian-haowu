package com.lingxian.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.Merchant;
import com.lingxian.common.entity.Order;
import com.lingxian.common.entity.OrderItem;
import com.lingxian.common.entity.User;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.MerchantService;
import com.lingxian.common.service.OrderItemService;
import com.lingxian.common.service.OrderService;
import com.lingxian.common.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Tag(name = "管理后台-订单管理", description = "订单管理相关接口")
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final UserService userService;
    private final MerchantService merchantService;

    private static final String[] STATUS_NAMES = {"待付款", "待发货", "待收货", "已完成", "已取消", "退款中"};

    @GetMapping
    @Operation(summary = "获取订单列表")
    public Result<PageResult<Order>> getOrderList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "商户ID") @RequestParam(required = false) Long merchantId,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        log.info("获取订单列表: page={}, size={}, orderNo={}, status={}", page, size, orderNo, status);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (merchantId != null) {
            wrapper.eq(Order::getMerchantId, merchantId);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(Order::getCreateTime, LocalDateTime.parse(startTime.replace(" ", "T")));
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(Order::getCreateTime, LocalDateTime.parse(endTime.replace(" ", "T")));
        }
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> pageResult = orderService.page(new Page<>(page, size), wrapper);

        // 填充用户昵称、商户名称、状态名称和订单商品
        for (Order order : pageResult.getRecords()) {
            fillOrderInfo(order);
        }

        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public Result<Order> getOrderDetail(@PathVariable Long id) {
        log.info("获取订单详情: id={}", id);

        Order order = orderService.getById(id);
        if (order == null) {
            return Result.failed("订单不存在");
        }

        fillOrderInfo(order);

        return Result.success(order);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取订单统计")
    public Result<Map<String, Object>> getOrderStats(
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        log.info("获取订单统计: startTime={}, endTime={}", startTime, endTime);

        Map<String, Object> stats = new HashMap<>();

        // 总订单统计
        stats.put("totalCount", orderService.count());

        // 各状态订单数量
        stats.put("pendingPayCount", orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 0)));
        stats.put("pendingDeliveryCount", orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1)));
        stats.put("pendingReceiveCount", orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 2)));
        stats.put("completedCount", orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3)));
        stats.put("cancelledCount", orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 4)));
        stats.put("refundingCount", orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 5)));

        // 今日统计
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        long todayOrderCount = orderService.count(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .le(Order::getCreateTime, todayEnd));

        List<Order> todayOrders = orderService.list(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .le(Order::getCreateTime, todayEnd)
                .gt(Order::getStatus, 0)); // 已付款

        BigDecimal todaySalesAmount = todayOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgOrderAmount = todayOrderCount > 0
                ? todaySalesAmount.divide(BigDecimal.valueOf(todayOrderCount), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> todayStats = new HashMap<>();
        todayStats.put("orderCount", todayOrderCount);
        todayStats.put("salesAmount", todaySalesAmount);
        todayStats.put("avgOrderAmount", avgOrderAmount);
        stats.put("todayStats", todayStats);

        return Result.success(stats);
    }

    @GetMapping("/export")
    @Operation(summary = "导出订单")
    public Result<String> exportOrders(
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        log.info("导出订单: status={}, startTime={}, endTime={}", status, startTime, endTime);
        // TODO: 实现导出功能
        return Result.success("导出功能待实现");
    }

    private void fillOrderInfo(Order order) {
        // 填充用户信息
        if (order.getUserId() != null) {
            User user = userService.getById(order.getUserId());
            if (user != null) {
                order.setUserNickname(user.getNickname());
                order.setUserPhone(user.getPhone());
            }
        }

        // 填充商户名称
        if (order.getMerchantId() != null) {
            Merchant merchant = merchantService.getById(order.getMerchantId());
            if (merchant != null) {
                order.setMerchantName(merchant.getName());
            }
        }

        // 填充状态名称
        if (order.getStatus() != null && order.getStatus() >= 0 && order.getStatus() < STATUS_NAMES.length) {
            order.setStatusName(STATUS_NAMES[order.getStatus()]);
        }

        // 获取订单商品
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getId())
        );
        order.setItems(items);
    }
}
