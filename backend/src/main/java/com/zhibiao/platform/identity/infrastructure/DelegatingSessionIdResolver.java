package com.zhibiao.platform.identity.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.session.Session;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;

import java.util.List;

/**
 * 会话标识解析：优先标准 SESSION Cookie（浏览器），兼容 Authorization: Bearer（Soybean 请求封装）。
 * 记录鉴权来源，供 CSRF 判定：Bearer 头的 API 调用不强制 CSRF；Cookie 会话的变更请求强制 CSRF。
 */
public class DelegatingSessionIdResolver implements HttpSessionIdResolver {

    public static final String AUTH_SOURCE_ATTR = DelegatingSessionIdResolver.class.getName() + ".AUTH_SOURCE";
    public static final String AUTH_SOURCE_COOKIE = "cookie";

    private final CookieHttpSessionIdResolver delegate = new CookieHttpSessionIdResolver();

    public DelegatingSessionIdResolver() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("SESSION");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setSameSite("Lax");
        delegate.setCookieSerializer(serializer);
    }

    @Override
    public List<String> resolveSessionIds(HttpServletRequest request) {
        List<String> ids = delegate.resolveSessionIds(request);
        if (!ids.isEmpty()) {
            request.setAttribute(AUTH_SOURCE_ATTR, AUTH_SOURCE_COOKIE);
            return ids;
        }
        String h = request.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ") && h.length() > 7) {
            return List.of(h.substring(7));
        }
        return List.of();
    }

    @Override
    public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
        delegate.setSessionId(request, response, sessionId);
    }

    @Override
    public void expireSession(HttpServletRequest request, HttpServletResponse response) {
        delegate.expireSession(request, response);
    }
}
