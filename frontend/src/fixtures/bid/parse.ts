import type { Bid } from '@/typings/bid';

/** 解析中心演示文件（与项目 P-2026-001 绑定） */
export const demoParseFiles: Bid.ParseFileVM[] = [
  {
    id: 'PF-001',
    projectId: 'P-2026-001',
    name: 'XX市智慧城市建设项目_招标文件.pdf',
    type: '招标文件',
    sizeLabel: '12.6 MB',
    pages: 152,
    status: 'done',
    uploadedAt: '2026-09-20 14:32',
    durationLabel: '2分18秒'
  },
  {
    id: 'PF-002',
    projectId: 'P-2026-001',
    name: '技术规范书.docx',
    type: '技术规范',
    sizeLabel: '3.8 MB',
    pages: 86,
    status: 'processing',
    uploadedAt: '2026-09-19 10:21'
  },
  {
    id: 'PF-003',
    projectId: 'P-2026-001',
    name: '评分办法.pdf',
    type: '评分办法',
    sizeLabel: '1.2 MB',
    pages: 32,
    status: 'pending',
    uploadedAt: '2026-09-18 16:03'
  }
];

/** 已解析招标要求版本（审查依据下拉只展示这些“已解析”的版本） */
export const demoRequirementVersions: Bid.RequirementVersionVM[] = [
  {
    id: 'RV-2026-001',
    projectId: 'P-2026-001',
    fileId: 'PF-001',
    name: 'XX市智慧城市建设项目-招标文件',
    version: 'V1.2',
    confirmed: true,
    confirmedAt: '2026-09-21 09:30'
  },
  {
    id: 'RV-2026-001-1',
    projectId: 'P-2026-001',
    fileId: 'PF-001',
    name: 'XX市智慧城市建设项目-招标文件',
    version: 'V1.1',
    confirmed: true,
    confirmedAt: '2026-09-15 15:00'
  },
  {
    id: 'RV-2026-002',
    projectId: 'P-2026-002',
    fileId: 'PF-002',
    name: 'XX区教育信息化项目-招标文件',
    version: 'V1.0',
    confirmed: true,
    confirmedAt: '2026-09-18 10:00'
  }
];

/** 条款分类 Tab 定义 */
export const clauseCategoryOptions: { key: Bid.ClauseCategory; label: string }[] = [
  { key: 'qualification', label: '资格条件' },
  { key: 'technical', label: '技术参数' },
  { key: 'commercial', label: '商务条款' },
  { key: 'scoring', label: '评分办法' },
  { key: 'disqualify', label: '废标风险条款' },
  { key: 'delivery', label: '交付要求' }
];

