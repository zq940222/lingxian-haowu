package com.lingxian.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lingxian.common.entity.*;
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
import java.util.stream.Collectors;

/**
 * 用户订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/orders")
@RequiredArgsConstructor
@Tag(name = "用户端-订单", description = "用户订单管理接口")
public class UserOrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final AddressService addressService;
    private final CartService cartService;
    private final MerchantService merchantService;
    private final ImageUrlUtil imageUrlUtil;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    @Operation(summary = "获取订单列表")
    public Result<Map<String, Object>> getOrderList(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        log.info("获取订单列表: userId={}, page={}, pageSize={}, status={}", userId, page, pageSize, status);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        Page<Order> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId)
                .eq(Order::getDeleted, 0);

        // 状态筛选: 0-全部, 1-待付款, 2-待发货, 3-待收货, 4-待评价, 5-已完成
        if (status != null && status > 0) {
            queryWrapper.eq(Order::getStatus, status);
        }

        queryWrapper.orderByDesc(Order::getCreateTime);

        Page<Order> pageResult = orderService.page(pageParam, queryWrapper);

        // 查询订单商品
        List<Order> orders = pageResult.getRecords();
        if (!orders.isEmpty()) {
            List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
            LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
            itemQuery.in(OrderItem::getOrderId, orderIds);
            List<OrderItem> allItems = orderItemService.list(itemQuery);

            Map<Long, List<OrderItem>> itemMap = allItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getOrderId));

            orders.forEach(order -> {
                order.setItems(itemMap.getOrDefault(order.getId(), new ArrayList<>()));
                order.setStatusName(getStatusName(order.getStatus()));
            });
        }

        // 转换响应格式
        List<Map<String, Object>> records = orders.stream().map(this::convertOrder).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", pageResult.getTotal());
        result.put("pages", pageResult.getPages());
        result.put("current", pageResult.getCurrent());

        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public Result<Map<String, Object>> getOrderDetail(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        log.info("获取订单详情: userId={}, orderId={}", userId, id);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.failed("订单不存在");
        }

        // 查询订单商品
        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItem::getOrderId, id);
        List<OrderItem> items = orderItemService.list(itemQuery);
        order.setItems(items);
        order.setStatusName(getStatusName(order.getStatus()));

        return Result.success(convertOrder(order));
    }

    @PostMapping
    @Operation(summary = "创建订单（支持多商户分单）")
    public Result<Map<String, Object>> createOrder(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        log.info("创建订单: userId={}, body={}", userId, body);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> ordersData = (List<Map<String, Object>>) body.get("orders");

        if (ordersData == null || ordersData.isEmpty()) {
            return Result.failed("订单数据不能为空");
        }

        List<Long> orderIds = new ArrayList<>();
        List<String> orderNos = new ArrayList<>();
        java.math.BigDecimal totalPayAmount = java.math.BigDecimal.ZERO;

        for (Map<String, Object> orderData : ordersData) {
            Long merchantId = orderData.get("merchantId") != null ?
                    Long.parseLong(orderData.get("merchantId").toString()) : null;
            Long addressId = orderData.get("addressId") != null ?
                    Long.parseLong(orderData.get("addressId").toString()) : null;
            String deliveryTime = (String) orderData.get("deliveryTime");
            String remark = (String) orderData.get("remark");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> products = (List<Map<String, Object>>) orderData.get("products");

            if (addressId == null) {
                return Result.failed("收货地址不能为空");
            }

            if (products == null || products.isEmpty()) {
                return Result.failed("商品不能为空");
            }

            // 验证收货地址
            Address address = addressService.getById(addressId);
            if (address == null || !address.getUserId().equals(userId)) {
                return Result.failed("收货地址不存在");
            }

            // 查询商户信息
            Merchant merchant = merchantId != null ? merchantService.getById(merchantId) : null;

            // 计算订单金额并创建订单项
            java.math.BigDecimal orderTotalAmount = java.math.BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();
            List<Long> productIds = new ArrayList<>();

            for (Map<String, Object> productData : products) {
                Long productId = Long.parseLong(productData.get("productId").toString());
                Integer quantity = Integer.parseInt(productData.get("quantity").toString());
                productIds.add(productId);

                Product product = productService.getById(productId);
                if (product == null) {
                    return Result.failed("商品不存在: " + productId);
                }
                if (product.getStatus() != 1) {
                    return Result.failed("商品已下架: " + product.getName());
                }
                if (product.getStock() < quantity) {
                    return Result.failed("商品库存不足: " + product.getName());
                }

                // 计算商品小计
                java.math.BigDecimal itemTotal = product.getPrice()
                        .multiply(java.math.BigDecimal.valueOf(quantity));
                orderTotalAmount = orderTotalAmount.add(itemTotal);

                // 创建订单项
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(productId);
                orderItem.setProductName(product.getName());
                orderItem.setProductImage(product.getImage());
                orderItem.setPrice(product.getPrice());
                orderItem.setQuantity(quantity);
                orderItem.setTotalAmount(itemTotal);
                orderItem.setCreateTime(LocalDateTime.now());
                orderItems.add(orderItem);
            }

            // 配送费（暂设为5元/单）
            java.math.BigDecimal freightAmount = java.math.BigDecimal.valueOf(5);
            java.math.BigDecimal payAmount = orderTotalAmount.add(freightAmount);

            // 生成订单号
            String orderNo = generateOrderNo();

            // 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setMerchantId(merchantId);
            order.setOrderType(1); // 普通订单
            order.setStatus(1); // 待付款
            order.setTotalAmount(orderTotalAmount);
            order.setFreightAmount(freightAmount);
            order.setDiscountAmount(java.math.BigDecimal.ZERO);
            order.setPayAmount(payAmount);
            order.setRemark(remark);

            // 收货信息
            order.setReceiverName(address.getName());
            order.setReceiverPhone(address.getPhone());
            order.setReceiverProvince(address.getProvince());
            order.setReceiverCity(address.getCity());
            order.setReceiverDistrict(address.getDistrict());
            order.setReceiverAddress(address.getAddress());

            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            order.setDeleted(0);

            orderService.save(order);

            // 保存订单项
            for (OrderItem item : orderItems) {
                item.setOrderId(order.getId());
                item.setOrderNo(orderNo);
            }
            orderItemService.saveBatch(orderItems);

            // 扣减库存
            for (Map<String, Object> productData : products) {
                Long productId = Long.parseLong(productData.get("productId").toString());
                Integer quantity = Integer.parseInt(productData.get("quantity").toString());
                productService.update(new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, productId)
                        .setSql("stock = stock - " + quantity));
            }

            // 删除购物车中对应的商品
            LambdaUpdateWrapper<Cart> cartUpdate = new LambdaUpdateWrapper<>();
            cartUpdate.eq(Cart::getUserId, userId)
                    .in(Cart::getProductId, productIds)
                    .eq(Cart::getDeleted, 0)
                    .set(Cart::getDeleted, 1)
                    .set(Cart::getUpdateTime, LocalDateTime.now());
            cartService.update(cartUpdate);

            orderIds.add(order.getId());
            orderNos.add(orderNo);
            totalPayAmount = totalPayAmount.add(payAmount);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderIds", orderIds);
        result.put("orderNos", orderNos);
        result.put("totalPayAmount", totalPayAmount);
        result.put("orderCount", orderIds.size());

        return Result.success(result);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "LX" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public Result<Void> cancelOrder(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        log.info("取消订单: userId={}, orderId={}", userId, id);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.failed("订单不存在");
        }

        // 只有待付款订单可以取消
        if (order.getStatus() != 1) {
            return Result.failed("当前订单状态不允许取消");
        }

        order.setStatus(6); // 已取消
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(body != null ? body.get("reason") : "用户取消");
        order.setUpdateTime(LocalDateTime.now());
        orderService.updateById(order);

        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认收货")
    public Result<Void> confirmOrder(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        log.info("确认收货: userId={}, orderId={}", userId, id);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.failed("订单不存在");
        }

        // 只有待收货订单可以确认收货
        if (order.getStatus() != 3) {
            return Result.failed("当前订单状态不允许确认收货");
        }

        order.setStatus(4); // 待评价
        order.setReceiveTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderService.updateById(order);

        return Result.success();
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "支付订单")
    public Result<Map<String, Object>> payOrder(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        log.info("支付订单: userId={}, orderId={}", userId, id);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.failed("订单不存在");
        }

        // 只有待付款订单可以支付
        if (order.getStatus() != 1) {
            return Result.failed("当前订单状态不允许支付");
        }

        // TODO: 调用微信支付接口
        // 这里模拟直接支付成功
        order.setStatus(2); // 待发货
        order.setPayTime(LocalDateTime.now());
        order.setPayType(1); // 微信支付
        order.setUpdateTime(LocalDateTime.now());
        orderService.updateById(order);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);

        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单")
    public Result<Void> deleteOrder(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        log.info("删除订单: userId={}, orderId={}", userId, id);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.failed("订单不存在");
        }

        // 只有已完成或已取消的订单可以删除
        if (order.getStatus() != 5 && order.getStatus() != 6) {
            return Result.failed("当前订单状态不允许删除");
        }

        order.setDeleted(1);
        order.setUpdateTime(LocalDateTime.now());
        orderService.updateById(order);

        return Result.success();
    }

    @PostMapping("/{id}/comment")
    @Operation(summary = "评价订单")
    public Result<Void> commentOrder(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        log.info("评价订单: userId={}, orderId={}, body={}", userId, id, body);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.failed("订单不存在");
        }

        // 只有待评价订单可以评价
        if (order.getStatus() != 4) {
            return Result.failed("当前订单状态不允许评价");
        }

        // TODO: 保存评价

        order.setStatus(5); // 已完成
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderService.updateById(order);

        return Result.success();
    }

    @PostMapping("/{id}/reorder")
    @Operation(summary = "再来一单")
    public Result<Map<String, Object>> reorder(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id) {
        log.info("再来一单: userId={}, orderId={}", userId, id);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.failed("订单不存在");
        }

        // TODO: 将订单商品加入购物车

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);

        return Result.success(result);
    }

    /**
     * 转换订单为响应格式
     */
    private Map<String, Object> convertOrder(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("orderNo", order.getOrderNo());
        map.put("status", order.getStatus());
        map.put("statusName", getStatusName(order.getStatus()));
        map.put("totalAmount", order.getTotalAmount());
        map.put("discountAmount", order.getDiscountAmount());
        map.put("freightAmount", order.getFreightAmount());
        map.put("payAmount", order.getPayAmount());
        map.put("payType", order.getPayType());
        map.put("orderType", order.getOrderType());
        map.put("remark", order.getRemark());
        map.put("createTime", order.getCreateTime() != null ? order.getCreateTime().format(FORMATTER) : null);
        map.put("payTime", order.getPayTime() != null ? order.getPayTime().format(FORMATTER) : null);
        map.put("deliveryTime", order.getDeliveryTime() != null ? order.getDeliveryTime().format(FORMATTER) : null);
        map.put("receiveTime", order.getReceiveTime() != null ? order.getReceiveTime().format(FORMATTER) : null);

        // 收货信息
        map.put("receiverName", order.getReceiverName());
        map.put("receiverPhone", order.getReceiverPhone());
        map.put("receiverAddress", order.getReceiverProvince() + order.getReceiverCity() +
                order.getReceiverDistrict() + order.getReceiverAddress());

        // 物流信息
        map.put("deliveryCompany", order.getDeliveryCompany());
        map.put("deliveryNo", order.getDeliveryNo());

        // 订单商品
        List<Map<String, Object>> items = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getId());
                itemMap.put("productId", item.getProductId());
                itemMap.put("productName", item.getProductName());
                itemMap.put("productImage", imageUrlUtil.generateUrl(item.getProductImage()));
                itemMap.put("skuId", item.getSkuId());
                itemMap.put("skuName", item.getSkuName());
                itemMap.put("price", item.getPrice());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("totalAmount", item.getTotalAmount());
                items.add(itemMap);
            }
        }
        map.put("items", items);
        map.put("productCount", items.size());

        return map;
    }

    /**
     * 获取订单状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "待付款";
            case 2:
                return "待发货";
            case 3:
                return "待收货";
            case 4:
                return "待评价";
            case 5:
                return "已完成";
            case 6:
                return "已取消";
            case 7:
                return "已退款";
            default:
                return "未知";
        }
    }
}
