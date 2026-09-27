package com.zhibiao.platform.audit.application;

import com.zhibiao.platform.audit.domain.SysAuditLog;
import com.zhibiao.platform.audit.infrastructure.SysAuditLogMapper;
import com.zhibiao.platform.shared.security.SecurityUtils;
import com.zhibiao.platform.shared.util.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Map;

/**
 * 操作审计：登录/退出/下载/授权变更等关键动作落库。日志不打印密钥与完整敏感文档。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final SysAuditLogMapper auditMapper;

    public void record(String action, String objectType, String objectId, String projectId,
                       Map<String, Object> detail, String result) {
        try {
            SysAuditLog log0 = new SysAuditLog();
            try {
                var userId = SecurityUtils.currentUserId();
                log0.setUserId(userId);
            } catch (Exception ignored) {
            }
            log0.setAction(action);
            log0.setObjectType(objectType);
            log0.setObjectId(objectId);
            log0.setProjectId(projectId);
            if (detail != null && !detail.isEmpty()) {
                log0.setDetailJson(Json.write(detail));
            }
            log0.setResult(result == null ? "SUCCESS" : result);
            log0.setCreatedAt(Instant.now());
            try {
                var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                var req = attrs.getRequest();
                String xff = req.getHeader("X-Forwarded-For");
                log0.setIp(xff != null && !xff.isBlank() ? xff.split(",")[0].trim() : req.getRemoteAddr());
                String ua = req.getHeader("User-Agent");
                if (ua != null && ua.length() > 255) {
                    ua = ua.substring(0, 255);
                }
                log0.setUserAgent(ua);
            } catch (Exception ignored) {
            }
            auditMapper.insert(log0);
        } catch (Exception e) {
            log.warn("audit write failed for action={}: {}", action, e.getMessage());
        }
    }
}
