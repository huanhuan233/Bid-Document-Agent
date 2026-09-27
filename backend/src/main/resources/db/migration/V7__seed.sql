-- V7: 种子数据：角色、权限。管理员用户由应用启动器依据环境变量创建，不硬编码默认密码。
INSERT INTO sys_role (id, code, name, description, created_at, updated_at, deleted) VALUES
 ('r0000000000000000000000000000001', 'R_SUPER', '系统管理员', '平台管理员，全部权限', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('r0000000000000000000000000000002', 'R_USER',  '普通用户',   '普通业务用户',        UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0);

INSERT INTO sys_permission (id, code, name, type, sort, created_at, updated_at, deleted) VALUES
 ('p0000000000000000000000000000001', 'menu:workbench',       '工作台',       'MENU', 1,  UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000002', 'menu:parse',           '标书解析',     'MENU', 2,  UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000003', 'menu:generate',        '标书生成',     'MENU', 3,  UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000004', 'menu:review',          '标书审查',     'MENU', 4,  UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000005', 'menu:template',        '模板中心',     'MENU', 5,  UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000006', 'menu:material',        '企业素材库',   'MENU', 6,  UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000007', 'menu:report',          '审查报告',     'MENU', 7,  UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000008', 'menu:system',          '系统设置',     'MENU', 8,  UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000011', 'api:project:manage',   '项目管理',     'API',  21, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000012', 'api:file:manage',      '文件管理',     'API',  22, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000013', 'api:library:manage',   '素材模板管理', 'API',  23, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000014', 'api:asset:manage',     '成果资产管理', 'API',  24, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000015', 'api:task:manage',      '任务管理',     'API',  25, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000016', 'api:binding:manage',   '工作流绑定管理','API', 26, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000017', 'api:system:manage',    '系统管理',     'API',  27, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0),
 ('p0000000000000000000000000000018', 'api:audit:query',      '审计查询',     'API',  28, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 'r0000000000000000000000000000001', id FROM sys_permission;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 'r0000000000000000000000000000002', id FROM sys_permission WHERE code NOT IN ('menu:system', 'api:system:manage', 'api:binding:manage', 'api:audit:query');
