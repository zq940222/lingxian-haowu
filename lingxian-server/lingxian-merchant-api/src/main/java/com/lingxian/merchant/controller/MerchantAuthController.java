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
@RequestMapping("/merchant/auth")
@RequiredArgsConstructor
@Tag(name = "商户端-认证", description = "商户认证登录接口")
public class MerchantAuthController {

    @PostMapping("/login")
    @Operation(summary = "账号密码登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");
        log.info("商户登录: {}", phone);

        // 模拟登录成功
        Map<String, Object> data = new HashMap<>();
        data.put("token", "mock_merchant_token_" + System.currentTimeMillis());

        Map<String, Object> merchantInfo = new HashMap<>();
        merchantInfo.put("id", 1);
        merchantInfo.put("phone", phone);
        merchantInfo.put("shopName", "铃鲜好物旗舰店");
        merchantInfo.put("shopLogo", "https://via.placeholder.com/100x100");
        merchantInfo.put("shopStatus", 1);
        merchantInfo.put("rating", new BigDecimal("4.8"));
        data.put("merchantInfo", merchantInfo);

        return Result.success(data);
    }

    @PostMapping("/wx-login")
    @Operation(summary = "微信登录")
    public Result<Map<String, Object>> wxLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        log.info("商户微信登录: code={}", code);

        // 模拟登录成功
        Map<String, Object> data = new HashMap<>();
        data.put("token", "mock_merchant_token_" + System.currentTimeMillis());

        Map<String, Object> merchantInfo = new HashMap<>();
        merchantInfo.put("id", 1);
        merchantInfo.put("phone", "138****1234");
        merchantInfo.put("shopName", "铃鲜好物旗舰店");
        merchantInfo.put("shopLogo", "https://via.placeholder.com/100x100");
        merchantInfo.put("shopStatus", 1);
        merchantInfo.put("rating", new BigDecimal("4.8"));
        data.put("merchantInfo", merchantInfo);

        return Result.success(data);
    }

    @GetMapping("/info")
    @Operation(summary = "获取商户信息")
    public Result<Map<String, Object>> getMerchantInfo() {
        Map<String, Object> merchantInfo = new HashMap<>();
        merchantInfo.put("id", 1);
        merchantInfo.put("phone", "138****1234");
        merchantInfo.put("shopName", "铃鲜好物旗舰店");
        merchantInfo.put("shopLogo", "https://via.placeholder.com/100x100");
        merchantInfo.put("shopDesc", "新鲜果蔬，每日直供");
        merchantInfo.put("shopStatus", 1);
        merchantInfo.put("rating", new BigDecimal("4.8"));
        merchantInfo.put("monthlySales", 1256);
        merchantInfo.put("balance", new BigDecimal("12580.50"));
        return Result.success(merchantInfo);
    }

    @PutMapping("/info")
    @Operation(summary = "更新商户信息")
    public Result<Void> updateMerchantInfo(@RequestBody Map<String, Object> body) {
        log.info("更新商户信息: {}", body);
        return Result.success();
    }
}
