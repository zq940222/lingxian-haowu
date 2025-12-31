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
@RequestMapping("/merchant/orders")
@RequiredArgsConstructor
@Tag(name = "商户端-订单管理", description = "商户订单管理接口")
public class MerchantOrderController {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    @Operation(summary = "获取订单列表")
    public Result<PageResult<Map<String, Object>>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {

        List<Map<String, Object>> orders = new ArrayList<>();

        // 模拟订单数据
        String[] names = {"张三", "李四", "王五", "赵六", "钱七"};
        String[] phones = {"138****1234", "139****5678", "137****9012", "136****3456", "135****7890"};
        String[] addresses = {
            "北京市朝阳区建国路88号SOHO现代城A座1201",
            "上海市浦东新区世纪大道100号环球金融中心",
            "广州市天河区珠江新城华夏路30号",
            "深圳市南山区科技园南路1号",
            "杭州市西湖区文三路456号"
        };

        for (int i = 1; i <= pageSize; i++) {
            Map<String, Object> order = new HashMap<>();
            int idx = (i - 1) % 5;
            int orderStatus = status != null ? status : (i % 5);

            order.put("id", (page - 1) * pageSize + i);
            order.put("orderNo", "DD" + System.currentTimeMillis() + String.format("%04d", i));
            order.put("status", orderStatus);
            order.put("receiverName", names[idx]);
            order.put("receiverPhone", phones[idx]);
            order.put("receiverAddress", addresses[idx]);
            order.put("totalQuantity", 2 + i % 5);
            order.put("productAmount", new BigDecimal("58.00").add(new BigDecimal(i * 10)));
            order.put("deliveryFee", new BigDecimal("5.00"));
            order.put("payAmount", new BigDecimal("63.00").add(new BigDecimal(i * 10)));
            order.put("createTime", LocalDateTime.now().minusHours(i).format(formatter));
            order.put("payTime", orderStatus > 0 ? LocalDateTime.now().minusHours(i).plusMinutes(5).format(formatter) : null);
            order.put("remark", i % 3 == 0 ? "少放辣，多放葱" : "");

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
            product2.put("price", new BigDecimal("35.00"));
            product2.put("quantity", 1);
            product2.put("spec", "混合装");
            products.add(product2);

            order.put("products", products);
            orders.add(order);
        }

        PageResult<Map<String, Object>> pageResult = PageResult.of(50L, (long) page, (long) pageSize, orders);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", id);
        order.put("orderNo", "DD" + System.currentTimeMillis());
        order.put("status", 1);
        order.put("receiverName", "张三");
        order.put("receiverPhone", "13812345678");
        order.put("receiverAddress", "北京市朝阳区建国路88号SOHO现代城A座1201");
        order.put("latitude", 39.9042);
        order.put("longitude", 116.4074);
        order.put("totalQuantity", 3);
        order.put("totalAmount", new BigDecimal("98.00"));
        order.put("deliveryFee", new BigDecimal("5.00"));
        order.put("discountAmount", new BigDecimal("10.00"));
        order.put("payAmount", new BigDecimal("93.00"));
        order.put("createTime", LocalDateTime.now().minusHours(2).format(formatter));
        order.put("payTime", LocalDateTime.now().minusHours(1).format(formatter));
        order.put("deliveryTime", "今天 14:00-15:00");
        order.put("remark", "少放辣");

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
        order.put("products", products);

        return Result.success(order);
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "接单")
    public Result<Void> acceptOrder(@PathVariable Long id) {
        log.info("接单: {}", id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "拒单")
    public Result<Void> rejectOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        log.info("拒单: {}, 原因: {}", id, reason);
        return Result.success();
    }

    @PutMapping("/{id}/delivery")
    @Operation(summary = "开始配送")
    public Result<Void> startDelivery(@PathVariable Long id) {
        log.info("开始配送: {}", id);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "完成配送")
    public Result<Void> completeDelivery(@PathVariable Long id) {
        log.info("完成配送: {}", id);
        return Result.success();
    }
}
