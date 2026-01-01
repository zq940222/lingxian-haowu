package com.lingxian.merchant.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingxian.common.entity.Merchant;
import com.lingxian.common.entity.MerchantUser;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.MerchantService;
import com.lingxian.common.service.MerchantUserService;
import com.lingxian.common.util.ImageUrlUtil;
import com.lingxian.common.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 商户端认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/merchant/auth")
@Tag(name = "商户端-认证", description = "商户认证登录接口")
public class MerchantAuthController {

    private final WxMaService wxMaService;
    private final MerchantUserService merchantUserService;
    private final MerchantService merchantService;
    private final JwtUtil jwtUtil;
    private final ImageUrlUtil imageUrlUtil;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public MerchantAuthController(
            @Qualifier("merchantWxMaService") WxMaService wxMaService,
            MerchantUserService merchantUserService,
            MerchantService merchantService,
            JwtUtil jwtUtil,
            ImageUrlUtil imageUrlUtil) {
        this.wxMaService = wxMaService;
        this.merchantUserService = merchantUserService;
        this.merchantService = merchantService;
        this.jwtUtil = jwtUtil;
        this.imageUrlUtil = imageUrlUtil;
    }

    @PostMapping("/wx-login")
    @Operation(summary = "微信登录")
    public Result<Map<String, Object>> wxLogin(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        String nickname = (String) body.get("nickname");
        String avatar = (String) body.get("avatar");

        log.info("商户微信登录: code={}, nickname={}", code, nickname);

        if (code == null || code.isEmpty()) {
            return Result.failed("code不能为空");
        }

        try {
            String openid;
            String unionId = null;

            // 开发环境支持模拟登录
            if ("dev".equals(activeProfile) && code.startsWith("mock_")) {
                if (code.startsWith("mock_openid_")) {
                    openid = code.substring(5);
                } else {
                    openid = "merchant_dev_openid_" + code.substring(5);
                }
                log.info("开发环境模拟登录: openid={}", openid);
            } else {
                // 正式环境调用微信接口
                WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
                openid = session.getOpenid();
                unionId = session.getUnionid();
            }

            log.info("微信登录成功: openid={}, unionId={}", openid, unionId);

            // 查找或创建商户用户
            MerchantUser merchantUser = merchantUserService.getOne(new LambdaQueryWrapper<MerchantUser>()
                    .eq(MerchantUser::getOpenid, openid));

            LocalDateTime now = LocalDateTime.now();
            boolean isNewUser = false;

            if (merchantUser == null) {
                // 创建新用户
                merchantUser = new MerchantUser();
                merchantUser.setOpenid(openid);
                merchantUser.setUnionId(unionId);
                merchantUser.setNickname(nickname != null ? nickname : "商户用户");
                merchantUser.setAvatar(avatar != null ? avatar : "");
                merchantUser.setRole("owner");
                merchantUser.setStatus(1);
                merchantUser.setLastLoginTime(now);
                merchantUser.setCreateTime(now);
                merchantUser.setUpdateTime(now);
                merchantUserService.save(merchantUser);
                isNewUser = true;
                log.info("创建新商户用户: userId={}", merchantUser.getId());
            } else {
                // 更新登录时间
                merchantUser.setLastLoginTime(now);
                merchantUser.setUpdateTime(now);
                if (nickname != null && !nickname.isEmpty()) {
                    merchantUser.setNickname(nickname);
                }
                if (avatar != null && !avatar.isEmpty()) {
                    merchantUser.setAvatar(avatar);
                }
                merchantUserService.updateById(merchantUser);
                log.info("商户用户登录: userId={}", merchantUser.getId());
            }

            // 生成 token (使用商户用户ID)
            String token = jwtUtil.generateToken(merchantUser.getId(), openid);
            String refreshToken = jwtUtil.generateRefreshToken(merchantUser.getId());

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("refreshToken", refreshToken);
            result.put("isNewUser", isNewUser);

            // 用户基本信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", merchantUser.getId());
            userInfo.put("nickname", merchantUser.getNickname());
            userInfo.put("avatar", imageUrlUtil.generateUrl(merchantUser.getAvatar()));
            userInfo.put("phone", merchantUser.getPhone());
            result.put("userInfo", userInfo);

            // 商户信息（如果已关联）
            if (merchantUser.getMerchantId() != null) {
                Merchant merchant = merchantService.getById(merchantUser.getMerchantId());
                if (merchant != null) {
                    Map<String, Object> merchantInfo = buildMerchantInfo(merchant);
                    result.put("merchantInfo", merchantInfo);
                    result.put("verifyStatus", merchant.getVerifyStatus());
                }
            } else {
                result.put("verifyStatus", 0); // 未提交申请
            }

            return Result.success(result);

        } catch (WxErrorException e) {
            log.error("微信登录失败: {}", e.getMessage(), e);
            return Result.failed("微信登录失败: " + e.getError().getErrorMsg());
        } catch (Exception e) {
            log.error("登录异常: {}", e.getMessage(), e);
            return Result.failed("登录失败，请重试");
        }
    }

