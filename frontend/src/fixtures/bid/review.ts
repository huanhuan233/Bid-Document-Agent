import type { Bid } from '@/typings/bid';

/**
 * 审查问题（演示：绑定报告 RR-2026-005 / 要求版本 RV-2026-001 / 投标文件 DV-2026-005）。
 * 注意：conclusion（结论）与 risk（风险等级）是两个独立字段；“满足”不是低风险问题。
 */
export const demoReviewIssues: Bid.ReviewIssueVM[] = [
  {
    id: 'IS-001', reportId: 'RR-2026-005', category: '资质资格缺失',
    requirement: '具有独立法人资格，提供有效营业执照', response: '已提供营业执照，符合要求', pages: '12',
    conclusion: '满足', risk: 'low', opinion: '—', relatedChapter: '资信证明',
    evidence: { id: 'EV-001', fileLabel: '技术标响应文件.docx', version: 'V2', page: 12, quote: '“营业执照（统一社会信用代码：91XX00…），经营范围含信息系统集成服务。”' },
    opinionStatus: '已复核'
  },
  {
    id: 'IS-002', reportId: 'RR-2026-005', category: '资质资格缺失',
    requirement: '近三年完成至少两项类似业绩，提供合同及验收证明', response: '提供了2个类似项目合同，部分合同缺少验收证明', pages: '48-56',
    conclusion: '部分满足', risk: 'medium', opinion: '补充近三年智慧城市类项目验收证明材料', relatedChapter: '资信证明',
    evidence: { id: 'EV-002', fileLabel: '技术标响应文件.docx', version: 'V2', page: 48, quote: '“2024-2026年部分同类合同（验收证明见附件）”，其中1项未附验收报告。' },
    opinionStatus: '待修订'
  },
  {
    id: 'IS-003', reportId: 'RR-2026-005', category: '资质资格缺失',
    requirement: '拟派项目经理具有一级建造师资格证书', response: '未提供项目经理一级建造师资格证书', pages: '—',
    conclusion: '不满足', risk: 'high', opinion: '补充项目经理一级建造师资格证书扫描件及职责安排', relatedChapter: '资信证明',
    evidence: null, opinionStatus: '待修订'
  },
  {
    id: 'IS-004', reportId: 'RR-2026-005', category: '技术方案不完整',
    requirement: '须提供完整的技术架构设计材料', response: '提供了总体技术架构设计，符合要求', pages: '60-62',
    conclusion: '满足', risk: 'low', opinion: '—', relatedChapter: '技术方案',
    evidence: { id: 'EV-003', fileLabel: '技术标响应文件.docx', version: 'V2', page: 60, quote: '“总体技术架构采用 1+2+N 分层设计（见图 3-1）。”' },
    opinionStatus: '已复核'
  },
  {
    id: 'IS-005', reportId: 'RR-2026-005', category: '关键技术指标偏离',
    requirement: '产品需符合国家及行业相关技术标准', response: '基本达到产品国标要求，符合要求', pages: '78-80',
    conclusion: '满足', risk: 'low', opinion: '—', relatedChapter: '技术方案',
    evidence: { id: 'EV-004', fileLabel: '技术标响应文件.docx', version: 'V2', page: 78, quote: '“系统性能指标对照国标 GB/T 22239-2019 逐项响应，详见指标对照表 4-2。”' },
    opinionStatus: '已复核'
  },
  {
    id: 'IS-006', reportId: 'RR-2026-005', category: '商务条款不一致',
    requirement: '投标报价不得高于最高限价 8620 万元', response: '投标总价为 8620 万元，低于限价', pages: '102',
    conclusion: '满足', risk: 'low', opinion: '—', relatedChapter: '报价文件',
    evidence: { id: 'EV-005', fileLabel: '技术标响应文件.docx', version: 'V2', page: 102, quote: '“投标总报价：8620 万元（含实施、运维及税费）。”' },
    opinionStatus: '已复核'
  },
    {
    id: 'IS-007', reportId: 'RR-2026-005', category: '商务条款不一致',
    requirement: '质保期不少于3年', response: '承诺质保期2年，低于招标要求', pages: '120-125',
    conclusion: '不满足', risk: 'high', opinion: '将质保期承诺修改为3年，并同步调整售后方案', relatedChapter: '售后服务',
    evidence: { id: 'EV-006', fileLabel: '技术标响应文件.docx', version: 'V2', page: 120, quote: '“我方承诺提供贰年免费质保服务。”（与招标要求不一致）' },
    opinionStatus: '待修订'
  },
  {
    id: 'IS-008', reportId: 'RR-2026-005', category: '证明材料缺失',
    requirement: '机柜设备需提供原厂授权证明', response: '未提供原厂授权证明', pages: '—',
    conclusion: '证据不足', risk: 'high', opinion: '联系设备厂商补充原厂授权及服务承诺函', relatedChapter: '附件',
    evidence: null, opinionStatus: '待修订'
  },
  {
    id: 'IS-009', reportId: 'RR-2026-005', category: '响应内容不准确',
    requirement: '数据汇聚接入不少于10类数据源', response: '方案中仅明确8类数据源接入', pages: '66',
    conclusion: '部分满足', risk: 'medium', opinion: '补充视频、物联感知等其余数据源接入方案', relatedChapter: '技术方案',
    evidence: { id: 'EV-007', fileLabel: '技术标响应文件.docx', version: 'V2', page: 66, quote: '“平台一期接入人口、法人、地理空间等8类数据。”' },
    opinionStatus: '部分修订'
  },
  {
    id: 'IS-010', reportId: 'RR-2026-005', category: '响应内容不准确',
    requirement: '系统在线用户不少于1000并发', response: '承诺支持1000并发，等于最低要求', pages: '72',
    conclusion: '待确认', risk: 'medium', opinion: '建议按1200并发以上响应以获得评分优势', relatedChapter: '技术方案',
    evidence: { id: 'EV-008', fileLabel: '技术标响应文件.docx', version: 'V2', page: 72, quote: '“系统支持不少于1000用户并发访问。”' },
    opinionStatus: '待修订'
  },
  {
    id: 'IS-011', reportId: 'RR-2026-005', category: '证明材料缺失',
    requirement: '提供信息安全等级保护三级备案证明', response: '仅提供等保二级证明', pages: '—',
    conclusion: '不满足', risk: 'high', opinion: '补充等保三级备案证明或承诺函', relatedChapter: '附件',
    evidence: null, opinionStatus: '待修订'
  },
  {
    id: 'IS-012', reportId: 'RR-2026-005', category: '格式与格式要求',
    requirement: '投标文件须按招标文件规定格式编制并装订', response: '正文格式基本符合，附件目录页码缺失', pages: '—',
    conclusion: '部分满足', risk: 'low', opinion: '按招标要求补全附件目录及页码', relatedChapter: '附件',
    evidence: { id: 'EV-009', fileLabel: '技术标响应文件.docx', version: 'V2', page: null, quote: '附件目录未标注页码，无法自动定位。' },
    opinionStatus: '待修订'
  }
];

