package com.zhibiao.platform.identity.application;

import com.zhibiao.platform.shared.exception.BizException;
import com.zhibiao.platform.shared.security.RequirePermission;
import com.zhibiao.platform.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 后端逐次校验对象/接口访问，不依赖前端隐藏按钮。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    @Before("@annotation(requirePermission)")
    public void check(JoinPoint jp, RequirePermission requirePermission) {
        String userId = SecurityUtils.currentUserId();
        for (String code : requirePermission.value()) {
            if (permissionService.hasPermission(userId, code)) {
                return;
            }
        }
        throw BizException.forbidden("缺少权限：" + String.join("/", requirePermission.value()));
    }
}
