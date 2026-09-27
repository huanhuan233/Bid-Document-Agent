-- V5: 任务域：任务、执行尝试、事件、Outbox
CREATE TABLE biz_task (
  id                  VARCHAR(32)  NOT NULL,
  project_id          VARCHAR(32)  NOT NULL,
  action_code         VARCHAR(64)  NOT NULL,
  binding_id          VARCHAR(32)  NOT NULL,
  binding_revision_id VARCHAR(32)  NOT NULL COMMENT '创建时锁定的绑定版本',
  status              VARCHAR(24)  NOT NULL COMMENT 'QUEUED/SUBMITTING/RUNNING/RECONCILING/SUCCEEDED/FAILED/CANCEL_REQUESTED/CANCELLED/UNKNOWN',
  idempotency_key     VARCHAR(128) NOT NULL,
  input_refs_json     JSON         NOT NULL COMMENT '输入版本引用（创建时固定）',
  params_json         JSON         NULL,
  created_by          VARCHAR(32)  NOT NULL,
  current_attempt_id  VARCHAR(32)  NULL,
  result_version_id   VARCHAR(32)  NULL COMMENT '成功后的成果版本',
  fail_code           VARCHAR(64)  NULL,
  fail_message        VARCHAR(1024) NULL,
  version             INT          NOT NULL DEFAULT 0,
  created_at          DATETIME(6)  NOT NULL,
  updated_at          DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_task_idem (project_id, idempotency_key),
  KEY idx_task_project_status (project_id, status),
  KEY idx_task_status (status),
  KEY idx_task_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE biz_task_attempt (
  id                  VARCHAR(32)  NOT NULL,
  task_id             VARCHAR(32)  NOT NULL,
  attempt_no          INT          NOT NULL,
  status              VARCHAR(24)  NOT NULL COMMENT 'RUNNING/SUCCEEDED/FAILED/CANCELLED/UNKNOWN',
  lease_owner         VARCHAR(64)  NULL,
  lease_expires_at    DATETIME(6)  NULL,
  heartbeat_at        DATETIME(6)  NULL,
  remote_task_id      VARCHAR(128) NULL COMMENT 'Dify task_id',
  remote_run_id       VARCHAR(128) NULL COMMENT 'Dify workflow_run_id',
  request_fingerprint VARCHAR(64)  NULL COMMENT '请求摘要（SHA-256）',
  started_at          DATETIME(6)  NULL,
  finished_at         DATETIME(6)  NULL,
  end_reason          VARCHAR(1024) NULL,
  created_at          DATETIME(6)  NOT NULL,
  updated_at          DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_attempt_no (task_id, attempt_no),
  KEY idx_attempt_lease (status, lease_expires_at),
  KEY idx_attempt_remote (remote_run_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE biz_task_event (
  id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '全局递增游标',
  event_id   VARCHAR(64)  NOT NULL,
  task_id    VARCHAR(32)  NOT NULL,
  status     VARCHAR(24)  NOT NULL,
  stage      VARCHAR(64)  NULL,
  progress   INT          NULL COMMENT '可为空；无真实依据不伪造百分比',
  message    VARCHAR(1024) NULL,
  occurred_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_event_id (event_id),
  KEY idx_event_task_cursor (task_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE outbox_event (
  id            VARCHAR(32)  NOT NULL,
  aggregate_type VARCHAR(32) NOT NULL,
  aggregate_id  VARCHAR(32)  NOT NULL,
  event_type    VARCHAR(64)  NOT NULL,
  payload_json  JSON         NOT NULL,
  status        VARCHAR(16)  NOT NULL DEFAULT 'NEW' COMMENT 'NEW/SENT/FAILED',
  retry_count   INT          NOT NULL DEFAULT 0,
  next_retry_at DATETIME(6)  NULL,
  trace_id      VARCHAR(64)  NULL,
  created_at    DATETIME(6)  NOT NULL,
  sent_at       DATETIME(6)  NULL,
  PRIMARY KEY (id),
  KEY idx_outbox_dispatch (status, next_retry_at, created_at),
  KEY idx_outbox_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
