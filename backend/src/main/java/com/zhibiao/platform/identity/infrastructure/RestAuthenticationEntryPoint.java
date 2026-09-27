package com.zhibiao.platform.identity.infrastructure;

import com.zhibiao.platform.shared.exception.ErrorCodes;
import com.zhibiao.platform.shared.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * 未认证（401）：返回 code=8888，前端据此跳转登录页。
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws java.io.IOException {
        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                com.zhibiao.platform.shared.util.Json.write(ApiResponse.fail(ErrorCodes.UNAUTHENTICATED, "未登录或会话已失效")));
    }
}
