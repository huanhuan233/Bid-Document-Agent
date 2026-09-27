-- V4: Dify 接入域：工作流绑定、绑定版本、文件映射
CREATE TABLE workflow_binding (
  id                VARCHAR(32)  NOT NULL,
  action_code       VARCHAR(64)  NOT NULL COMMENT '外层路由码：PARSE/GENERATE/REVIEW/REVISE 等',
  name              VARCHAR(128) NOT NULL,
  api_base_url      VARCHAR(255) NOT NULL,
  api_key_enc       VARBINARY(512) NOT NULL COMMENT '主密钥 AES-GCM 加密后的 API Key，读取只返回掩码',
  app_id            VARCHAR(128) NULL COMMENT '应用标识，如实际接口需要',
  input_mapping_json  JSON       NULL COMMENT '输入映射',
  output_mapping_json JSON       NULL COMMENT '输出映射',
  input_schema_json   JSON       NULL,
  output_schema_json  JSON       NULL,
  timeout_ms        INT          NOT NULL DEFAULT 300000,
  concurrency_limit INT          NOT NULL DEFAULT 2,
  enabled           TINYINT      NOT NULL DEFAULT 1,
  supported_capabilities JSON    NULL COMMENT '支持能力：execute/query/cancel/upload 等',
  current_revision  INT          NOT NULL DEFAULT 0 COMMENT '当前绑定版本号',
  version           INT          NOT NULL DEFAULT 0,
  created_at        DATETIME(6)  NOT NULL,
  updated_at        DATETIME(6)  NOT NULL,
  created_by        VARCHAR(32)  NULL,
  updated_by        VARCHAR(32)  NULL,
  deleted           TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_binding_action (action_code, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE workflow_binding_revision (
  id            VARCHAR(32) NOT NULL COMMENT '绑定配置快照，任务创建时锁定',
  binding_id    VARCHAR(32) NOT NULL,
  revision      INT         NOT NULL,
  config_json   JSON        NOT NULL COMMENT '绑定配置快照（不含明文密钥，密钥引用 binding 主表）',
  created_by    VARCHAR(32) NOT NULL,
  created_at    DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_binding_revision (binding_id, revision)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE dify_file_mapping (
  id            VARCHAR(32) NOT NULL,
  binding_id    VARCHAR(32) NOT NULL,
  dify_user     VARCHAR(64) NOT NULL COMMENT '运行用户标识，映射不可跨作用域复用',
  file_object_id VARCHAR(32) NOT NULL,
  dify_file_id  VARCHAR(128) NOT NULL COMMENT 'Dify 上传返回的文件 id',
  created_at    DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_dify_file_map (binding_id, dify_user, file_object_id),
  KEY idx_dfm_file (file_object_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
