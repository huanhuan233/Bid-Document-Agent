package com.zhibiao.platform.audit.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

@Data
@TableName("integration_call_log")
public class IntegrationCallLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskId;
    private String bindingId;
    /** OUT / IN */
    private String direction;
    private String action;
    private String endpoint;
    private Integer httpStatus;
    private Integer durationMs;
    /** 脱敏请求摘要，不含密钥 */
    private String requestDigest;
    private String responseDigest;
    private String errorCode;
    private String errorMessage;
    /** real / mock，mock 结果明确标注 */
    private String adapterMode;
    private Instant createdAt;
}
