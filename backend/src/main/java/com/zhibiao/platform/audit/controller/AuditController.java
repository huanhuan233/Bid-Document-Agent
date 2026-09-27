package com.zhibiao.platform.audit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhibiao.platform.audit.domain.IntegrationCallLog;
import com.zhibiao.platform.audit.domain.SysAuditLog;
import com.zhibiao.platform.audit.infrastructure.IntegrationCallLogMapper;
import com.zhibiao.platform.audit.infrastructure.SysAuditLogMapper;
import com.zhibiao.platform.shared.security.RequirePermission;
import com.zhibiao.platform.shared.web.ApiResponse;
import com.zhibiao.platform.shared.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * /api/v1/audit：审计查询。
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final SysAuditLogMapper auditMapper;
    private final IntegrationCallLogMapper callLogMapper;

    @GetMapping("/logs")
    @RequirePermission("api:audit:query")
    public ApiResponse<PageResult<SysAuditLog>> logs(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
                                                     @RequestParam(required = false) String action,
                                                     @RequestParam(required = false) String userId,
                                                     @RequestParam(required = false, name = "project_id") String projectId) {
        LambdaQueryWrapper<SysAuditLog> qw = new LambdaQueryWrapper<>();
        qw.eq(StringUtils.hasText(action), SysAuditLog::getAction, action)
                .eq(StringUtils.hasText(userId), SysAuditLog::getUserId, userId)
                .eq(StringUtils.hasText(projectId), SysAuditLog::getProjectId, projectId)
                .orderByDesc(SysAuditLog::getId);
        Page<SysAuditLog> p = auditMapper.selectPage(new Page<>(page, pageSize), qw);
        return ApiResponse.ok(PageResult.of(p.getRecords(), p.getTotal(), page, pageSize));
    }

    @GetMapping("/integration")
    @RequirePermission("api:audit:query")
    public ApiResponse<PageResult<IntegrationCallLog>> integration(@RequestParam(defaultValue = "1") long page,
                                                                   @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
                                                                   @RequestParam(required = false, name = "task_id") String taskId,
                                                                   @RequestParam(required = false, name = "binding_id") String bindingId) {
        LambdaQueryWrapper<IntegrationCallLog> qw = new LambdaQueryWrapper<>();
        qw.eq(StringUtils.hasText(taskId), IntegrationCallLog::getTaskId, taskId)
                .eq(StringUtils.hasText(bindingId), IntegrationCallLog::getBindingId, bindingId)
                .orderByDesc(IntegrationCallLog::getId);
        Page<IntegrationCallLog> p = callLogMapper.selectPage(new Page<>(page, pageSize), qw);
        return ApiResponse.ok(PageResult.of(p.getRecords(), p.getTotal(), page, pageSize));
    }
}