    @PostMapping("/login")
    @Operation(summary = "账号密码登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");
        log.info("商户账号登录: phone={}", phone);

        if (phone == null || phone.isEmpty()) {
            return Result.failed("手机号不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.failed("密码不能为空");
        }

        // 根据手机号查找商户用户
        MerchantUser merchantUser = merchantUserService.getOne(new LambdaQueryWrapper<MerchantUser>()
                .eq(MerchantUser::getPhone, phone));

        LocalDateTime now = LocalDateTime.now();
        boolean isNewUser = false;

        // 开发环境：如果用户不存在则自动创建
        if (merchantUser == null) {
            if ("dev".equals(activeProfile)) {
                // 开发环境自动创建用户
                merchantUser = new MerchantUser();
                merchantUser.setPhone(phone);
                merchantUser.setOpenid("phone_" + phone);
                merchantUser.setNickname("商户" + phone.substring(phone.length() - 4));
                merchantUser.setAvatar("");
                merchantUser.setRole("owner");
                merchantUser.setStatus(1);
                merchantUser.setLastLoginTime(now);
                merchantUser.setCreateTime(now);
                merchantUser.setUpdateTime(now);
                merchantUserService.save(merchantUser);
                isNewUser = true;
                log.info("开发环境自动创建商户用户: userId={}, phone={}", merchantUser.getId(), phone);
            } else {
                return Result.failed("账号不存在");
            }
        }

        // TODO: 实际项目中需要验证密码（需要在数据库中添加密码字段）
        // 目前开发阶段直接通过
        if (!"dev".equals(activeProfile)) {
            return Result.failed("暂不支持账号密码登录");
        }

        // 更新登录时间
        if (!isNewUser) {
            merchantUser.setLastLoginTime(now);
            merchantUser.setUpdateTime(now);
            merchantUserService.updateById(merchantUser);
        }

        // 生成 token
        String token = jwtUtil.generateToken(merchantUser.getId(), merchantUser.getOpenid());
        String refreshToken = jwtUtil.generateRefreshToken(merchantUser.getId());

        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("isNewUser", isNewUser);

        // 用户基本信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", merchantUser.getId());
        userInfo.put("nickname", merchantUser.getNickname());
        userInfo.put("avatar", imageUrlUtil.generateUrl(merchantUser.getAvatar()));
        userInfo.put("phone", merchantUser.getPhone());
        result.put("userInfo", userInfo);

        // 商户信息
        if (merchantUser.getMerchantId() != null) {
            Merchant merchant = merchantService.getById(merchantUser.getMerchantId());
            if (merchant != null) {
                Map<String, Object> merchantInfo = buildMerchantInfo(merchant);
                result.put("merchantInfo", merchantInfo);
                result.put("verifyStatus", merchant.getVerifyStatus());
            }
        } else {
            result.put("verifyStatus", 0); // 未提交申请
        }

        return Result.success(result);
    }

