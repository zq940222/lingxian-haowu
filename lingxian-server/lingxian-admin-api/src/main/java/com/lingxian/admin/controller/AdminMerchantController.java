package com.lingxian.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.Merchant;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/merchants")
@RequiredArgsConstructor
@Tag(name = "管理后台-商户管理", description = "商户管理相关接口")
public class AdminMerchantController {

    private final MerchantService merchantService;

    @GetMapping
    @Operation(summary = "获取商户列表")
    public Result<PageResult<Merchant>> getMerchantList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "商户名称") @RequestParam(required = false) String name,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "营业状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "审核状态: 0-未提交,1-待审核,2-已通过,3-已拒绝") @RequestParam(required = false) Integer verifyStatus) {
        log.info("获取商户列表: page={}, size={}, name={}, phone={}, status={}, verifyStatus={}", page, size, name, phone, status, verifyStatus);

        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Merchant::getName, name);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(Merchant::getContactPhone, phone);
        }
        if (status != null) {
            wrapper.eq(Merchant::getStatus, status);
        }
        if (verifyStatus != null) {
            wrapper.eq(Merchant::getVerifyStatus, verifyStatus);
        }
        wrapper.orderByDesc(Merchant::getCreateTime);

        Page<Merchant> pageResult = merchantService.page(new Page<>(page, size), wrapper);

        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords()));
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待审核商户列表")
    public Result<PageResult<Merchant>> getPendingMerchantList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        log.info("获取待审核商户列表: page={}, size={}", page, size);

        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getVerifyStatus, 1); // 1-待审核
        wrapper.orderByAsc(Merchant::getCreateTime);

        Page<Merchant> pageResult = merchantService.page(new Page<>(page, size), wrapper);

        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商户详情")
    public Result<Merchant> getMerchantDetail(@PathVariable Long id) {
        log.info("获取商户详情: id={}", id);

        Merchant merchant = merchantService.getById(id);
        if (merchant == null) {
            return Result.failed("商户不存在");
        }

        return Result.success(merchant);
    }

    @PostMapping
    @Operation(summary = "创建商户")
    public Result<Void> createMerchant(@RequestBody Merchant merchant) {
        log.info("创建商户: {}", merchant);
        merchant.setCreateTime(LocalDateTime.now());
        merchant.setUpdateTime(LocalDateTime.now());
        merchantService.save(merchant);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商户信息")
    public Result<Void> updateMerchant(@PathVariable Long id, @RequestBody Merchant merchant) {
        log.info("更新商户信息: id={}, merchant={}", id, merchant);

        Merchant existing = merchantService.getById(id);
        if (existing == null) {
            return Result.failed("商户不存在");
        }

        merchant.setId(id);
        merchant.setUpdateTime(LocalDateTime.now());
        merchantService.updateById(merchant);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新商户状态")
    public Result<Void> updateMerchantStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        log.info("更新商户状态: id={}, status={}", id, status);

        Merchant merchant = merchantService.getById(id);
        if (merchant == null) {
            return Result.failed("商户不存在");
        }

        merchant.setStatus(status);
        merchant.setUpdateTime(LocalDateTime.now());
        merchantService.updateById(merchant);
        return Result.success();
    }

    @PutMapping("/{id}/verify")
    @Operation(summary = "审核商户")
    public Result<Void> verifyMerchant(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer verifyStatus = (Integer) body.get("verifyStatus");
        String remark = (String) body.get("remark");
        log.info("审核商户: id={}, verifyStatus={}, remark={}", id, verifyStatus, remark);

        Merchant merchant = merchantService.getById(id);
        if (merchant == null) {
            return Result.failed("商户不存在");
        }

        merchant.setVerifyStatus(verifyStatus);
        merchant.setVerifyRemark(remark);
        merchant.setVerifyTime(LocalDateTime.now());
        merchant.setUpdateTime(LocalDateTime.now());
        merchantService.updateById(merchant);
        return Result.success();
    }

    // ============ 商户信息变更审核接口 ============

    @GetMapping("/info-audits")
    @Operation(summary = "获取商户信息变更审核列表", description = "包含店铺名称、头像、联系电话、地址等信息的变更审核")
    public Result<PageResult<Map<String, Object>>> getInfoAuditList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "审核状态: 1-待审核,2-已通过,3-已拒绝") @RequestParam(required = false) Integer status,
            @Parameter(description = "商户名称") @RequestParam(required = false) String merchantName) {
        log.info("获取商户信息变更审核列表: page={}, size={}, status={}, merchantName={}", page, size, status, merchantName);

        // TODO: 实际实现需要从 merchant_info_audit 表查询
        // 这里返回模拟数据
        java.util.List<Map<String, Object>> records = new java.util.ArrayList<>();

        // 模拟数据1：修改了店铺名称和头像
        Map<String, Object> item1 = new java.util.HashMap<>();
        item1.put("id", 1L);
        item1.put("merchantId", 1001L);
        item1.put("merchantName", "铃鲜好物旗舰店");
        // 当前信息
        Map<String, Object> currentInfo1 = new java.util.HashMap<>();
        currentInfo1.put("shopLogo", "https://via.placeholder.com/100x100?text=Current");
        currentInfo1.put("shopName", "铃鲜好物旗舰店");
        currentInfo1.put("phone", "13812345678");
        currentInfo1.put("address", "北京市朝阳区建国路88号");
        item1.put("currentInfo", currentInfo1);
        // 待审核信息
        Map<String, Object> pendingInfo1 = new java.util.HashMap<>();
        pendingInfo1.put("shopLogo", "https://via.placeholder.com/100x100?text=New");
        pendingInfo1.put("shopName", "铃鲜好物官方旗舰店");
        pendingInfo1.put("phone", "13812345678");
        pendingInfo1.put("address", "北京市朝阳区建国路88号");
        item1.put("pendingInfo", pendingInfo1);
        // 变更字段列表
        java.util.List<String> changedFields1 = new java.util.ArrayList<>();
        changedFields1.add("shopLogo");
        changedFields1.add("shopName");
        item1.put("changedFields", changedFields1);
        item1.put("status", 1); // 待审核
        item1.put("applyTime", "2024-01-15 10:30:00");
        item1.put("auditTime", null);
        item1.put("rejectReason", null);
        item1.put("monthlyModifyCount", 1); // 本月第1次修改
        records.add(item1);

        // 模拟数据2：修改了地址和电话
        Map<String, Object> item2 = new java.util.HashMap<>();
        item2.put("id", 2L);
        item2.put("merchantId", 1002L);
        item2.put("merchantName", "生鲜直供店");
        // 当前信息
        Map<String, Object> currentInfo2 = new java.util.HashMap<>();
        currentInfo2.put("shopLogo", "https://via.placeholder.com/100x100?text=Old");
        currentInfo2.put("shopName", "生鲜直供店");
        currentInfo2.put("phone", "13900001111");
        currentInfo2.put("address", "上海市浦东新区张江路100号");
        item2.put("currentInfo", currentInfo2);
        // 待审核信息
        Map<String, Object> pendingInfo2 = new java.util.HashMap<>();
        pendingInfo2.put("shopLogo", "https://via.placeholder.com/100x100?text=Old");
        pendingInfo2.put("shopName", "生鲜直供店");
        pendingInfo2.put("phone", "13900002222");
        pendingInfo2.put("address", "上海市浦东新区世纪大道200号");
        item2.put("pendingInfo", pendingInfo2);
        // 变更字段列表
        java.util.List<String> changedFields2 = new java.util.ArrayList<>();
        changedFields2.add("phone");
        changedFields2.add("address");
        item2.put("changedFields", changedFields2);
        item2.put("status", 1); // 待审核
        item2.put("applyTime", "2024-01-15 14:20:00");
        item2.put("auditTime", null);
        item2.put("rejectReason", null);
        item2.put("monthlyModifyCount", 2); // 本月第2次修改
        records.add(item2);

        // 根据status过滤
        if (status != null) {
            records = records.stream()
                    .filter(r -> status.equals(r.get("status")))
                    .collect(java.util.stream.Collectors.toList());
        }

        // 根据商户名称过滤
        if (StringUtils.hasText(merchantName)) {
            final String searchName = merchantName;
            records = records.stream()
                    .filter(r -> ((String) r.get("merchantName")).contains(searchName))
                    .collect(java.util.stream.Collectors.toList());
        }

        return Result.success(PageResult.of((long) records.size(), (long) page, (long) size, records));
    }

    @GetMapping("/info-audits/{id}")
    @Operation(summary = "获取商户信息变更审核详情")
    public Result<Map<String, Object>> getInfoAuditDetail(@PathVariable Long id) {
        log.info("获取商户信息变更审核详情: id={}", id);

        // TODO: 实际实现从数据库查询
        Map<String, Object> detail = new java.util.HashMap<>();
        detail.put("id", id);
        detail.put("merchantId", 1001L);
        detail.put("merchantName", "铃鲜好物旗舰店");

        // 当前信息
        Map<String, Object> currentInfo = new java.util.HashMap<>();
        currentInfo.put("shopLogo", "https://via.placeholder.com/100x100?text=Current");
        currentInfo.put("shopName", "铃鲜好物旗舰店");
        currentInfo.put("phone", "13812345678");
        currentInfo.put("address", "北京市朝阳区建国路88号");
        detail.put("currentInfo", currentInfo);

        // 待审核信息
        Map<String, Object> pendingInfo = new java.util.HashMap<>();
        pendingInfo.put("shopLogo", "https://via.placeholder.com/100x100?text=New");
        pendingInfo.put("shopName", "铃鲜好物官方旗舰店");
        pendingInfo.put("phone", "13812345678");
        pendingInfo.put("address", "北京市朝阳区建国路88号");
        detail.put("pendingInfo", pendingInfo);

        // 变更字段
        java.util.List<String> changedFields = new java.util.ArrayList<>();
        changedFields.add("shopLogo");
        changedFields.add("shopName");
        detail.put("changedFields", changedFields);

        detail.put("status", 1);
        detail.put("applyTime", "2024-01-15 10:30:00");
        detail.put("auditTime", null);
        detail.put("rejectReason", null);
        detail.put("monthlyModifyCount", 1);

        return Result.success(detail);
    }

    @PutMapping("/info-audits/{id}")
    @Operation(summary = "审核商户信息变更")
    public Result<Void> auditMerchantInfo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status"); // 2-通过, 3-拒绝
        String rejectReason = (String) body.get("rejectReason");
        log.info("审核商户信息变更: id={}, status={}, rejectReason={}", id, status, rejectReason);

        // TODO: 实际实现
        // 1. 查询 merchant_info_audit 记录
        // 2. 如果通过(status=2)：
        //    - 根据 changedFields 更新商户表对应字段
        //    - 更新审核记录状态为已通过
        //    - 清空商户的 infoAuditStatus
        // 3. 如果拒绝(status=3)：
        //    - 更新审核记录状态和拒绝原因
        //    - 更新商户的 infoAuditStatus 为已拒绝
        //    - 商户端可重新提交修改

        return Result.success();
    }
}
