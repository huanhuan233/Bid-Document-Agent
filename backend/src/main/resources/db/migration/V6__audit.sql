-- V6: 审计域：操作审计与外部调用审计
CREATE TABLE sys_audit_log (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  user_id     VARCHAR(32)   NULL,
  username    VARCHAR(64)   NULL,
  action      VARCHAR(64)   NOT NULL COMMENT 'LOGIN/LOGOUT/FILE_DOWNLOAD/PROJECT_CREATE/... ',
  object_type VARCHAR(64)   NULL,
  object_id   VARCHAR(64)   NULL,
  project_id  VARCHAR(32)   NULL,
  detail_json JSON          NULL,
  ip          VARCHAR(64)   NULL,
  user_agent  VARCHAR(255)  NULL,
  result      VARCHAR(16)   NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS/DENIED/FAILED',
  created_at  DATETIME(6)   NOT NULL,
  PRIMARY KEY (id),
  KEY idx_audit_user_time (user_id, created_at),
  KEY idx_audit_action_time (action, created_at),
  KEY idx_audit_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE integration_call_log (
  id               BIGINT        NOT NULL AUTO_INCREMENT,
  task_id          VARCHAR(32)   NULL,
  binding_id       VARCHAR(32)   NULL,
  direction        VARCHAR(16)   NOT NULL COMMENT 'OUT/IN',
  action           VARCHAR(64)   NOT NULL COMMENT 'execute/query/cancel/upload_file',
  endpoint         VARCHAR(255)  NULL,
  http_status      INT           NULL,
  duration_ms      INT           NULL,
  request_digest   VARCHAR(1024) NULL COMMENT '脱敏请求摘要，不含密钥',
  response_digest  VARCHAR(1024) NULL COMMENT '脱敏结果引用',
  error_code       VARCHAR(64)   NULL,
  error_message    VARCHAR(1024) NULL,
  adapter_mode     VARCHAR(16)   NOT NULL COMMENT 'real/mock，mock 结果明确标注',
  created_at       DATETIME(6)   NOT NULL,
  PRIMARY KEY (id),
  KEY idx_icl_task (task_id),
  KEY idx_icl_binding_time (binding_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