/** 一致性校验 */
export const demoConsistencyItems: Bid.ConsistencyItemVM[] = [
  { field: '项目名称', tenderValue: 'XX市智慧城市建设项目', bidValue: 'XX市智慧城市建设项目', result: '一致', desc: '—' },
  { field: '投标人名称', tenderValue: 'XX科技信息科技有限公司', bidValue: 'XX科技信息科技有限公司', result: '一致', desc: '—' },
  { field: '投标保证金', tenderValue: '80万元', bidValue: '80万元', result: '一致', desc: '—' },
  { field: '投标总报价', tenderValue: '最高限价 8620 万元', bidValue: '8620 万元', result: '一致', desc: '等于最高限价，未超出' },
  { field: '质保期', tenderValue: '3年', bidValue: '2年', result: '不一致', desc: '投标文件承诺2年，低于招标文件要求' },
  { field: '项目工期', tenderValue: '签订后8个月内', bidValue: '8个月', result: '一致', desc: '—' }
];

/** 审查报告 RR-2026-005（分类合计 = 46，与总数一致） */
export const demoReviewReport: Bid.ReviewReportVM = {
  id: 'RR-2026-005',
  projectId: 'P-2026-001',
  projectName: 'XX市智慧城市建设项目',
  requirementVersionLabel: 'XX市智慧城市建设项目-招标文件 V1.2（RV-2026-001）',
  docVersionLabel: '技术标响应文件.docx V2（DV-2026-005）',
  reviewedAt: '2026-09-20 14:32:16',
  reviewer: '张三',
  scope: ['资格审查', '技术响应', '商务响应', '全量审查'],
  total: 46,
  high: 8,
  medium: 12,
  low: 26,
  coverageRate: 92.5,
  consistencyRate: 78.6,
  digest:
    '本次审查共发现 46 个问题，其中高风险 8 个、中风险 12 个、低风险 26 个。评分点覆盖率 92.5%（演示值），较上次提升 2.3%，仍有 3 个评分点需完善。一致性通过率 78.6%，主要问题集中在质保期、技术参数与商务条款不一致。建议优先处理高风险问题，特别是资格证明和关键技术指标。',
  categories: [
    { name: '资质资格缺失', count: 8, percent: 17.4 },
    { name: '技术方案不完整', count: 7, percent: 15.2 },
    { name: '关键技术指标偏离', count: 6, percent: 13.0 },
    { name: '商务条款不一致', count: 5, percent: 10.9 },
    { name: '证明材料缺失', count: 4, percent: 8.7 },
    { name: '响应内容不准确', count: 4, percent: 8.7 },
    { name: '格式与格式要求', count: 3, percent: 6.5 },
    { name: '售后服务承诺', count: 3, percent: 6.5 },
    { name: '其他问题', count: 6, percent: 13.1 }
  ],
  advices: [
    { key: '资质资格类', title: '资质资格类（8项）', desc: '完善企业资质、人员证书、业绩证明等材料', priority: 'high' },
    { key: '技术方案类', title: '技术方案类（13项）', desc: '补全技术响应偏离项，完善关键技术指标', priority: 'high' },
    { key: '商务条款类', title: '商务条款类（9项）', desc: '核对质保期、付款条件、交货期说明', priority: 'medium' },
    { key: '格式与文件类', title: '格式与文件类（6项）', desc: '按招标文件规定调整格式与装订', priority: 'low' },
    { key: '其他问题', title: '其他问题（10项）', desc: '处理遗留问题、完善服务承诺相关内容', priority: 'low' }
  ]
};

