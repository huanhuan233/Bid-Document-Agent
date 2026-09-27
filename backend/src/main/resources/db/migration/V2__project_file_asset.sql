-- V2: 项目、文件、资产与版本
CREATE TABLE biz_project (
  id            VARCHAR(32)  NOT NULL,
  name          VARCHAR(128) NOT NULL,
  code          VARCHAR(64)  NOT NULL,
  description   VARCHAR(512) NULL,
  status        VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/ARCHIVED',
  owner_user_id VARCHAR(32)  NOT NULL,
  version       INT          NOT NULL DEFAULT 0,
  created_at    DATETIME(6)  NOT NULL,
  updated_at    DATETIME(6)  NOT NULL,
  created_by    VARCHAR(32)  NULL,
  updated_by    VARCHAR(32)  NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_project_code (code),
  KEY idx_project_owner (owner_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE biz_project_member (
  id         VARCHAR(32) NOT NULL,
  project_id VARCHAR(32) NOT NULL,
  user_id    VARCHAR(32) NOT NULL,
  role       VARCHAR(16) NOT NULL COMMENT 'OWNER/EDITOR/VIEWER',
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  created_by VARCHAR(32) NULL,
  updated_by VARCHAR(32) NULL,
  deleted    TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_member_project_user (project_id, user_id),
  KEY idx_member_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE file_object (
  id            VARCHAR(32)  NOT NULL COMMENT '不可变物理文件元数据',
  project_id    VARCHAR(32)  NULL COMMENT '归属项目，可为空（组织级）',
  bucket        VARCHAR(64)  NOT NULL,
  object_key    VARCHAR(512) NOT NULL COMMENT '服务端生成，含随机标识，不用用户文件名做路径',
  original_name VARCHAR(255) NOT NULL,
  size_bytes    BIGINT       NOT NULL,
  media_type    VARCHAR(128) NOT NULL,
  ext           VARCHAR(32)  NOT NULL,
  sha256        VARCHAR(64)  NULL,
  category      VARCHAR(32)  NOT NULL COMMENT 'originals/materials/templates/results/reports/temp',
  status        VARCHAR(16)  NOT NULL DEFAULT 'UPLOADING' COMMENT 'UPLOADING/READY/DELETED/FAILED',
  uploader_id   VARCHAR(32)  NOT NULL,
  created_at    DATETIME(6)  NOT NULL,
  updated_at    DATETIME(6)  NOT NULL,
  created_by    VARCHAR(32)  NULL,
  updated_by    VARCHAR(32)  NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_file_object_key (bucket, object_key(191)),
  KEY idx_file_project (project_id),
  KEY idx_file_uploader (uploader_id),
  KEY idx_file_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE biz_asset (
  id                         VARCHAR(32)  NOT NULL COMMENT '逻辑成果对象',
  project_id                 VARCHAR(32)  NOT NULL,
  category                   VARCHAR(32)  NOT NULL COMMENT 'SOURCE_DOCUMENT/MATERIAL/TEMPLATE/TENDER_REQUIREMENT/BID_DOCUMENT/REVIEW_REPORT/OTHER',
  name                       VARCHAR(255) NOT NULL,
  current_published_version_id VARCHAR(32) NULL COMMENT '发布指针，可变，乐观锁保护',
  owner_user_id              VARCHAR(32)  NOT NULL,
  version                    INT          NOT NULL DEFAULT 0,
  created_at                 DATETIME(6)  NOT NULL,
  updated_at                 DATETIME(6)  NOT NULL,
  created_by                 VARCHAR(32)  NULL,
  updated_by                 VARCHAR(32)  NULL,
  deleted                    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_asset_project_category (project_id, category),
  KEY idx_asset_published (current_published_version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE biz_asset_version (
  id             VARCHAR(32)  NOT NULL COMMENT '不可变版本',
  asset_id       VARCHAR(32)  NOT NULL,
  version_no     INT          NOT NULL,
  file_object_id VARCHAR(32)  NOT NULL,
  status         VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED/ARCHIVED',
  summary        VARCHAR(1024) NULL,
  meta_json      JSON         NULL COMMENT '扩展元数据，不替代结构化字段',
  created_by     VARCHAR(32)  NOT NULL,
  created_at     DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_asset_version (asset_id, version_no),
  KEY idx_av_file (file_object_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE biz_asset_relation (
  id            VARCHAR(32) NOT NULL,
  from_version_id VARCHAR(32) NOT NULL COMMENT '来源固定版本',
  to_version_id VARCHAR(32) NOT NULL COMMENT '目标固定版本',
  relation_type VARCHAR(32) NOT NULL COMMENT 'DERIVED_FROM/USES_TEMPLATE/REFERENCES_MATERIAL/REVIEW_TARGET/REVISION_BASE',
  created_by    VARCHAR(32) NOT NULL,
  created_at    DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_relation (from_version_id, to_version_id, relation_type),
  KEY idx_rel_to (to_version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