    @PostMapping("/phone")
    @Operation(summary = "获取手机号")
    public Result<Map<String, Object>> getPhoneNumber(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");

        log.info("获取商户手机号: userId={}, code={}", userId, code);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        if (code == null || code.isEmpty()) {
            return Result.failed("code不能为空");
        }

        try {
            WxMaPhoneNumberInfo phoneInfo = wxMaService.getUserService().getPhoneNoInfo(code);
            String phoneNumber = phoneInfo.getPhoneNumber();

            log.info("获取手机号成功: userId={}, phone={}", userId, phoneNumber);

            // 更新用户手机号
            MerchantUser merchantUser = merchantUserService.getById(userId);
            if (merchantUser != null) {
                merchantUser.setPhone(phoneNumber);
                merchantUser.setUpdateTime(LocalDateTime.now());
                merchantUserService.updateById(merchantUser);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("phone", phoneNumber);

            return Result.success(result);

        } catch (WxErrorException e) {
            log.error("获取手机号失败: {}", e.getMessage(), e);
            return Result.failed("获取手机号失败: " + e.getError().getErrorMsg());
        } catch (Exception e) {
            log.error("获取手机号异常: {}", e.getMessage(), e);
            return Result.failed("获取手机号失败，请重试");
        }
    }

    @GetMapping("/check")
    @Operation(summary = "检查登录状态")
    public Result<Map<String, Object>> checkLogin(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("检查商户登录状态");

        Map<String, Object> result = new HashMap<>();

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            result.put("isLogin", false);
            return Result.success(result);
        }

        String token = authorization.substring(7);
        if (jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserId(token);
            MerchantUser merchantUser = merchantUserService.getById(userId);

            if (merchantUser != null && merchantUser.getStatus() == 1) {
                result.put("isLogin", true);

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", merchantUser.getId());
                userInfo.put("nickname", merchantUser.getNickname());
                userInfo.put("avatar", imageUrlUtil.generateUrl(merchantUser.getAvatar()));
                userInfo.put("phone", merchantUser.getPhone());
                result.put("userInfo", userInfo);

                // 商户信息
                if (merchantUser.getMerchantId() != null) {
                    Merchant merchant = merchantService.getById(merchantUser.getMerchantId());
                    if (merchant != null) {
                        Map<String, Object> merchantInfo = buildMerchantInfo(merchant);
                        result.put("merchantInfo", merchantInfo);
                        result.put("verifyStatus", merchant.getVerifyStatus());
                    }
                } else {
                    result.put("verifyStatus", 0);
                }
            } else {
                result.put("isLogin", false);
            }
        } else {
            result.put("isLogin", false);
        }

        return Result.success(result);
    }

