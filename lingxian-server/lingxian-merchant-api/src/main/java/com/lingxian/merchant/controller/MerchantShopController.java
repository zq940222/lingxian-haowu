package com.lingxian.merchant.controller;

import com.lingxian.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/merchant/shop")
@RequiredArgsConstructor
@Tag(name = "商户端-店铺设置", description = "商户店铺设置接口")
public class MerchantShopController {

    @GetMapping
    @Operation(summary = "获取店铺信息")
    public Result<Map<String, Object>> getShopInfo() {
        Map<String, Object> shop = new HashMap<>();
        shop.put("shopLogo", "https://via.placeholder.com/100x100");
        shop.put("shopName", "铃鲜好物旗舰店");
        shop.put("phone", "13812345678");
        shop.put("notice", "新店开业，全场8折优惠！");
        shop.put("address", "北京市朝阳区建国路88号");
        shop.put("latitude", 39.9042);
        shop.put("longitude", 116.4074);
        shop.put("shopStatus", 1);
        shop.put("openTime", "08:00");
        shop.put("closeTime", "22:00");
        shop.put("autoAccept", true);
        shop.put("deliveryRange", 5);
        shop.put("deliveryFee", new BigDecimal("5.00"));
        shop.put("minOrderAmount", new BigDecimal("20.00"));
        shop.put("freeDeliveryAmount", new BigDecimal("50.00"));
        return Result.success(shop);
    }

    @PutMapping
    @Operation(summary = "更新店铺信息")
    public Result<Void> updateShopInfo(@RequestBody Map<String, Object> body) {
        log.info("更新店铺信息: {}", body);
        return Result.success();
    }

    @PutMapping("/status")
    @Operation(summary = "更新营业状态")
    public Result<Void> updateShopStatus(@RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        log.info("更新营业状态: {}", status);
        return Result.success();
    }

    @PutMapping("/hours")
    @Operation(summary = "更新营业时间")
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
    @Operation(summary = "更新配送设置")
    public Result<Void> updateDeliverySettings(@RequestBody Map<String, Object> body) {
        log.info("更新配送设置: {}", body);
        return Result.success();
    }
}
