package com.zhibiao.platform.shared.web;

import org.slf4j.MDC;

/**
 * 请求追踪标识：从 X-Request-Id 透传或生成，写入 MDC 与响应头。
 */
public final class TraceIdFilter extends org.springframework.web.filter.OncePerRequestFilter {

    public static final String HEADER = "X-Request-Id";
    private static final String MDC_KEY = "traceId";
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    public static String currentTraceId() {
        String id = CURRENT.get();
        return id == null ? "" : id;
    }

    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                                    jakarta.servlet.http.HttpServletResponse response,
                                    jakarta.servlet.FilterChain chain) throws jakarta.servlet.ServletException, java.io.IOException {
        String traceId = request.getHeader(HEADER);
        if (traceId == null || traceId.isBlank() || traceId.length() > 64) {
            traceId = com.zhibiao.platform.shared.util.Ids.shortId();
        }
        MDC.put(MDC_KEY, traceId);
        CURRENT.set(traceId);
        response.setHeader(HEADER, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
            CURRENT.remove();
        }
    }
}
