-- V3: 素材/模板库：分类、标签、素材档案、模板档案
CREATE TABLE library_category (
  id         VARCHAR(32)  NOT NULL,
  name       VARCHAR(64)  NOT NULL,
  kind       VARCHAR(16)  NOT NULL COMMENT 'MATERIAL/TEMPLATE',
  parent_id  VARCHAR(32)  NULL,
  sort       INT          NOT NULL DEFAULT 0,
  created_at DATETIME(6)  NOT NULL,
  updated_at DATETIME(6)  NOT NULL,
  created_by VARCHAR(32)  NULL,
  updated_by VARCHAR(32)  NULL,
  deleted    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_category (kind, name, parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE library_tag (
  id         VARCHAR(32) NOT NULL,
  name       VARCHAR(64) NOT NULL,
  kind       VARCHAR(16) NOT NULL COMMENT 'MATERIAL/TEMPLATE',
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  created_by VARCHAR(32) NULL,
  updated_by VARCHAR(32) NULL,
  deleted    TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tag (kind, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE library_asset_tag (
  asset_id  VARCHAR(32) NOT NULL COMMENT '指向素材/模板档案 id',
  tag_id    VARCHAR(32) NOT NULL,
  PRIMARY KEY (asset_id, tag_id),
  KEY idx_lat_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE material_profile (
  id           VARCHAR(32)  NOT NULL COMMENT '素材档案，内容文件引用资产体系',
  asset_id     VARCHAR(32)  NOT NULL,
  project_id   VARCHAR(32)  NULL COMMENT '为空表示组织级',
  visibility   VARCHAR(16)  NOT NULL DEFAULT 'PRIVATE' COMMENT 'PRIVATE/PROJECT/ORG',
  owner_user_id VARCHAR(32) NOT NULL,
  material_type VARCHAR(32) NOT NULL COMMENT 'COMPANY_PROFILE/CASE/CERTIFICATE/COPY/OTHER',
  company_name VARCHAR(128) NULL,
  valid_from   DATETIME(6)  NULL COMMENT '有效期字段，用于列表过滤',
  valid_until  DATETIME(6)  NULL,
  remark       VARCHAR(512) NULL,
  version      INT          NOT NULL DEFAULT 0,
  created_at   DATETIME(6)  NOT NULL,
  updated_at   DATETIME(6)  NOT NULL,
  created_by   VARCHAR(32)  NULL,
  updated_by   VARCHAR(32)  NULL,
  deleted      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_material_asset (asset_id),
  KEY idx_material_project_vis (project_id, visibility),
  KEY idx_material_valid_until (valid_until),
  KEY idx_material_type (material_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE template_profile (
  id            VARCHAR(32)  NOT NULL COMMENT '模板档案（文档排版模板，与招标要求成果是不同类别）',
  asset_id      VARCHAR(32)  NOT NULL,
  project_id    VARCHAR(32)  NULL,
  visibility    VARCHAR(16)  NOT NULL DEFAULT 'PRIVATE' COMMENT 'PRIVATE/PROJECT/ORG',
  owner_user_id VARCHAR(32)  NOT NULL,
  template_type VARCHAR(32)  NOT NULL COMMENT 'COMMERCIAL/TECHNICAL/OFFICIAL/OTHER',
  status        VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/ARCHIVED',
  published_version_id VARCHAR(32) NULL COMMENT '已发布版本指针，可变，乐观锁保护',
  version       INT          NOT NULL DEFAULT 0,
  created_at    DATETIME(6)  NOT NULL,
  updated_at    DATETIME(6)  NOT NULL,
  created_by    VARCHAR(32)  NULL,
  updated_by    VARCHAR(32)  NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_template_asset (asset_id),
  KEY idx_template_project_vis (project_id, visibility),
  KEY idx_template_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