/** 结构化条款（演示：绑定 PF-001） */
export const demoClauses: Bid.ClauseVM[] = [
  {
    id: 'C-001', fileId: 'PF-001', category: 'qualification', code: '2.1', name: '投标人资格要求',
    content: '投标人应具备独立法人资格，具有有效的营业执照。', page: 12, importance: '高', confidence: 0.98,
    sourceKind: 'observed', confirmed: true
  },
  {
    id: 'C-002', fileId: 'PF-001', category: 'qualification', code: '2.2', name: '财务要求',
    content: '投标人应具备计算机信息系统集成二级及以上资质，财务状况良好。', page: 12, importance: '中', confidence: 0.9,
    sourceKind: 'observed', confirmed: false
  },
  {
    id: 'C-003', fileId: 'PF-001', category: 'qualification', code: '2.3', name: '业绩要求',
    content: '近三年内（2024年1月1日至2026年12月31日）具有类似项目业绩，并提供合同复印件及验收证明。',
    page: 12, importance: '高', confidence: 0.96, sourceKind: 'observed', confirmed: true
  },
  {
    id: 'C-004', fileId: 'PF-001', category: 'qualification', code: '2.4', name: '联合体投标',
    content: '本项目不接受联合体投标。', page: 12, importance: '高', confidence: 0.95, sourceKind: 'observed', confirmed: true
  },
  {
    id: 'C-101', fileId: 'PF-001', category: 'technical', code: '3.1', name: '系统功能要求',
    content: '系统应支持城市综合治理、数据共享、智能分析等功能，数据汇聚接入不少于10类数据源。',
    page: 28, importance: '高', confidence: 0.92, sourceKind: 'observed', confirmed: true
  },
  {
    id: 'C-102', fileId: 'PF-001', category: 'technical', code: '3.2', name: '性能指标要求',
    content: '系统在线用户不少于1000并发，页面响应时间不超过2秒，可用性不低于99.9%。',
    page: 30, importance: '高', confidence: 0.94, sourceKind: 'observed', confirmed: true
  },
  {
    id: 'C-103', fileId: 'PF-001', category: 'technical', code: '3.5', name: '数据安全要求',
    content: '须通过信息安全等级保护三级测评，提供数据备份与容灾方案。', page: 33, importance: '中', confidence: 0.88,
    sourceKind: 'observed', confirmed: false
  },
  {
    id: 'C-201', fileId: 'PF-001', category: 'commercial', code: '5.1', name: '报价要求',
    content: '投标报价不得高于最高限价8620万元，报价应包含实施、运维及税费等全部费用。', page: 102, importance: '高',
    confidence: 0.97, sourceKind: 'observed', confirmed: true
  },
  {
    id: 'C-202', fileId: 'PF-001', category: 'commercial', code: '5.3', name: '付款方式',
    content: '合同签订后支付30%，初验通过支付40%，终验通过支付25%，质保期满支付5%。', page: 104, importance: '中',
    confidence: 0.91, sourceKind: 'observed', confirmed: false
  },
  {
    id: 'C-301', fileId: 'PF-001', category: 'scoring', code: '6.2', name: '技术评分办法',
    content: '技术部分评分占总分60分，商务部分评分占总分40分；技术方案30分、实施方案15分、售后服务15分。',
    page: 118, importance: '高', confidence: 0.95, sourceKind: 'observed', confirmed: true
  },
  {
    id: 'C-302', fileId: 'PF-001', category: 'scoring', code: '6.4', name: '项目经理要求',
    content: '拟派项目经理须具有一级建造师（信息技术相关专业）资格证书，并提供社保证明。', page: 119, importance: '高',
    confidence: 0.93, sourceKind: 'observed', confirmed: false
  },
  {
    id: 'C-401', fileId: 'PF-001', category: 'disqualify', code: '8.1', name: '废标情形',
    content: '未按要求提供投标保证金、报价超过最高限价、资格证明文件不全的，按废标处理。', page: 128, importance: '高',
    confidence: 0.96, sourceKind: 'observed', confirmed: true
  },
  {
    id: 'C-501', fileId: 'PF-001', category: 'delivery', code: '7.1', name: '交付时间与地点',
    content: '合同签订后8个月内完成系统建设并通过终验；交付地点：XX市大数据管理局机房。', page: 110, importance: '高',
    confidence: 0.94, sourceKind: 'observed', confirmed: true
  },
  {
    id: 'C-502', fileId: 'PF-001', category: 'delivery', code: '7.4', name: '质保期要求',
    content: '系统提供不少于3年免费质保，质保期内提供7×24小时响应服务。', page: 112, importance: '中', confidence: 0.9,
    sourceKind: 'observed', confirmed: false
  }
];

/** 第 12 页版式标注（与演示文档页对应，坐标为页面百分比） */
export const demoPage12Annotations: Bid.DocAnnotationVM[] = [
  { id: 'A-H1-1', type: 'h1', label: 'H1 章标题', x: 8, y: 6, w: 84, h: 7, page: 12 },
  { id: 'A-H2-1', type: 'h2', label: 'H2 节标题', x: 10, y: 17, w: 52, h: 5, page: 12 },
  { id: 'A-P-1', type: 'paragraph', label: '段落', x: 10, y: 24, w: 80, h: 10, page: 12 },
  { id: 'A-L-1', type: 'list', label: '列表段落', x: 10, y: 44, w: 80, h: 13, page: 12 },
  { id: 'A-T-1', type: 'table', label: '表格区域', x: 10, y: 66, w: 80, h: 24, page: 12 }
];

