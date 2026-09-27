import type { Bid } from '@/typings/bid';

/** 投标模板（生成中心左栏使用） */
export const demoGenTemplates = [
  { id: 'TP-001', name: '通用工程类投标模板（2026版）' },
  { id: 'TP-005', name: '智慧城市技术方案模板' },
  { id: 'TP-003', name: '企业综合商务模板' }
];

/** 章节大纲（9 章，与生成进度摘要一致：已生成 6、待整改 2、未配置 2） */
export const demoChapters: Bid.ChapterVM[] = [
  { id: 'CH-01', name: '封面', selected: true, status: 'generated' },
  { id: 'CH-02', name: '投标函', selected: true, status: 'generated' },
  { id: 'CH-03', name: '资信证明', selected: true, status: 'revise' },
  { id: 'CH-04', name: '商务响应', selected: true, status: 'revise' },
  { id: 'CH-05', name: '技术方案', selected: true, status: 'generating' },
  { id: 'CH-06', name: '实施计划', selected: true, status: 'pending' },
  { id: 'CH-07', name: '售后服务', selected: true, status: 'revise' },
  { id: 'CH-08', name: '报价文件', selected: false, status: 'unconfigured' },
  { id: 'CH-09', name: '附件', selected: false, status: 'unconfigured' }
];

/** 章节正文（演示工作副本，仅技术方案配置了完整内容） */
export const demoChapterContents: Record<string, string> = {
  'CH-05':
    '<h1>第五章 技术方案</h1>' +
    '<h2>5.1 项目总体思路</h2>' +
    '<p>本项目以“数据驱动、智慧治理、便民高效”为建设理念，围绕城市治理数字化转型总体要求，构建新一代智慧城市运营管理平台，提升城市管理精细化、智能化水平。</p>' +
    '<h2>5.2 建设目标</h2>' +
    '<ul><li>构建统一的城市数据中台，实现多源数据融合与共享；</li><li>建设智能化应用系统，提升城市治理能力；</li><li>建立安全可控的运行保障体系，确保系统长期稳定运行；</li><li>实现便民服务能力提升，增强市民获得感和满意度。</li></ul>' +
    '<h2>5.3 技术路线</h2>' +
    '<p>采用“1+2+N”的总体技术架构，基于云计算、大数据、人工智能等先进技术，构建可扩展、可持续的智慧城市解决方案。</p>' +
    '<table><thead><tr><th>阶段</th><th>时间安排</th><th>主要任务</th><th>预期成果</th></tr></thead><tbody>' +
    '<tr><td>第一阶段</td><td>1-3个月</td><td>基础研究与方案设计</td><td>完成调研设计方案</td></tr>' +
    '<tr><td>第二阶段</td><td>4-8个月</td><td>系统开发与测试</td><td>完成系统核心运行</td></tr>' +
    '<tr><td>第三阶段</td><td>9-12个月</td><td>上线运营与评估</td><td>正式投入使用</td></tr>' +
    '</tbody></table>',
  'CH-02': '<h1>投标函</h1><p>致：XX市大数据管理局</p><p>根据贵方招标文件（编号：ZB-2026-018）要求，我方愿以人民币（大写）玖仟万元整的投标总价，按合同约定实施和完成本招标项目。</p>',
  'CH-03': '<h1>资信证明</h1><p>1. 营业执照副本复印件（加盖公章）；</p><p>2. ISO9001 质量管理体系认证证书；</p><p>3. 安全生产许可证。</p><p class="revise-tip">【待整改】按审查意见需补充项目经理一级建造师证书说明。</p>'
};

