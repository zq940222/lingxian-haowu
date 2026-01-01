package com.lingxian.user.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingxian.common.entity.User;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.UserService;
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
 * 用户认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/user/auth")
@Tag(name = "用户端-认证", description = "用户登录认证接口")
public class UserAuthController {

    private final WxMaService wxMaService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final ImageUrlUtil imageUrlUtil;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public UserAuthController(
            @Qualifier("userWxMaService") WxMaService wxMaService,
            UserService userService,
            JwtUtil jwtUtil,
            ImageUrlUtil imageUrlUtil) {
        this.wxMaService = wxMaService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.imageUrlUtil = imageUrlUtil;
    }

    @PostMapping("/login")
    @Operation(summary = "微信登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        String nickname = (String) body.get("nickname");
        String avatar = (String) body.get("avatar");
        Integer gender = body.get("gender") != null ? Integer.parseInt(body.get("gender").toString()) : 0;

        log.info("微信登录: code={}, nickname={}", code, nickname);

        if (code == null || code.isEmpty()) {
            return Result.failed("code不能为空");
        }

        try {
            String openid;
            String unionId = null;

            // 开发环境支持模拟登录
            if ("dev".equals(activeProfile) && code.startsWith("mock_")) {
                // 模拟登录：code 格式为 mock_用户ID 或 mock_openid_xxx
                if (code.startsWith("mock_openid_")) {
                    openid = code.substring(5); // 去掉 mock_
                } else {
                    openid = "dev_openid_" + code.substring(5);
                }
                log.info("开发环境模拟登录: openid={}", openid);
            } else {
                // 正式环境调用微信接口获取 openid 和 session_key
                WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
                openid = session.getOpenid();
                unionId = session.getUnionid();
            }

            log.info("微信登录成功: openid={}, unionId={}", openid, unionId);

            // 查找或创建用户
            User user = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getOpenid, openid));

            LocalDateTime now = LocalDateTime.now();
            boolean isNewUser = false;

            if (user == null) {
                // 创建新用户
                user = new User();
                user.setOpenid(openid);
                user.setUnionId(unionId);
                user.setNickname(nickname != null ? nickname : "微信用户");
                user.setAvatar(avatar != null ? avatar : "");
                user.setGender(gender);
                user.setPoints(0);
                user.setBalance(BigDecimal.ZERO);
                user.setStatus(1);
                user.setLastLoginTime(now);
                user.setCreateTime(now);
                user.setUpdateTime(now);
                userService.save(user);
                isNewUser = true;
                log.info("创建新用户: userId={}", user.getId());
            } else {
                // 更新登录时间和用户信息
                user.setLastLoginTime(now);
                user.setUpdateTime(now);
                if (nickname != null && !nickname.isEmpty()) {
                    user.setNickname(nickname);
                }
                if (avatar != null && !avatar.isEmpty()) {
                    user.setAvatar(avatar);
                }
                userService.updateById(user);
                log.info("用户登录: userId={}", user.getId());
            }

            // 生成 token
            String token = jwtUtil.generateToken(user.getId(), openid);
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            // 返回用户信息和 token
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("refreshToken", refreshToken);
            result.put("isNewUser", isNewUser);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", imageUrlUtil.generateUrl(user.getAvatar()));
            userInfo.put("phone", user.getPhone());
            userInfo.put("gender", user.getGender());
            userInfo.put("points", user.getPoints());
            userInfo.put("balance", user.getBalance());
            result.put("userInfo", userInfo);

            return Result.success(result);

        } catch (WxErrorException e) {
            log.error("微信登录失败: {}", e.getMessage(), e);
            return Result.failed("微信登录失败: " + e.getError().getErrorMsg());
        } catch (Exception e) {
            log.error("登录异常: {}", e.getMessage(), e);
            return Result.failed("登录失败，请重试");
        }
    }

    @PostMapping("/phone")
    @Operation(summary = "获取手机号")
    public Result<Map<String, Object>> getPhoneNumber(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");

        log.info("获取手机号: userId={}, code={}", userId, code);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        if (code == null || code.isEmpty()) {
            return Result.failed("code不能为空");
        }

        try {
            // 通过 code 获取手机号
            WxMaPhoneNumberInfo phoneInfo = wxMaService.getUserService().getPhoneNoInfo(code);
            String phoneNumber = phoneInfo.getPhoneNumber();

            log.info("获取手机号成功: userId={}, phone={}", userId, phoneNumber);

            // 更新用户手机号
            User user = userService.getById(userId);
            if (user != null) {
                user.setPhone(phoneNumber);
                user.setUpdateTime(LocalDateTime.now());
                userService.updateById(user);
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
        log.info("检查登录状态: authorization={}", authorization);

        Map<String, Object> result = new HashMap<>();

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            result.put("isLogin", false);
            return Result.success(result);
        }

        String token = authorization.substring(7);
        if (jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserId(token);
            User user = userService.getById(userId);

            if (user != null && user.getStatus() == 1) {
                result.put("isLogin", true);

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", imageUrlUtil.generateUrl(user.getAvatar()));
                userInfo.put("phone", user.getPhone());
                userInfo.put("gender", user.getGender());
                userInfo.put("points", user.getPoints());
                userInfo.put("balance", user.getBalance());
                result.put("userInfo", userInfo);
            } else {
                result.put("isLogin", false);
            }
        } else {
            result.put("isLogin", false);
        }

        return Result.success(result);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新token")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, Object> body) {
        String refreshToken = (String) body.get("refreshToken");

        log.info("刷新token");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.failed("refreshToken不能为空");
        }

        if (!jwtUtil.validateToken(refreshToken)) {
            return Result.failed("refreshToken无效或已过期");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        User user = userService.getById(userId);

        if (user == null || user.getStatus() != 1) {
            return Result.failed("用户不存在或已禁用");
        }

        // 生成新 token
        String newToken = jwtUtil.generateToken(user.getId(), user.getOpenid());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", newToken);
        result.put("refreshToken", newRefreshToken);

        return Result.success(result);
    }
}
