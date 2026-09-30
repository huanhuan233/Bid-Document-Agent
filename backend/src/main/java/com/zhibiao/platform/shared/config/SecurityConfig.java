package com.zhibiao.platform.shared.config;

import com.zhibiao.platform.identity.infrastructure.DelegatingSessionIdResolver;
import com.zhibiao.platform.identity.infrastructure.RestAuthenticationEntryPoint;
import com.zhibiao.platform.shared.exception.ErrorCodes;
import com.zhibiao.platform.shared.util.Json;
import com.zhibiao.platform.shared.web.ApiResponse;
import com.zhibiao.platform.shared.web.TraceIdFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 安全与会话装配：
 * - 认证：服务端会话（Spring Session + Redis），浏览器 HttpOnly SESSION Cookie；
 *   兼容 Authorization: Bearer 携带同一会话标识（见 DelegatingSessionIdResolver）。
 * - CSRF：CookieCsrfTokenRepository（XSRF-TOKEN Cookie，前端回传 X-XSRF-TOKEN 头）；
 *   /api/v1/auth/login 与携带 Bearer 头的请求不强制 CSRF（Bearer 调用不存在浏览器自动携带凭据的 CSRF 面）。
 * - 401/403：返回统一 ApiResponse JSON（code=8888 时前端跳转登录页）。
 * - CORS：允许配置的前端开发地址携带凭据跨域（联调直连后端时使用；dev 代理场景不触发）。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String BEARER_PREFIX = "Bearer ";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /** 会话标识解析：SESSION Cookie 优先，兼容 Bearer。检测到该 Bean 时 Spring Boot 不再装配默认解析器。 */
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return new DelegatingSessionIdResolver();
    }

    /** 请求追踪：X-Request-Id 透传/生成，写入 MDC 与响应头，优先级最高。 */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration() {
        FilterRegistrationBean<TraceIdFilter> reg = new FilterRegistrationBean<>(new TraceIdFilter());
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${zhibiao.cors.allowed-origins:http://localhost:10032,http://127.0.0.1:10032}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList());
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** 携带 Bearer 会话标识的请求：跳过 CSRF（与 Cookie 来源区分）。 */
    static RequestMatcher bearerRequests() {
        return request -> {
            String header = request.getHeader("Authorization");
            return header != null && header.startsWith(BEARER_PREFIX) && header.length() > BEARER_PREFIX.length();
        };
    }

    static AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, e) -> {
            response.setStatus(403);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(Json.write(ApiResponse.fail(ErrorCodes.FORBIDDEN, "无权访问该资源")));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(bearerRequests())
                        .ignoringRequestMatchers("/api/v1/auth/login"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/csrf",
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                        .accessDeniedHandler(restAccessDeniedHandler()))
                // 会话由 Spring Session + Redis 管理；按需创建（登录时创建）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession());
        return http.build();
    }
}