/** 审查记录 */
export const demoReviewRecords: Bid.ReviewRecordVM[] = [
  {
    id: 'RR-2026-005', projectId: 'P-2026-001', projectName: 'XX市智慧城市建设项目',
    basisLabel: '招标要求 V1.2（RV-2026-001）', docVersionLabel: '技术标响应文件 V2', status: '已完成',
    high: 8, medium: 12, low: 26, operator: '张三', startedAt: '2026-09-20 14:30', finishedAt: '2026-09-20 14:32', reportId: 'RR-2026-005'
  },
  {
    id: 'RR-2026-004', projectId: 'P-2026-001', projectName: 'XX市智慧城市建设项目',
    basisLabel: '招标要求 V1.1（RV-2026-001-1）', docVersionLabel: '技术标响应文件 V1', status: '已完成',
    high: 10, medium: 15, low: 28, operator: '张三', startedAt: '2026-09-05 10:00', finishedAt: '2026-09-05 10:03', reportId: 'RR-2026-004'
  },
  {
    id: 'RR-2026-003', projectId: 'P-2026-002', projectName: 'XX区教育信息化项目',
    basisLabel: '招标要求 V1.0（RV-2026-002）', docVersionLabel: '投标文件 V1', status: '审查中',
    high: 0, medium: 0, low: 0, operator: '李四', startedAt: '2026-09-26 16:10', finishedAt: '—'
  },
  {
    id: 'RR-2026-002', projectId: 'P-2026-005', projectName: 'XX市道路改造工程项目',
    basisLabel: '招标要求 V1.0', docVersionLabel: '投标文件 V1', status: '排队中',
    high: 0, medium: 0, low: 0, operator: '陈七', startedAt: '—', finishedAt: '—'
  }
];

/** 报告列表页的其他历史报告（演示） */
export const demoReportList = [
  demoReviewRecords[0],
  demoReviewRecords[1],
  demoReviewRecords[2]
];

