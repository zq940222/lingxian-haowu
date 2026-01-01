package com.lingxian.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.*;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/refunds")
@RequiredArgsConstructor
@Tag(name = "管理后台-退款管理", description = "退款管理相关接口")
public class AdminRefundController {

    private final RefundService refundService;
    private final RefundItemService refundItemService;
    private final OrderService orderService;
    private final UserService userService;
    private final MerchantService merchantService;

    private static final String[] STATUS_NAMES = {"待审核", "退款中", "已完成", "已拒绝", "已取消"};
    private static final String[] REFUND_TYPE_NAMES = {"仅退款", "退货退款"};

    @GetMapping
    @Operation(summary = "获取退款列表")
    public Result<PageResult<Refund>> getRefundList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "退款单号") @RequestParam(required = false) String refundNo,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        log.info("获取退款列表: page={}, size={}, refundNo={}, orderNo={}, status={}", page, size, refundNo, orderNo, status);

        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(refundNo)) {
            wrapper.like(Refund::getRefundNo, refundNo);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(Refund::getOrderNo, orderNo);
        }
        if (status != null) {
            wrapper.eq(Refund::getStatus, status);
        }
        wrapper.orderByDesc(Refund::getCreateTime);

        Page<Refund> pageResult = refundService.page(new Page<>(page, size), wrapper);

        // 填充额外信息
        for (Refund refund : pageResult.getRecords()) {
            fillRefundInfo(refund);
        }

        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取退款详情")
    public Result<Map<String, Object>> getRefundDetail(@PathVariable Long id) {
        log.info("获取退款详情: id={}", id);

        Refund refund = refundService.getById(id);
        if (refund == null) {
            return Result.failed("退款单不存在");
        }

        fillRefundInfo(refund);

        // 获取订单信息
        Order order = orderService.getById(refund.getOrderId());
        Map<String, Object> orderInfo = null;
        if (order != null) {
            orderInfo = new HashMap<>();
            orderInfo.put("orderNo", order.getOrderNo());
            orderInfo.put("totalAmount", order.getTotalAmount());
            orderInfo.put("payAmount", order.getPayAmount());
            orderInfo.put("payType", order.getPayType());
            orderInfo.put("payTypeName", order.getPayType() == 1 ? "微信支付" : "余额支付");
            orderInfo.put("createTime", order.getCreateTime());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", refund.getId());
        result.put("refundNo", refund.getRefundNo());
        result.put("orderId", refund.getOrderId());
        result.put("orderNo", refund.getOrderNo());
        result.put("userId", refund.getUserId());
        result.put("userNickname", refund.getUserNickname());
        result.put("userPhone", refund.getUserPhone());
        result.put("merchantId", refund.getMerchantId());
        result.put("merchantName", refund.getMerchantName());
        result.put("refundAmount", refund.getRefundAmount());
        result.put("reason", refund.getReason());
        result.put("description", refund.getDescription());
        result.put("images", refund.getImages());
        result.put("status", refund.getStatus());
        result.put("statusName", refund.getStatusName());
        result.put("refundType", refund.getRefundType());
        result.put("refundTypeName", refund.getRefundTypeName());
        result.put("createTime", refund.getCreateTime());
        result.put("auditTime", refund.getAuditTime());
        result.put("auditRemark", refund.getAuditRemark());
        result.put("refundTime", refund.getRefundTime());
        result.put("items", refund.getItems());
        result.put("orderInfo", orderInfo);

        return Result.success(result);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "同意退款")
    public Result<Void> approveRefund(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String remark = (String) body.get("remark");
        log.info("同意退款: id={}, remark={}", id, remark);

        Refund refund = refundService.getById(id);
        if (refund == null) {
            return Result.failed("退款单不存在");
        }

        if (refund.getStatus() != 0) {
            return Result.failed("该退款单已处理");
        }

        refund.setStatus(1); // 退款中
        refund.setAuditTime(LocalDateTime.now());
        refund.setAuditRemark(remark);
        refund.setUpdateTime(LocalDateTime.now());
        refundService.updateById(refund);

        // TODO: 实际应调用支付接口进行退款

        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "拒绝退款")
    public Result<Void> rejectRefund(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String remark = (String) body.get("remark");
        log.info("拒绝退款: id={}, remark={}", id, remark);

        Refund refund = refundService.getById(id);
        if (refund == null) {
            return Result.failed("退款单不存在");
        }

        if (refund.getStatus() != 0) {
            return Result.failed("该退款单已处理");
        }

        refund.setStatus(3); // 已拒绝
        refund.setAuditTime(LocalDateTime.now());
        refund.setAuditRemark(remark);
        refund.setUpdateTime(LocalDateTime.now());
        refundService.updateById(refund);

        return Result.success();
    }

    private void fillRefundInfo(Refund refund) {
        // 填充用户信息
        if (refund.getUserId() != null) {
            User user = userService.getById(refund.getUserId());
            if (user != null) {
                refund.setUserNickname(user.getNickname());
                refund.setUserPhone(user.getPhone());
            }
        }

        // 填充商户名称
        if (refund.getMerchantId() != null) {
            Merchant merchant = merchantService.getById(refund.getMerchantId());
            if (merchant != null) {
                refund.setMerchantName(merchant.getName());
            }
        }

        // 填充状态名称
        if (refund.getStatus() != null && refund.getStatus() >= 0 && refund.getStatus() < STATUS_NAMES.length) {
            refund.setStatusName(STATUS_NAMES[refund.getStatus()]);
        }

        // 填充退款类型名称
        if (refund.getRefundType() != null && refund.getRefundType() >= 1 && refund.getRefundType() <= REFUND_TYPE_NAMES.length) {
            refund.setRefundTypeName(REFUND_TYPE_NAMES[refund.getRefundType() - 1]);
        }

        // 获取退款商品
        List<RefundItem> items = refundItemService.list(
                new LambdaQueryWrapper<RefundItem>()
                        .eq(RefundItem::getRefundId, refund.getId())
        );
        refund.setItems(items);
    }
}