/**
 * 字体样式（PF-001 第 12 页）。
 * observed = 原文观察到的格式；required = 招标文件要求投标文件使用的格式，两者不可混淆。
 */
export const demoTypographyObserved: Bid.TypographyVM = {
  zhFont: '宋体',
  enFont: 'Times New Roman',
  h1Size: '16 pt（三号）',
  h2Size: '14 pt（四号）',
  bodySize: '12 pt（小四）',
  bold: '标题加粗',
  italic: '无斜体',
  align: '两端对齐',
  lineHeight: '1.5 倍',
  spaceBefore: '0.5 行',
  spaceAfter: '0.5 行',
  firstLineIndent: '2 字符'
};

export const demoTypographyRequired: Bid.TypographyVM = {
  zhFont: '仿宋_GB2312',
  enFont: 'Times New Roman',
  h1Size: '16 pt（三号）',
  h2Size: '15 pt',
  bodySize: '12 pt（小四）',
  bold: '一级标题加粗',
  italic: '不允许斜体',
  align: '两端对齐',
  lineHeight: '1.5 倍',
  spaceBefore: '0.5 行',
  spaceAfter: '0.5 行',
  firstLineIndent: '2 字符'
};

export const demoLayoutObserved: Bid.LayoutVM = {
  paper: 'A4（210 × 297 mm）',
  orientation: '纵向',
  marginTop: '2.5 cm',
  marginBottom: '2.5 cm',
  marginLeft: '3.0 cm',
  marginRight: '3.0 cm',
  columns: '单栏',
  header: '存在（项目名称居中）',
  footer: '存在',
  pageNumPos: '页脚居中',
  counts: { h1: 18, h2: 62, paragraph: 186, table: 24, image: 8, list: 32, other: 18 }
};

export const demoLayoutRequired: Bid.LayoutVM = {
  paper: 'A4（210 × 297 mm）',
  orientation: '纵向',
  marginTop: '2.5 cm',
  marginBottom: '2.5 cm',
  marginLeft: '3.0 cm',
  marginRight: '2.5 cm',
  columns: '单栏',
  header: '须包含项目名称与招标编号',
  footer: '存在',
  pageNumPos: '页脚居中',
  counts: { h1: 0, h2: 0, paragraph: 0, table: 0, image: 0, list: 0, other: 0 }
};

/** 解析记录 */
export const demoParseRecords: Bid.ParseRecordVM[] = [
  {
    id: 'PR-2026-031', projectId: 'P-2026-001', projectName: 'XX市智慧城市建设项目',
    fileName: 'XX市智慧城市建设项目_招标文件.pdf', status: 'done', pages: 152, clauses: 286, operator: '张三',
    startedAt: '2026-09-20 14:32', finishedAt: '2026-09-20 14:35', resultId: 'PF-001'
  },
  {
    id: 'PR-2026-030', projectId: 'P-2026-001', projectName: 'XX市智慧城市建设项目',
    fileName: '技术规范书.docx', status: 'processing', pages: 86, clauses: 0, operator: '张三',
    startedAt: '2026-09-19 10:21', finishedAt: '—'
  },
  {
    id: 'PR-2026-028', projectId: 'P-2026-001', projectName: 'XX市智慧城市建设项目',
    fileName: '评分办法.pdf', status: 'pending', pages: 32, clauses: 0, operator: '李四',
    startedAt: '—', finishedAt: '—'
  },
  {
    id: 'PR-2026-025', projectId: 'P-2026-002', projectName: 'XX区教育信息化项目',
    fileName: 'XX区教育信息化项目_招标文件.pdf', status: 'done', pages: 96, clauses: 174, operator: '李四',
    startedAt: '2026-09-17 09:12', finishedAt: '2026-09-17 09:14', resultId: 'PF-002'
  },
  {
    id: 'PR-2026-021', projectId: 'P-2026-003', projectName: 'XX医院医疗设备采购项目',
    fileName: '采购需求及技术参数.pdf', status: 'failed', pages: 45, clauses: 0, operator: '王五',
    startedAt: '2026-09-15 16:40', finishedAt: '2026-09-15 16:41'
  }
];


