package com.lingxian.merchant.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用 session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 放行所有商户端接口 (开发阶段)
                .requestMatchers("/merchant/**").permitAll()
                // 放行 Swagger 文档
                .requestMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**").permitAll()
                // 放行健康检查
                .requestMatchers("/actuator/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
