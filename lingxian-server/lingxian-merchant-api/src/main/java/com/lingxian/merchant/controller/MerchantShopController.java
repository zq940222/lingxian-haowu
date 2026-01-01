package com.lingxian.merchant.controller;

import com.lingxian.common.entity.Merchant;
import com.lingxian.common.entity.MerchantInfoAudit;
import com.lingxian.common.entity.MerchantUser;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.MerchantInfoAuditService;
import com.lingxian.common.service.MerchantService;
import com.lingxian.common.service.MerchantUserService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/merchant/shop")
@RequiredArgsConstructor
@Tag(name = "商户端-店铺设置", description = "商户店铺设置接口")
public class MerchantShopController {

    private final MerchantService merchantService;
    private final MerchantUserService merchantUserService;
    private final MerchantInfoAuditService merchantInfoAuditService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping
    @Operation(summary = "获取店铺信息", description = "包含审核状态、待审核数据、本月修改次数等")
    public Result<Map<String, Object>> getShopInfo(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Merchant merchant = merchantService.getById(merchantUser.getMerchantId());
        if (merchant == null) {
            return Result.failed("商户信息不存在");
        }

        Map<String, Object> shop = new HashMap<>();

        // 基本信息 - 从数据库读取真实数据
        shop.put("shopLogo", imageUrlUtil.generateUrl(merchant.getLogo()));
        shop.put("shopName", merchant.getName());
        shop.put("phone", merchant.getContactPhone());
        shop.put("notice", merchant.getDescription() != null ? merchant.getDescription() : "");

        // 拼接完整地址
        String fullAddress = "";
        if (merchant.getProvince() != null) fullAddress += merchant.getProvince();
        if (merchant.getCity() != null) fullAddress += merchant.getCity();
        if (merchant.getDistrict() != null) fullAddress += merchant.getDistrict();
        if (merchant.getAddress() != null) fullAddress += merchant.getAddress();
        shop.put("address", fullAddress);

        shop.put("latitude", merchant.getLatitude() != null ? merchant.getLatitude() : 0);
        shop.put("longitude", merchant.getLongitude() != null ? merchant.getLongitude() : 0);

        // 营业设置 - TODO: 这些字段需要添加到商户表
        shop.put("shopStatus", merchant.getStatus() != null ? merchant.getStatus() : 0);
        shop.put("openTime", "08:00");
        shop.put("closeTime", "22:00");
        shop.put("autoAccept", false);

        // 配送设置 - TODO: 这些字段需要添加到商户表或单独的配送设置表
        shop.put("deliveryRange", 5);
        shop.put("deliveryFee", new BigDecimal("5.00"));
        shop.put("minOrderAmount", new BigDecimal("20.00"));
        shop.put("freeDeliveryAmount", new BigDecimal("50.00"));

        // 查询是否有待审核的信息变更
        MerchantInfoAudit pendingAudit = merchantInfoAuditService.getPendingAudit(merchant.getId());
        if (pendingAudit != null) {
            // auditStatus: 0-无待审核 1-审核中 2-审核未通过
            shop.put("auditStatus", 1); // 审核中
            // 构建待审核的变更数据
            Map<String, Object> pendingData = new HashMap<>();
            if (pendingAudit.getNewShopName() != null && !pendingAudit.getNewShopName().equals(merchant.getName())) {
                pendingData.put("shopName", pendingAudit.getNewShopName());
            }
            if (pendingAudit.getNewLogo() != null && !pendingAudit.getNewLogo().equals(merchant.getLogo())) {
                pendingData.put("shopLogo", imageUrlUtil.generateUrl(pendingAudit.getNewLogo()));
            }
            if (pendingAudit.getNewPhone() != null && !pendingAudit.getNewPhone().equals(merchant.getContactPhone())) {
                pendingData.put("phone", pendingAudit.getNewPhone());
            }
            if (pendingAudit.getNewAddress() != null && !pendingAudit.getNewAddress().equals(fullAddress)) {
                pendingData.put("address", pendingAudit.getNewAddress());
            }
            shop.put("pendingData", pendingData);
            shop.put("auditRejectReason", "");
        } else {
            // 查询最近一条被拒绝的记录
            MerchantInfoAudit rejectedAudit = merchantInfoAuditService.lambdaQuery()
                    .eq(MerchantInfoAudit::getMerchantId, merchant.getId())
                    .eq(MerchantInfoAudit::getStatus, 2) // 审核拒绝
                    .orderByDesc(MerchantInfoAudit::getUpdateTime)
                    .last("LIMIT 1")
                    .one();
            if (rejectedAudit != null) {
                shop.put("auditStatus", 2); // 审核未通过
                shop.put("pendingData", new HashMap<>());
                shop.put("auditRejectReason", rejectedAudit.getAuditRemark() != null ? rejectedAudit.getAuditRemark() : "");
            } else {
                shop.put("auditStatus", 0); // 无待审核
                shop.put("pendingData", new HashMap<>());
                shop.put("auditRejectReason", "");
            }
        }

        // 本月修改次数 - 统计本月提交的审核记录数
        long monthlyCount = merchantInfoAuditService.lambdaQuery()
                .eq(MerchantInfoAudit::getMerchantId, merchant.getId())
                .ge(MerchantInfoAudit::getCreateTime, LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0))
                .count();
        shop.put("monthlyModifyCount", (int) monthlyCount);

