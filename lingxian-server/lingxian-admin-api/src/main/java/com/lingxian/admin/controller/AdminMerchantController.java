package com.lingxian.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.Merchant;
import com.lingxian.common.entity.MerchantInfoAudit;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.MerchantInfoAuditService;
import com.lingxian.common.service.MerchantService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/merchants")
@RequiredArgsConstructor
@Tag(name = "管理后台-商户管理", description = "商户管理相关接口")
public class AdminMerchantController {

    private final MerchantService merchantService;
    private final MerchantInfoAuditService merchantInfoAuditService;
    private final ImageUrlUtil imageUrlUtil;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

        // 转换图片URL
        for (Merchant merchant : pageResult.getRecords()) {
            convertMerchantImageUrls(merchant);
        }

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

        // 转换图片URL
        for (Merchant merchant : pageResult.getRecords()) {
            convertMerchantImageUrls(merchant);
        }

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

        // 转换图片URL
        convertMerchantImageUrls(merchant);

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

        // 构建查询条件
        LambdaQueryWrapper<MerchantInfoAudit> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            // 前端状态码转换为数据库状态码: 前端1->数据库0, 前端2->数据库1, 前端3->数据库2
            int dbStatus = status - 1;
            wrapper.eq(MerchantInfoAudit::getStatus, dbStatus);
        }
        wrapper.orderByDesc(MerchantInfoAudit::getCreateTime);

        // 分页查询
        Page<MerchantInfoAudit> pageResult = merchantInfoAuditService.page(new Page<>(page, size), wrapper);

        // 转换为前端需要的格式
        List<Map<String, Object>> records = new ArrayList<>();
        for (MerchantInfoAudit audit : pageResult.getRecords()) {
            // 获取商户信息
            Merchant merchant = merchantService.getById(audit.getMerchantId());
            if (merchant == null) continue;

            // 如果需要按商户名称过滤
            if (StringUtils.hasText(merchantName) && !merchant.getName().contains(merchantName)) {
                continue;
            }

            Map<String, Object> item = buildAuditItemMap(audit, merchant);
            records.add(item);
        }

        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), records));
    }

    @GetMapping("/info-audits/{id}")
    @Operation(summary = "获取商户信息变更审核详情")
    public Result<Map<String, Object>> getInfoAuditDetail(@PathVariable Long id) {
        log.info("获取商户信息变更审核详情: id={}", id);

        MerchantInfoAudit audit = merchantInfoAuditService.getById(id);
        if (audit == null) {
            return Result.failed("审核记录不存在");
        }

        Merchant merchant = merchantService.getById(audit.getMerchantId());
        if (merchant == null) {
            return Result.failed("商户不存在");
        }

        Map<String, Object> detail = buildAuditItemMap(audit, merchant);
        return Result.success(detail);
    }

    @PutMapping("/info-audits/{id}")
    @Operation(summary = "审核商户信息变更")
    public Result<Void> auditMerchantInfo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Integer frontendStatus = (Integer) body.get("status"); // 前端: 2-通过, 3-拒绝
        String rejectReason = (String) body.get("rejectReason");
        log.info("审核商户信息变更: id={}, status={}, rejectReason={}", id, frontendStatus, rejectReason);

        MerchantInfoAudit audit = merchantInfoAuditService.getById(id);
        if (audit == null) {
            return Result.failed("审核记录不存在");
        }

        if (audit.getStatus() != 0) {
            return Result.failed("该记录已审核");
        }

        // 前端状态码转换为数据库状态码: 前端2->数据库1(通过), 前端3->数据库2(拒绝)
        int dbStatus = frontendStatus - 1;

        LocalDateTime now = LocalDateTime.now();

        if (dbStatus == 1) {
            // 通过审核：更新商户表对应字段
            Merchant merchant = merchantService.getById(audit.getMerchantId());
            if (merchant != null) {
                if (audit.getNewShopName() != null) {
                    merchant.setName(audit.getNewShopName());
                }
                if (audit.getNewLogo() != null) {
                    merchant.setLogo(audit.getNewLogo());
                }
                if (audit.getNewPhone() != null) {
                    merchant.setContactPhone(audit.getNewPhone());
                }
                if (audit.getNewAddress() != null) {
                    merchant.setAddress(audit.getNewAddress());
                }
                if (audit.getNewLongitude() != null) {
                    merchant.setLongitude(audit.getNewLongitude());
                }
                if (audit.getNewLatitude() != null) {
                    merchant.setLatitude(audit.getNewLatitude());
                }
                merchant.setUpdateTime(now);
                merchantService.updateById(merchant);
            }
        }

        // 更新审核记录
        audit.setStatus(dbStatus);
        audit.setAuditRemark(rejectReason);
        audit.setAuditTime(now);
        audit.setUpdateTime(now);
        merchantInfoAuditService.updateById(audit);

        return Result.success();
    }

    /**
     * 构建审核记录的Map数据
     */
    private Map<String, Object> buildAuditItemMap(MerchantInfoAudit audit, Merchant merchant) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", audit.getId());
        item.put("merchantId", audit.getMerchantId());
        item.put("merchantName", merchant.getName());

        // 当前信息
        Map<String, Object> currentInfo = new HashMap<>();
        currentInfo.put("shopLogo", imageUrlUtil.generateUrl(audit.getOldLogo()));
        currentInfo.put("shopName", audit.getOldShopName());
        currentInfo.put("phone", audit.getOldPhone());
        currentInfo.put("address", audit.getOldAddress());
        item.put("currentInfo", currentInfo);

        // 待审核信息
        Map<String, Object> pendingInfo = new HashMap<>();
        pendingInfo.put("shopLogo", imageUrlUtil.generateUrl(audit.getNewLogo()));
        pendingInfo.put("shopName", audit.getNewShopName());
        pendingInfo.put("phone", audit.getNewPhone());
        pendingInfo.put("address", audit.getNewAddress());
        item.put("pendingInfo", pendingInfo);

        // 变更字段列表
        List<String> changedFields = new ArrayList<>();
        if (audit.getNewShopName() != null && !audit.getNewShopName().equals(audit.getOldShopName())) {
            changedFields.add("shopName");
        }
        if (audit.getNewLogo() != null && !audit.getNewLogo().equals(audit.getOldLogo())) {
            changedFields.add("shopLogo");
        }
        if (audit.getNewPhone() != null && !audit.getNewPhone().equals(audit.getOldPhone())) {
            changedFields.add("phone");
        }
        if (audit.getNewAddress() != null && !audit.getNewAddress().equals(audit.getOldAddress())) {
            changedFields.add("address");
        }
        item.put("changedFields", changedFields);

        // 状态映射: 数据库 0-待审核 1-已通过 2-已拒绝 -> 前端 1-待审核 2-已通过 3-已拒绝
        int frontendStatus = audit.getStatus() + 1;
        item.put("status", frontendStatus);

        item.put("applyTime", audit.getCreateTime() != null ? audit.getCreateTime().format(DATE_TIME_FORMATTER) : null);
        item.put("auditTime", audit.getAuditTime() != null ? audit.getAuditTime().format(DATE_TIME_FORMATTER) : null);
        item.put("rejectReason", audit.getAuditRemark());

        // 计算本月修改次数
        int monthlyCount = countMonthlyModifications(audit.getMerchantId());
        item.put("monthlyModifyCount", monthlyCount);

        return item;
    }

    /**
     * 统计商户本月的信息变更次数
     */
    private int countMonthlyModifications(Long merchantId) {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long count = merchantInfoAuditService.count(new LambdaQueryWrapper<MerchantInfoAudit>()
                .eq(MerchantInfoAudit::getMerchantId, merchantId)
                .ge(MerchantInfoAudit::getCreateTime, startOfMonth));
        return (int) count;
    }

    /**
     * 转换商户图片URL（将存储路径转换为可访问的URL）
     */
    private void convertMerchantImageUrls(Merchant merchant) {
        if (merchant == null) return;

        // 转换logo
        if (merchant.getLogo() != null && !merchant.getLogo().isEmpty()) {
            merchant.setLogo(imageUrlUtil.generateUrl(merchant.getLogo()));
        }
        // 转换banner
        if (merchant.getBanner() != null && !merchant.getBanner().isEmpty()) {
            merchant.setBanner(imageUrlUtil.generateUrl(merchant.getBanner()));
        }
        // 转换营业执照照片
        if (merchant.getLicenseImage() != null && !merchant.getLicenseImage().isEmpty()) {
            merchant.setLicenseImage(imageUrlUtil.generateUrl(merchant.getLicenseImage()));
        }
        // 转换身份证正面
        if (merchant.getIdCardFront() != null && !merchant.getIdCardFront().isEmpty()) {
            merchant.setIdCardFront(imageUrlUtil.generateUrl(merchant.getIdCardFront()));
        }
        // 转换身份证反面
        if (merchant.getIdCardBack() != null && !merchant.getIdCardBack().isEmpty()) {
            merchant.setIdCardBack(imageUrlUtil.generateUrl(merchant.getIdCardBack()));
        }
    }
}