/** 按整改意见修订：预置整改意见（来自审查报告 RR-2026-005） */
export const demoRevisionOpinions: Bid.RevisionOpinionVM[] = [
  {
    id: 'OP-001',
    reportId: 'RR-2026-005',
    priority: 'high',
    status: '待修订',
    title: '补充项目经理一级建造师资格证书说明',
    desc: '需在技术方案中补充项目经理的一级建造师资格证书扫描件及其在本项目中的职责与工作安排。',
    targetChapterId: 'CH-03'
  },
  {
    id: 'OP-002',
    reportId: 'RR-2026-005',
    priority: 'high',
    status: '待修订',
    title: '补充近三年同类项目业绩与验收证明',
    desc: '需提供近三年智慧城市类同类项目的业绩清单，并附验收证明材料，增强方案可信度。',
    targetChapterId: 'CH-03'
  },
  {
    id: 'OP-003',
    reportId: 'RR-2026-005',
    priority: 'medium',
    status: '待修订',
    title: '完善数据安全与售后服务承诺',
    desc: '需在技术方案中完善数据安全保障措施，并补充售后服务承诺的服务响应机制。',
    targetChapterId: 'CH-07'
  },
  {
    id: 'OP-004',
    reportId: 'RR-2026-005',
    priority: 'medium',
    status: '部分修订',
    title: '修订评分点点覆盖章节',
    desc: '审查发现评分点 3.2、4.1 在当前方案中未明确体现，需补充相关内容并与评分标准逐项匹配。',
    targetChapterId: 'CH-05'
  }
];

/** 整改演示：预置修订前后版本（明确标注，不调用模型） */
export const demoRevisionDiff: Record<string, { before: string; after: string; note: string }> = {
  'OP-004': {
    before:
      '5.2 建设目标\n构建统一的城市数据中台，实现多源数据融合与共享；建设智能化应用系统，提升城市治理能力。',
    after:
      '5.2 建设目标\n构建统一的城市数据中台，实现不少于10类数据源的多源数据融合与共享（对应评分点3.2）；建设智能化应用系统，在线用户支持1000并发以上，页面响应不超过2秒（对应评分点4.1），提升城市治理能力。',
    note: '演示修订版本：由预置整改意见 OP-004 生成，仅用于演示流程，未调用真实模型。'
  }
};

/** 投标文件版本（生成/审查中心选择用） */
export const demoBidDocVersions = [
  { id: 'DV-2026-005', projectId: 'P-2026-001', name: '技术标响应文件.docx', version: 'V2', uploadedAt: '2026-09-19 09:18', sizeLabel: '36.8 MB' },
  { id: 'DV-2026-004', projectId: 'P-2026-001', name: '技术标响应文件.docx', version: 'V1', uploadedAt: '2026-09-05 14:20', sizeLabel: '31.2 MB' }
];

/** 生成记录 */
export const demoGenerationRecords: Bid.GenerationRecordVM[] = [
  {
    id: 'GD-2026-018', projectId: 'P-2026-001', projectName: 'XX市智慧城市建设项目', mode: 'revision', scope: '技术标',
    status: '待完善', operator: '张三', startedAt: '2026-09-24 09:30', finishedAt: '—', chaptersDone: 6, chaptersTotal: 9
  },
  {
    id: 'GD-2026-012', projectId: 'P-2026-001', projectName: 'XX市智慧城市建设项目', mode: 'normal', scope: '技术标',
    status: '已完成', operator: '张三', startedAt: '2026-09-12 10:00', finishedAt: '2026-09-12 10:26', chaptersDone: 9, chaptersTotal: 9
  },
  {
    id: 'GD-2026-009', projectId: 'P-2026-002', projectName: 'XX区教育信息化项目', mode: 'normal', scope: '整本草稿',
    status: '已完成', operator: '李四', startedAt: '2026-09-08 15:10', finishedAt: '2026-09-08 15:44', chaptersDone: 8, chaptersTotal: 8
  },
  {
    id: 'GD-2026-006', projectId: 'P-2026-004', projectName: 'XX集团办公系统升级项目', mode: 'normal', scope: '商务标',
    status: '生成中', operator: '赵六', startedAt: '2026-09-26 11:02', finishedAt: '—', chaptersDone: 3, chaptersTotal: 7
  },
  {
    id: 'GD-2026-003', projectId: 'P-2026-005', projectName: 'XX市道路改造工程项目', mode: 'revision', scope: '技术标',
    status: '失败', operator: '陈七', startedAt: '2026-09-03 09:20', finishedAt: '2026-09-03 09:21', chaptersDone: 0, chaptersTotal: 9
  }
];
