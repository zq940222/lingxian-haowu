package com.lingxian.merchant.controller;

import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @GetMapping("/pending")
    @Operation(summary = "获取待配送列表")
    public Result<PageResult<Map<String, Object>>> getPendingDeliveries(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return getDeliveryList(page, pageSize, "pending");
    }

    @GetMapping("/delivering")
    @Operation(summary = "获取配送中列表")
    public Result<PageResult<Map<String, Object>>> getDeliveringList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return getDeliveryList(page, pageSize, "delivering");
    }

    @GetMapping("/completed")
    @Operation(summary = "获取已完成列表")
    public Result<PageResult<Map<String, Object>>> getCompletedDeliveries(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return getDeliveryList(page, pageSize, "completed");
    }

    private Result<PageResult<Map<String, Object>>> getDeliveryList(Integer page, Integer pageSize, String status) {
        List<Map<String, Object>> orders = new ArrayList<>();

        String[] names = {"张三", "李四", "王五", "赵六", "钱七"};
        String[] phones = {"138****1234", "139****5678", "137****9012", "136****3456", "135****7890"};
        String[] addresses = {
            "北京市朝阳区建国路88号SOHO现代城A座1201",
            "上海市浦东新区世纪大道100号",
            "广州市天河区珠江新城华夏路30号",
            "深圳市南山区科技园南路1号",
            "杭州市西湖区文三路456号"
        };

        int total = status.equals("pending") ? 5 : (status.equals("delivering") ? 3 : 20);
        int count = Math.min(pageSize, total - (page - 1) * pageSize);

        for (int i = 0; i < count; i++) {
            Map<String, Object> order = new HashMap<>();
            int idx = i % 5;

            order.put("id", (page - 1) * pageSize + i + 1);
            order.put("orderNo", "DD" + System.currentTimeMillis() + String.format("%04d", i));
            order.put("status", status);
            order.put("receiverName", names[idx]);
            order.put("receiverPhone", phones[idx]);
            order.put("receiverAddress", addresses[idx]);
            order.put("latitude", 39.9042 + idx * 0.01);
            order.put("longitude", 116.4074 + idx * 0.01);
            order.put("totalQuantity", 2 + i % 5);
            order.put("payAmount", new BigDecimal("63.00").add(new BigDecimal(i * 10)));
            order.put("createTime", LocalDateTime.now().minusHours(i + 1).format(formatter));
            order.put("deliveryTime", i % 2 == 0 ? "今天 14:00-15:00" : null);

            orders.add(order);
        }

        PageResult<Map<String, Object>> pageResult = PageResult.of((long) total, (long) page, (long) pageSize, orders);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取配送详情")
    public Result<Map<String, Object>> getDeliveryDetail(@PathVariable Long id) {
        Map<String, Object> delivery = new HashMap<>();
        delivery.put("id", id);
        delivery.put("orderNo", "DD" + System.currentTimeMillis());
        delivery.put("status", "delivering");
        delivery.put("receiverName", "张三");
        delivery.put("receiverPhone", "13812345678");
        delivery.put("receiverAddress", "北京市朝阳区建国路88号SOHO现代城A座1201");
        delivery.put("latitude", 39.9042);
        delivery.put("longitude", 116.4074);
        delivery.put("totalQuantity", 3);
        delivery.put("totalAmount", new BigDecimal("98.00"));
        delivery.put("deliveryFee", new BigDecimal("5.00"));
        delivery.put("payAmount", new BigDecimal("103.00"));
        delivery.put("createTime", LocalDateTime.now().minusHours(2).format(formatter));
        delivery.put("deliveryStartTime", LocalDateTime.now().minusMinutes(30).format(formatter));
        delivery.put("deliveryTime", "今天 14:00-15:00");
        delivery.put("remark", "放门口即可");

        // 商品列表
        List<Map<String, Object>> products = new ArrayList<>();
        Map<String, Object> product1 = new HashMap<>();
        product1.put("id", 1);
        product1.put("name", "新鲜蔬菜套餐");
        product1.put("image", "https://via.placeholder.com/100x100");
        product1.put("price", new BigDecimal("28.00"));
        product1.put("quantity", 2);
        product1.put("spec", "500g/份");
        products.add(product1);

        Map<String, Object> product2 = new HashMap<>();
        product2.put("id", 2);
        product2.put("name", "精选水果拼盘");
        product2.put("image", "https://via.placeholder.com/100x100");
        product2.put("price", new BigDecimal("42.00"));
        product2.put("quantity", 1);
        product2.put("spec", "混合装");
        products.add(product2);
        delivery.put("products", products);

        return Result.success(delivery);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量开始配送")
    public Result<Void> batchDelivery(@RequestBody Map<String, List<Long>> body) {
        List<Long> orderIds = body.get("orderIds");
        log.info("批量配送: {}", orderIds);
        return Result.success();
    }
}
