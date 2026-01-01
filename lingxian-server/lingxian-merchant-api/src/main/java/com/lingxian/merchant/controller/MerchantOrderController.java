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
@RequestMapping("/merchant/orders")
@RequiredArgsConstructor
@Tag(name = "商户端-订单管理", description = "商户订单管理接口")
public class MerchantOrderController {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final UserService userService;
    private final MerchantUserService merchantUserService;
    private final ProductCommentService productCommentService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping
    @Operation(summary = "获取订单列表")
    public Result<PageResult<Map<String, Object>>> getOrderList(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {

        Long merchantId = getMerchantId(userId);
        if (merchantId == null) {
            return Result.failed("请先完成商户入驻");
        }

        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getMerchantId, merchantId)
                .orderByDesc(Order::getCreateTime);

        if (status != null) {
            // status=4 表示已完成（包含待评价4和已完成5）
            if (status == 4) {
                wrapper.in(Order::getStatus, 4, 5);
            } else {
                wrapper.eq(Order::getStatus, status);
            }
        }

        // 分页查询
        Page<Order> pageData = orderService.page(new Page<>(page, pageSize), wrapper);

        // 转换数据
        List<Map<String, Object>> orders = new ArrayList<>();
        for (Order order : pageData.getRecords()) {
            Map<String, Object> orderMap = buildOrderMap(order);
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
    @Operation(summary = "获取订单详情")
    public Result<Map<String, Object>> getOrderDetail(
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

        Map<String, Object> orderMap = buildOrderMap(order);
        return Result.success(orderMap);
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "接单（发货）")
    public Result<Void> acceptOrder(
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

        // 状态2=待发货，商户接单后变为状态3=待收货（配送中）
        if (order.getStatus() != 2) {
            return Result.failed("订单状态不正确，无法接单");
        }

        // 更新订单状态为待收货（配送中）
        order.setStatus(3);
        order.setDeliveryTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderService.updateById(order);

        log.info("商户接单发货: orderId={}, merchantId={}", id, merchantId);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "拒单")
    public Result<Void> rejectOrder(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Long merchantId = getMerchantId(userId);
        if (merchantId == null) {
            return Result.failed("请先完成商户入驻");
        }

        Order order = orderService.getById(id);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            return Result.failed("订单不存在");
        }

        if (order.getStatus() != 2) {
            return Result.failed("订单状态不正确，无法拒单");
        }

        String reason = body.get("reason");

        // 更新订单状态为已取消
        order.setStatus(6); // 已取消
        order.setCancelReason(reason != null ? reason : "商家拒单");
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderService.updateById(order);

        // TODO: 退款逻辑

        log.info("商户拒单: orderId={}, merchantId={}, reason={}", id, merchantId, reason);
        return Result.success();
    }

    @PutMapping("/{id}/delivery")
    @Operation(summary = "开始配送")
    public Result<Void> startDelivery(
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

        // 状态2=待发货，可以直接开始配送，变为状态3=待收货
        if (order.getStatus() != 2) {
            return Result.failed("订单状态不正确，无法开始配送");
        }

        // 更新订单状态为待收货（配送中）
        order.setStatus(3);
        order.setDeliveryTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderService.updateById(order);

        log.info("开始配送: orderId={}, merchantId={}", id, merchantId);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "完成配送")
    public Result<Void> completeDelivery(
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

        // 状态3=待收货（配送中），完成配送后变为状态4=待评价
        if (order.getStatus() != 3) {
            return Result.failed("订单状态不正确，无法完成配送");
        }

        // 更新订单状态为待评价
        order.setStatus(4);
        order.setReceiveTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderService.updateById(order);

        log.info("完成配送: orderId={}, merchantId={}", id, merchantId);
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
     * 构建订单Map
     */
    private Map<String, Object> buildOrderMap(Order order) {
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", order.getId());
        orderMap.put("orderNo", order.getOrderNo());
        orderMap.put("status", order.getStatus());
        orderMap.put("statusName", getStatusName(order.getStatus()));
        orderMap.put("receiverName", order.getReceiverName());
        orderMap.put("receiverPhone", maskPhone(order.getReceiverPhone()));
        orderMap.put("receiverAddress", buildFullAddress(order));
        orderMap.put("totalAmount", order.getTotalAmount());
        orderMap.put("discountAmount", order.getDiscountAmount());
        orderMap.put("deliveryFee", order.getFreightAmount());
        orderMap.put("payAmount", order.getPayAmount());
        orderMap.put("remark", order.getRemark());

        // 时间格式化
        orderMap.put("createTime", order.getCreateTime() != null ? order.getCreateTime().format(formatter) : null);
        orderMap.put("payTime", order.getPayTime() != null ? order.getPayTime().format(formatter) : null);
        orderMap.put("deliveryTime", order.getDeliveryTime() != null ? order.getDeliveryTime().format(formatter) : null);
        orderMap.put("receiveTime", order.getReceiveTime() != null ? order.getReceiveTime().format(formatter) : null);
        orderMap.put("completeTime", order.getCompleteTime() != null ? order.getCompleteTime().format(formatter) : null);

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
            product.put("totalAmount", item.getTotalAmount());
            products.add(product);
            totalQuantity += item.getQuantity();
        }
        orderMap.put("products", products);
        orderMap.put("totalQuantity", totalQuantity);

        // 用户信息
        User user = userService.getById(order.getUserId());
        if (user != null) {
            orderMap.put("userNickname", user.getNickname());
            orderMap.put("userAvatar", imageUrlUtil.generateUrl(user.getAvatar()));
        }

        // 取消原因
        orderMap.put("cancelReason", order.getCancelReason());

        // 评价信息（状态>=4时查询）
        if (order.getStatus() != null && order.getStatus() >= 4) {
            List<ProductComment> comments = productCommentService.list(
                    new LambdaQueryWrapper<ProductComment>()
                            .eq(ProductComment::getOrderId, order.getId())
                            .eq(ProductComment::getDeleted, 0)
            );
            if (!comments.isEmpty()) {
                List<Map<String, Object>> commentList = new ArrayList<>();
                for (ProductComment comment : comments) {
                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("id", comment.getId());
                    commentMap.put("productId", comment.getProductId());
                    commentMap.put("content", comment.getContent());
                    commentMap.put("rating", comment.getRating());
                    commentMap.put("images", comment.getImages());
                    commentMap.put("isAnonymous", comment.getIsAnonymous());
                    commentMap.put("replyContent", comment.getReplyContent());
                    commentMap.put("createTime", comment.getCreateTime() != null ?
                            comment.getCreateTime().format(formatter) : null);
                    commentList.add(commentMap);
                }
                orderMap.put("comments", commentList);
                orderMap.put("hasComment", true);
            } else {
                orderMap.put("hasComment", false);
            }
        }

        return orderMap;
    }

    /**
     * 获取订单状态名称
     * 状态：1-待付款 2-待发货 3-待收货 4-待评价 5-已完成 6-已取消 7-已退款
     */
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "待付款";
            case 2: return "待发货";
            case 3: return "配送中";
            case 4: return "待评价";
            case 5: return "已完成";
            case 6: return "已取消";
            case 7: return "已退款";
            default: return "未知";
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