    @PostMapping("/apply")
    @Operation(summary = "提交入驻申请")
    public Result<Map<String, Object>> applyMerchant(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {

        log.info("提交入驻申请: userId={}, body={}", userId, body);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null) {
            return Result.failed("用户不存在");
        }

        // 检查是否已有商户
        if (merchantUser.getMerchantId() != null) {
            Merchant existMerchant = merchantService.getById(merchantUser.getMerchantId());
            if (existMerchant != null) {
                if (existMerchant.getVerifyStatus() == 1) {
                    return Result.failed("您已提交申请，请等待审核");
                }
                if (existMerchant.getVerifyStatus() == 2) {
                    return Result.failed("您的申请已通过审核");
                }
            }
        }

        // 创建商户信息
        LocalDateTime now = LocalDateTime.now();
        Merchant merchant = new Merchant();
        merchant.setName((String) body.get("shopName"));
        merchant.setLogo((String) body.get("logo"));
        merchant.setDescription((String) body.get("description"));
        merchant.setContactName((String) body.get("contactName"));
        merchant.setContactPhone((String) body.get("contactPhone"));
        merchant.setCategory((String) body.get("category"));
        merchant.setProvince((String) body.get("province"));
        merchant.setCity((String) body.get("city"));
        merchant.setDistrict((String) body.get("district"));
        merchant.setAddress((String) body.get("address"));

        if (body.get("longitude") != null) {
            merchant.setLongitude(new BigDecimal(body.get("longitude").toString()));
        }
        if (body.get("latitude") != null) {
            merchant.setLatitude(new BigDecimal(body.get("latitude").toString()));
        }

        merchant.setBusinessLicense((String) body.get("businessLicense"));
        merchant.setIdCardFront((String) body.get("idCardFront"));
        merchant.setIdCardBack((String) body.get("idCardBack"));
        merchant.setLicenseImage((String) body.get("licenseImage"));

        merchant.setStatus(0); // 店铺未营业
        merchant.setVerifyStatus(1); // 待审核
        merchant.setRating(new BigDecimal("5.0"));
        merchant.setCommissionRate(new BigDecimal("0.05"));
        merchant.setBalance(BigDecimal.ZERO);
        merchant.setFrozenBalance(BigDecimal.ZERO);
        merchant.setCreateTime(now);
        merchant.setUpdateTime(now);

        merchantService.save(merchant);

        // 关联商户用户
        merchantUser.setMerchantId(merchant.getId());
        merchantUser.setPhone((String) body.get("contactPhone"));
        merchantUser.setUpdateTime(now);
        merchantUserService.updateById(merchantUser);

        log.info("入驻申请提交成功: merchantId={}", merchant.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("merchantId", merchant.getId());
        result.put("verifyStatus", 1);
        result.put("message", "申请已提交，请等待审核");

        return Result.success(result);
    }

    @PutMapping("/apply")
    @Operation(summary = "更新入驻申请（被拒绝后重新提交）")
    public Result<Map<String, Object>> updateApply(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {

        log.info("更新入驻申请: userId={}", userId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("请先提交入驻申请");
        }

        Merchant merchant = merchantService.getById(merchantUser.getMerchantId());
        if (merchant == null) {
            return Result.failed("商户信息不存在");
        }

        if (merchant.getVerifyStatus() != 3) {
            return Result.failed("当前状态不允许修改");
        }

        // 更新商户信息
        LocalDateTime now = LocalDateTime.now();
        if (body.get("shopName") != null) {
            merchant.setName((String) body.get("shopName"));
        }
        if (body.get("logo") != null) {
            merchant.setLogo((String) body.get("logo"));
        }
        if (body.get("description") != null) {
            merchant.setDescription((String) body.get("description"));
        }
        if (body.get("contactName") != null) {
            merchant.setContactName((String) body.get("contactName"));
        }
        if (body.get("contactPhone") != null) {
            merchant.setContactPhone((String) body.get("contactPhone"));
        }
        if (body.get("category") != null) {
            merchant.setCategory((String) body.get("category"));
        }
        if (body.get("province") != null) {
            merchant.setProvince((String) body.get("province"));
        }
        if (body.get("city") != null) {
            merchant.setCity((String) body.get("city"));
        }
        if (body.get("district") != null) {
            merchant.setDistrict((String) body.get("district"));
        }
        if (body.get("address") != null) {
            merchant.setAddress((String) body.get("address"));
        }
        if (body.get("longitude") != null) {
            merchant.setLongitude(new BigDecimal(body.get("longitude").toString()));
        }
        if (body.get("latitude") != null) {
            merchant.setLatitude(new BigDecimal(body.get("latitude").toString()));
        }
        if (body.get("businessLicense") != null) {
            merchant.setBusinessLicense((String) body.get("businessLicense"));
        }
        if (body.get("idCardFront") != null) {
            merchant.setIdCardFront((String) body.get("idCardFront"));
        }
        if (body.get("idCardBack") != null) {
            merchant.setIdCardBack((String) body.get("idCardBack"));
        }
        if (body.get("licenseImage") != null) {
            merchant.setLicenseImage((String) body.get("licenseImage"));
        }

        merchant.setVerifyStatus(1); // 重新提交审核
        merchant.setVerifyRemark(null);
        merchant.setUpdateTime(now);

        merchantService.updateById(merchant);

        log.info("入驻申请更新成功: merchantId={}", merchant.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("merchantId", merchant.getId());
        result.put("verifyStatus", 1);
        result.put("message", "申请已重新提交，请等待审核");

        return Result.success(result);
    }

    @GetMapping("/apply/status")
    @Operation(summary = "查询申请状态")
    public Result<Map<String, Object>> getApplyStatus(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {

        log.info("查询申请状态: userId={}", userId);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null) {
            return Result.failed("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();

        if (merchantUser.getMerchantId() == null) {
            result.put("verifyStatus", 0);
            result.put("statusText", "未提交申请");
            return Result.success(result);
        }

        Merchant merchant = merchantService.getById(merchantUser.getMerchantId());
        if (merchant == null) {
            result.put("verifyStatus", 0);
            result.put("statusText", "未提交申请");
            return Result.success(result);
        }

        result.put("verifyStatus", merchant.getVerifyStatus());
        result.put("verifyRemark", merchant.getVerifyRemark());
        result.put("verifyTime", merchant.getVerifyTime());

        String statusText;
        switch (merchant.getVerifyStatus()) {
            case 0:
                statusText = "未提交申请";
                break;
            case 1:
                statusText = "待审核";
                break;
            case 2:
                statusText = "审核通过";
                break;
            case 3:
                statusText = "审核拒绝";
                break;
            default:
                statusText = "未知状态";
        }
        result.put("statusText", statusText);

        // 返回商户信息
        Map<String, Object> merchantInfo = buildMerchantInfo(merchant);
        result.put("merchantInfo", merchantInfo);

        return Result.success(result);
    }

    @GetMapping("/info")
    @Operation(summary = "获取商户信息")
    public Result<Map<String, Object>> getMerchantInfo(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null) {
            return Result.failed("用户不存在");
        }

        if (merchantUser.getMerchantId() == null) {
            return Result.failed("请先提交入驻申请");
        }

        Merchant merchant = merchantService.getById(merchantUser.getMerchantId());
        if (merchant == null) {
            return Result.failed("商户信息不存在");
        }

        Map<String, Object> result = buildMerchantInfo(merchant);
        return Result.success(result);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新token")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, Object> body) {
        String refreshToken = (String) body.get("refreshToken");

        log.info("商户刷新token");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.failed("refreshToken不能为空");
        }

        if (!jwtUtil.validateToken(refreshToken)) {
            return Result.failed("refreshToken无效或已过期");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        MerchantUser merchantUser = merchantUserService.getById(userId);

        if (merchantUser == null || merchantUser.getStatus() != 1) {
            return Result.failed("用户不存在或已禁用");
        }

        // 生成新 token
        String newToken = jwtUtil.generateToken(merchantUser.getId(), merchantUser.getOpenid());
        String newRefreshToken = jwtUtil.generateRefreshToken(merchantUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", newToken);
        result.put("refreshToken", newRefreshToken);

        return Result.success(result);
    }

    /**
     * 构建商户信息Map
     */
    private Map<String, Object> buildMerchantInfo(Merchant merchant) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", merchant.getId());
        info.put("shopName", merchant.getName());
        info.put("logo", imageUrlUtil.generateUrl(merchant.getLogo()));
        info.put("banner", imageUrlUtil.generateUrl(merchant.getBanner()));
        info.put("description", merchant.getDescription());
        info.put("contactName", merchant.getContactName());
        info.put("contactPhone", merchant.getContactPhone());
        info.put("category", merchant.getCategory());
        info.put("province", merchant.getProvince());
        info.put("city", merchant.getCity());
        info.put("district", merchant.getDistrict());
        info.put("address", merchant.getAddress());
        info.put("longitude", merchant.getLongitude());
        info.put("latitude", merchant.getLatitude());
        info.put("businessLicense", merchant.getBusinessLicense());
        info.put("idCardFront", imageUrlUtil.generateUrl(merchant.getIdCardFront()));
        info.put("idCardBack", imageUrlUtil.generateUrl(merchant.getIdCardBack()));
        info.put("licenseImage", imageUrlUtil.generateUrl(merchant.getLicenseImage()));
        info.put("status", merchant.getStatus());
        info.put("verifyStatus", merchant.getVerifyStatus());
        info.put("verifyRemark", merchant.getVerifyRemark());
        info.put("rating", merchant.getRating());
        info.put("balance", merchant.getBalance());
        return info;
    }
}
