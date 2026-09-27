package com.zhibiao.platform.shared.security;

import com.zhibiao.platform.shared.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户上下文。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser lu) {
            return lu;
        }
        throw new com.zhibiao.platform.shared.exception.BizException(
                com.zhibiao.platform.shared.exception.ErrorCodes.UNAUTHENTICATED, "未登录或会话已失效", 401);
    }

    public static String currentUserId() {
        return current().getUserId();
    }

    public static boolean isSuper() {
        try {
            return current().isSuperUser();
        } catch (BizException e) {
            return false;
        }
    }
}