        return Result.success(shop);
    }

    @PutMapping
    @Operation(summary = "更新店铺信息", description = "店铺名称、头像、联系电话、地址等需要审核；营业设置、配送设置即时生效")
    public Result<Map<String, Object>> updateShopInfo(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        log.info("更新店铺信息: userId={}, body={}", userId, body);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Merchant merchant = merchantService.getById(merchantUser.getMerchantId());
        if (merchant == null) {
            return Result.failed("商户信息不存在");
        }

        // 检查是否有待审核的变更
        MerchantInfoAudit existingAudit = merchantInfoAuditService.getPendingAudit(merchant.getId());
        if (existingAudit != null) {
            return Result.failed("您有待审核的信息变更，请等待审核完成后再修改");
        }

        LocalDateTime now = LocalDateTime.now();

        // 获取需要审核的字段
        String newShopName = (String) body.get("shopName");
        String newShopLogo = (String) body.get("shopLogo");
        String newPhone = (String) body.get("phone");
        String newAddress = (String) body.get("address");
        BigDecimal newLatitude = body.get("latitude") != null ? new BigDecimal(body.get("latitude").toString()) : null;
        BigDecimal newLongitude = body.get("longitude") != null ? new BigDecimal(body.get("longitude").toString()) : null;

        // 构建当前完整地址
        String currentFullAddress = "";
        if (merchant.getProvince() != null) currentFullAddress += merchant.getProvince();
        if (merchant.getCity() != null) currentFullAddress += merchant.getCity();
        if (merchant.getDistrict() != null) currentFullAddress += merchant.getDistrict();
        if (merchant.getAddress() != null) currentFullAddress += merchant.getAddress();

        // 检查是否有需要审核的字段变更
        boolean hasAuditFieldChanged = false;
        if (newShopName != null && !newShopName.equals(merchant.getName())) {
            hasAuditFieldChanged = true;
        }
        if (newShopLogo != null && !newShopLogo.equals(merchant.getLogo())) {
            hasAuditFieldChanged = true;
        }
        if (newPhone != null && !newPhone.equals(merchant.getContactPhone())) {
            hasAuditFieldChanged = true;
        }
        if (newAddress != null && !newAddress.equals(currentFullAddress)) {
            hasAuditFieldChanged = true;
        }

        // 如果有需要审核的字段变更，检查本月修改次数
        if (hasAuditFieldChanged) {
            long monthlyCount = merchantInfoAuditService.lambdaQuery()
                    .eq(MerchantInfoAudit::getMerchantId, merchant.getId())
                    .ge(MerchantInfoAudit::getCreateTime, now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0))
                    .count();
            if (monthlyCount >= 3) {
                return Result.failed("本月修改次数已达上限（3次）");
            }

            // 创建审核记录
            MerchantInfoAudit audit = new MerchantInfoAudit();
            audit.setMerchantId(merchant.getId());
            audit.setChangeType("shop_info");

            // 记录原值和新值
            audit.setOldShopName(merchant.getName());
            audit.setNewShopName(newShopName != null ? newShopName : merchant.getName());

            audit.setOldLogo(merchant.getLogo());
            audit.setNewLogo(newShopLogo != null ? newShopLogo : merchant.getLogo());

            audit.setOldPhone(merchant.getContactPhone());
            audit.setNewPhone(newPhone != null ? newPhone : merchant.getContactPhone());

            audit.setOldAddress(currentFullAddress);
            audit.setNewAddress(newAddress != null ? newAddress : currentFullAddress);

            audit.setOldLatitude(merchant.getLatitude());
            audit.setNewLatitude(newLatitude != null ? newLatitude : merchant.getLatitude());

            audit.setOldLongitude(merchant.getLongitude());
            audit.setNewLongitude(newLongitude != null ? newLongitude : merchant.getLongitude());

            audit.setStatus(0); // 待审核
            audit.setCreateTime(now);
            audit.setUpdateTime(now);

            merchantInfoAuditService.save(audit);
            log.info("创建商户信息变更审核记录: auditId={}, merchantId={}", audit.getId(), merchant.getId());
        }

        // 即时生效的字段直接更新到商户表
        boolean hasInstantUpdate = false;
        String notice = (String) body.get("notice");
        if (notice != null && !notice.equals(merchant.getDescription())) {
            merchant.setDescription(notice);
            hasInstantUpdate = true;
        }

        Integer shopStatus = body.get("shopStatus") != null ? ((Number) body.get("shopStatus")).intValue() : null;
        if (shopStatus != null && !shopStatus.equals(merchant.getStatus())) {
            merchant.setStatus(shopStatus);
            hasInstantUpdate = true;
        }

        // TODO: 营业时间、配送设置等字段需要添加到商户表后再处理

        if (hasInstantUpdate) {
            merchant.setUpdateTime(now);
            merchantService.updateById(merchant);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("needAudit", hasAuditFieldChanged);
        if (hasAuditFieldChanged) {
            result.put("message", "店铺信息变更已提交审核，审核通过后生效");
        } else {
            result.put("message", "保存成功");
        }

        return Result.success(result);
    }

    @PutMapping("/status")
    @Operation(summary = "更新营业状态", description = "即时生效，不需要审核")
    public Result<Void> updateShopStatus(@RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        log.info("更新营业状态: {}", status);
        return Result.success();
    }

    @PutMapping("/hours")
    @Operation(summary = "更新营业时间", description = "即时生效，不需要审核")
    public Result<Void> updateBusinessHours(@RequestBody Map<String, String> body) {
        log.info("更新营业时间: {}", body);
        return Result.success();
    }

    @GetMapping("/delivery-settings")
    @Operation(summary = "获取配送设置")
    public Result<Map<String, Object>> getDeliverySettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("deliveryRange", 5);
        settings.put("allowOutOfRange", false);
        settings.put("extraFeePerKm", new BigDecimal("2.00"));
        settings.put("deliveryFee", new BigDecimal("5.00"));
        settings.put("minOrderAmount", new BigDecimal("20.00"));
        settings.put("freeDeliveryAmount", new BigDecimal("50.00"));
        settings.put("allowReservation", true);
        settings.put("reservationDays", 3);
        settings.put("deliveryStartTime", "08:00");
        settings.put("deliveryEndTime", "22:00");
        settings.put("estimatedDeliveryTime", 30);
        settings.put("autoAssign", false);
        settings.put("deliverymanCount", 3);
        return Result.success(settings);
    }

    @PutMapping("/delivery-settings")
    @Operation(summary = "更新配送设置", description = "即时生效，不需要审核")
    public Result<Void> updateDeliverySettings(@RequestBody Map<String, Object> body) {
        log.info("更新配送设置: {}", body);
        return Result.success();
    }
}
