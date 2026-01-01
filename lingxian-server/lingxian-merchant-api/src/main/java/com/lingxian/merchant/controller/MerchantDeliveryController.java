package com.lingxian.merchant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.*;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.*;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/merchant/deliveries")
@RequiredArgsConstructor
@Tag(name = "商户端-配送管理", description = "商户配送管理接口")
public class MerchantDeliveryController {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final MerchantUserService merchantUserService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping("/pending")
    @Operation(summary = "获取待配送列表")
    public Result<PageResult<Map<String, Object>>> getPendingDeliveries(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return getDeliveryList(userId, page, pageSize, 3); // 状态3：已接单待配送
    }

    @GetMapping("/delivering")
    @Operation(summary = "获取配送中列表")
    public Result<PageResult<Map<String, Object>>> getDeliveringList(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return getDeliveryList(userId, page, pageSize, 4); // 状态4：配送中
    }

    @GetMapping("/completed")
    @Operation(summary = "获取已完成列表")
    public Result<PageResult<Map<String, Object>>> getCompletedDeliveries(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return getDeliveryList(userId, page, pageSize, 5); // 状态5：已完成
    }

    private Result<PageResult<Map<String, Object>>> getDeliveryList(Long userId, Integer page, Integer pageSize, Integer status) {
        Long merchantId = getMerchantId(userId);
        if (merchantId == null) {
            return Result.failed("请先完成商户入驻");
        }

        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId)
                .eq(Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);

        // 分页查询
        Page<Order> pageData = orderService.page(new Page<>(page, pageSize), wrapper);

        // 转换数据
        List<Map<String, Object>> orders = new ArrayList<>();
        for (Order order : pageData.getRecords()) {
            Map<String, Object> orderMap = buildDeliveryMap(order);
            orders.add(orderMap);
        }

        PageResult<Map<String, Object>> pageResult = PageResult.of(
                pageData.getTotal(),
                pageData.getCurrent(),
                pageData.getSize(),
                orders);

        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取配送详情")
    public Result<Map<String, Object>> getDeliveryDetail(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long id) {

        Long merchantId = getMerchantId(userId);
        if (merchantId == null) {
            return Result.failed("请先完成商户入驻");
        }

        Order order = orderService.getById(id);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            return Result.failed("订单不存在");
        }

        Map<String, Object> delivery = buildDeliveryMap(order);
        return Result.success(delivery);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量开始配送")
    public Result<Void> batchDelivery(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestBody Map<String, List<Long>> body) {

        Long merchantId = getMerchantId(userId);
        if (merchantId == null) {
            return Result.failed("请先完成商户入驻");
        }

        List<Long> orderIds = body.get("orderIds");
        if (orderIds == null || orderIds.isEmpty()) {
            return Result.failed("请选择要配送的订单");
        }

        LocalDateTime now = LocalDateTime.now();
        int successCount = 0;

        for (Long orderId : orderIds) {
            Order order = orderService.getById(orderId);
            if (order != null && order.getMerchantId().equals(merchantId) && order.getStatus() == 3) {
                order.setStatus(4); // 配送中
                order.setDeliveryTime(now);
                order.setUpdateTime(now);
                orderService.updateById(order);
                successCount++;
            }
        }

        log.info("批量配送: merchantId={}, orderIds={}, successCount={}", merchantId, orderIds, successCount);
        return Result.success();
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
     * 构建配送信息Map
     */
    private Map<String, Object> buildDeliveryMap(Order order) {
        Map<String, Object> delivery = new HashMap<>();
        delivery.put("id", order.getId());
        delivery.put("orderNo", order.getOrderNo());
        delivery.put("status", getStatusString(order.getStatus()));
        delivery.put("receiverName", order.getReceiverName());
        delivery.put("receiverPhone", maskPhone(order.getReceiverPhone()));
        delivery.put("receiverAddress", buildFullAddress(order));
        delivery.put("payAmount", order.getPayAmount());
        delivery.put("remark", order.getRemark());

        // 时间格式化
        delivery.put("createTime", order.getCreateTime() != null ? order.getCreateTime().format(formatter) : null);
        delivery.put("deliveryStartTime", order.getDeliveryTime() != null ? order.getDeliveryTime().format(formatter) : null);

        // 获取订单商品
        List<OrderItem> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));

        List<Map<String, Object>> products = new ArrayList<>();
        int totalQuantity = 0;
        for (OrderItem item : items) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", item.getProductId());
            product.put("name", item.getProductName());
            product.put("image", imageUrlUtil.generateUrl(item.getProductImage()));
            product.put("price", item.getPrice());
            product.put("quantity", item.getQuantity());
            product.put("spec", item.getSkuName());
            products.add(product);
            totalQuantity += item.getQuantity();
        }
        delivery.put("products", products);
        delivery.put("totalQuantity", totalQuantity);

        return delivery;
    }

    /**
     * 获取配送状态字符串
     */
    private String getStatusString(Integer status) {
        if (status == null) return "unknown";
        switch (status) {
            case 3: return "pending";
            case 4: return "delivering";
            case 5: return "completed";
            default: return "unknown";
        }
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 构建完整地址
     */
    private String buildFullAddress(Order order) {
        StringBuilder sb = new StringBuilder();
        if (order.getReceiverProvince() != null) {
            sb.append(order.getReceiverProvince());
        }
        if (order.getReceiverCity() != null) {
            sb.append(order.getReceiverCity());
        }
        if (order.getReceiverDistrict() != null) {
            sb.append(order.getReceiverDistrict());
        }
        if (order.getReceiverAddress() != null) {
            sb.append(order.getReceiverAddress());
        }
        return sb.toString();
    }
}
