package com.lingxian.common.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置 - 用户端
 * 只有当配置了 wechat.miniapp.user.enabled=true 时才会加载
 */
@Data
@Configuration
@ConditionalOnProperty(prefix = "wechat.miniapp.user", name = "enabled", havingValue = "true")
@ConfigurationProperties(prefix = "wechat.miniapp.user")
public class WechatMiniAppConfig {

    private boolean enabled;
    private String appId;
    private String secret;

    @Bean("userWxMaService")
    public WxMaService wxMaService() {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(appId);
        config.setSecret(secret);

        WxMaService service = new WxMaServiceImpl();
        service.setWxMaConfig(config);
        return service;
    }
}
