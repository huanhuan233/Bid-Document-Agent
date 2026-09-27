import type { Bid } from '@/typings/bid';

/** 投标排版模板（与“招标要求成果”是两种对象，互不混淆） */
export const demoTemplates: Bid.TemplateVM[] = [
  {
    id: 'TP-001', name: '通用工程类投标模板', category: '标准投标模板', version: 'V2.4', industry: '建筑工程', scene: '工程投标',
    chapters: 12, usage: 286, updatedAt: '2026-09-19 14:32', creator: '张三', status: 'published',
    desc: '适用于市政、房建等通用工程类投标，含商务标与技术标完整章节。', coverTone: 0
  },
  {
    id: 'TP-002', name: '政府采购响应标模板', category: '标准投标模板', version: 'V1.8', industry: '政府事务', scene: '政府采购',
    chapters: 15, usage: 192, updatedAt: '2026-09-16 10:21', creator: '李四', status: 'published',
    desc: '按政府采购格式要求编制，含中小企业声明等标准附件。', coverTone: 1
  },
  {
    id: 'TP-005', name: '智慧城市技术方案模板', category: '技术标模板', version: 'V3.1', industry: '信息技术', scene: '智慧城市',
    chapters: 18, usage: 320, updatedAt: '2026-09-14 16:20', creator: '王五', status: 'published', recommended: true,
    desc: '数据中台、智能应用、运营指挥中心等技术方案章节齐全。', coverTone: 2
  },
  {
    id: 'TP-003', name: '企业综合商务模板', category: '商务标模板', version: 'V2.0', industry: '综合', scene: '通用商务',
    chapters: 10, usage: 168, updatedAt: '2026-09-10 09:15', creator: '张三', status: 'published',
    desc: '资质证明、业绩、报价、商务响应偏离表等商务章节。', coverTone: 3
  },
  {
    id: 'TP-004', name: '技术响应方案模板', category: '技术标模板', version: 'V1.5', industry: '软件与信息化', scene: '软件项目',
    chapters: 14, usage: 196, updatedAt: '2026-09-08 11:20', creator: '赵六', status: 'published',
    desc: '点对点应答表 + 技术方案说明，适合软件类项目。', coverTone: 4
  },
  {
    id: 'TP-006', name: '运维服务投标模板', category: '技术标模板', version: 'V2.1', industry: 'IT 运维服务', scene: '运维',
    chapters: 11, usage: 142, updatedAt: '2026-09-06 15:33', creator: '李四', status: 'reviewing',
    desc: '含 SLA 承诺、驻场方案、应急响应等运维章节。', coverTone: 5
  },
  {
    id: 'TP-007', name: '项目管理方案模板', category: '自定义模板', version: 'V1.9', industry: '工程管理', scene: '项目管理',
    chapters: 13, usage: 96, updatedAt: '2026-04-28 19:11', creator: '王五', status: 'published',
    desc: '项目组织架构、进度计划、质量安全管理方案。', coverTone: 6
  },
  {
    id: 'TP-008', name: '投标文件附件模板', category: '附件模板', version: 'V1.0', industry: '通用', scene: '附件编制',
    chapters: 8, usage: 78, updatedAt: '2026-04-25 10:45', creator: '赵六', status: 'draft',
    desc: '法人授权书、承诺函、声明等常用附件格式。', coverTone: 7
  }
];

/** 模板版本记录 */
export const demoTemplateVersions: Bid.TemplateVersionVM[] = [
  { id: 'TV-001', templateId: 'TP-005', version: 'V3.1', updatedAt: '2026-09-14 16:20', creator: '王五', note: '新增数据安全与信创章节' },
  { id: 'TV-002', templateId: 'TP-005', version: 'V2.4', updatedAt: '2026-08-11 11:32', creator: '张三', note: '优化评分点对照表结构' },
  { id: 'TV-003', templateId: 'TP-005', version: 'V2.0', updatedAt: '2026-06-08 09:12', creator: '张三', note: '初始发布版本' }
];
