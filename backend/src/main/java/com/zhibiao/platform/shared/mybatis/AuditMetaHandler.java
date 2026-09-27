package com.zhibiao.platform.shared.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.zhibiao.platform.shared.security.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 审计字段自动填充（UTC）。
 */
@Component
public class AuditMetaHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Instant now = Instant.now();
        strictInsertFill(metaObject, "createdAt", Instant.class, now);
        strictInsertFill(metaObject, "updatedAt", Instant.class, now);
        String userId = safeUserId();
        strictInsertFill(metaObject, "createdBy", String.class, userId);
        strictInsertFill(metaObject, "updatedBy", String.class, userId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", Instant.class, Instant.now());
        strictUpdateFill(metaObject, "updatedBy", String.class, safeUserId());
    }

    private String safeUserId() {
        try {
            return SecurityUtils.currentUserId();
        } catch (Exception e) {
            return "system";
        }
    }
}
