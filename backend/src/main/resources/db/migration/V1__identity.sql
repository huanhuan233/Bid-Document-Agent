-- V1: 身份域：用户、角色、权限
CREATE TABLE sys_user (
  id            VARCHAR(32)  NOT NULL,
  username      VARCHAR(64)  NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  display_name  VARCHAR(64)  NOT NULL,
  email         VARCHAR(128) NULL,
  phone         VARCHAR(32)  NULL,
  status        VARCHAR(16)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
  is_super      TINYINT      NOT NULL DEFAULT 0,
  dify_user_id  VARCHAR(64)  NOT NULL COMMENT '后端生成的稳定 Dify 系统用户标识',
  version       INT          NOT NULL DEFAULT 0 COMMENT '乐观锁',
  created_at    DATETIME(6)  NOT NULL,
  updated_at    DATETIME(6)  NOT NULL,
  created_by    VARCHAR(32)  NULL,
  updated_by    VARCHAR(32)  NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_username (username),
  UNIQUE KEY uk_user_dify (dify_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE sys_role (
  id          VARCHAR(32) NOT NULL,
  code        VARCHAR(64) NOT NULL,
  name        VARCHAR(64) NOT NULL,
  description VARCHAR(255) NULL,
  created_at  DATETIME(6) NOT NULL,
  updated_at  DATETIME(6) NOT NULL,
  created_by  VARCHAR(32) NULL,
  updated_by  VARCHAR(32) NULL,
  deleted     TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE sys_permission (
  id         VARCHAR(32)  NOT NULL,
  code       VARCHAR(128) NOT NULL,
  name       VARCHAR(128) NOT NULL,
  type       VARCHAR(16)  NOT NULL COMMENT 'MENU/API/DATA',
  parent_id  VARCHAR(32)  NULL,
  sort       INT          NOT NULL DEFAULT 0,
  created_at DATETIME(6)  NOT NULL,
  updated_at DATETIME(6)  NOT NULL,
  created_by VARCHAR(32)  NULL,
  updated_by VARCHAR(32)  NULL,
  deleted    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE sys_user_role (
  user_id VARCHAR(32) NOT NULL,
  role_id VARCHAR(32) NOT NULL,
  PRIMARY KEY (user_id, role_id),
  KEY idx_ur_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE sys_role_permission (
  role_id       VARCHAR(32) NOT NULL,
  permission_id VARCHAR(32) NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  KEY idx_rp_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
