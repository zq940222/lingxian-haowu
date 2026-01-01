package com.lingxian.merchant.config;

import com.lingxian.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器 - 商户端
 * 从Authorization头提取JWT，验证后将商户用户ID设置到请求头
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);

            try {
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserId(token);
                    // 使用 MutableHttpServletRequest 包装原请求，添加自定义头
                    MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
                    mutableRequest.putHeader("X-Merchant-User-Id", String.valueOf(userId));
                    filterChain.doFilter(mutableRequest, response);
                    return;
                }
            } catch (Exception e) {
                log.warn("JWT验证失败: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
