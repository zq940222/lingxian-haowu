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
}
