package com.lingxian.user.config;

import com.lingxian.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * JWT认证过滤器
 * 从Authorization头中解析token，提取用户ID并设置到X-User-Id请求头
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
                    if (userId != null) {
                        // 创建包装请求，添加X-User-Id头
                        HttpServletRequest wrappedRequest = new UserIdHeaderWrapper(request, userId);
                        filterChain.doFilter(wrappedRequest, response);
                        return;
                    }
                }
            } catch (Exception e) {
                log.debug("Token验证失败: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 请求包装器，用于添加X-User-Id请求头
     */
    private static class UserIdHeaderWrapper extends HttpServletRequestWrapper {
        private final Long userId;
        private final Map<String, String> customHeaders = new HashMap<>();

        public UserIdHeaderWrapper(HttpServletRequest request, Long userId) {
            super(request);
            this.userId = userId;
            customHeaders.put("X-User-Id", String.valueOf(userId));
        }

        @Override
        public String getHeader(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return headerValue;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (customHeaders.containsKey(name)) {
                return Collections.enumeration(Collections.singletonList(customHeaders.get(name)));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new HashSet<>(customHeaders.keySet());
            Enumeration<String> e = super.getHeaderNames();
            while (e.hasMoreElements()) {
                names.add(e.nextElement());
            }
            return Collections.enumeration(names);
        }
    }
}
