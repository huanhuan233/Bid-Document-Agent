package com.zhibiao.platform.identity.controller;

import com.zhibiao.platform.audit.application.AuditService;
import com.zhibiao.platform.identity.application.AuthService;
import com.zhibiao.platform.identity.application.dto.AuthDtos.LoginRequest;
import com.zhibiao.platform.identity.application.dto.AuthDtos.LoginResponse;
import com.zhibiao.platform.identity.application.dto.AuthDtos.UserInfoVO;
import com.zhibiao.platform.shared.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * /api/v1/auth：登录、退出、当前用户、CSRF、修改密码。
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuditService auditService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest req, HttpServletRequest request) {
        try {
            LoginResponse resp = authService.login(req, clientIp(request));
            auditService.record("LOGIN", "user", resp.userId(), null,
                    Map.of("username", req.userName()), "SUCCESS");
            return ApiResponse.ok(resp);
        } catch (RuntimeException e) {
            auditService.record("LOGIN", "user", null, null,
                    Map.of("username", String.valueOf(req == null ? null : req.userName())), "FAILED");
            throw e;
        }
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout();
        auditService.record("LOGOUT", "user", null, null, Map.of(), "SUCCESS");
        return ApiResponse.ok();
    }

    @GetMapping("/getUserInfo")
    public ApiResponse<UserInfoVO> userInfo() {
        return ApiResponse.ok(authService.currentUser());
    }

    @GetMapping("/csrf")
    public ApiResponse<Map<String, String>> csrf(CsrfToken token) {
        // SPA + Cookie 会话场景：前端从该端点或 XSRF-TOKEN Cookie 取值，回传 X-XSRF-TOKEN
        return ApiResponse.ok(Map.of("token", token.getToken(), "header", token.getHeaderName()));
    }

    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body) {
        String userId = com.zhibiao.platform.shared.security.SecurityUtils.currentUserId();
        authService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        auditService.record("CHANGE_PASSWORD", "user", userId, null, Map.of(), "SUCCESS");
        return ApiResponse.ok();
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
