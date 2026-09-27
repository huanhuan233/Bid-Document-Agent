package com.zhibiao.platform.audit.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

@Data
@TableName("sys_audit_log")
public class SysAuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String username;
    private String action;
    private String objectType;
    private String objectId;
    private String projectId;
    private String detailJson;
    private String ip;
    private String userAgent;
    /** SUCCESS / DENIED / FAILED */
    private String result;
    private Instant createdAt;
}
