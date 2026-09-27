import type { Bid } from '@/typings/bid';

/** 演示项目（稳定演示数据，非真实统计） */
export const demoProjects: Bid.ProjectVM[] = [
  {
    id: 'P-2026-001',
    name: 'XX市智慧城市建设项目',
    owner: '张三',
    stage: '标书生成',
    deadline: '2026-10-25',
    progress: 70,
    riskLevel: 'medium',
    riskDesc: '评分点覆盖不足',
    updatedAt: '2026-09-26 16:32',
    desc: '城市治理数字化平台建设，含数据中台、智能分析与运营指挥中心。'
  },
  {
    id: 'P-2026-002',
    name: 'XX区教育信息化项目',
    owner: '李四',
    stage: '标书审查',
    deadline: '2026-10-22',
    progress: 45,
    riskLevel: 'medium',
    riskDesc: '技术评分点覆盖率不足70%',
    updatedAt: '2026-09-25 11:08'
  },
  {
    id: 'P-2026-003',
    name: 'XX医院医疗设备采购项目',
    owner: '王五',
    stage: '资料整理',
    deadline: '2026-10-28',
    progress: 30,
    riskLevel: 'high',
    riskDesc: '缺少企业资质文件',
    updatedAt: '2026-09-24 09:40'
  },
  {
    id: 'P-2026-004',
    name: 'XX集团办公系统升级项目',
    owner: '赵六',
    stage: '标书生成',
    deadline: '2026-11-02',
    progress: 60,
    riskLevel: 'low',
    riskDesc: '—',
    updatedAt: '2026-09-23 18:12'
  },
  {
    id: 'P-2026-005',
    name: 'XX市道路改造工程项目',
    owner: '陈七',
    stage: '标书审查',
    deadline: '2026-10-30',
    progress: 20,
    riskLevel: 'medium',
    riskDesc: '缺少业绩证明附件',
    updatedAt: '2026-09-22 14:55'
  }
];

/** 顶部统计卡（演示数据） */
export const demoWorkbenchStats: Bid.WorkbenchStatVM = {
  inProgressProjects: 5,
  weekNewTasks: 24,
  pendingRisks: 4,
  monthBids: 12,
  winRate: 33.3,
  deltas: { projects: '较上周 +1', tasks: '较上周 +6', risks: '较上周 -2', bids: '较上月 +4', winRate: '较上月 +5.6%' }
};

export const demoTodos: Bid.TodoVM[] = [
  { id: 'T-001', title: '完成XX市智慧城市项目标书初稿', dueLabel: '今天 18:00', done: false },
  { id: 'T-002', title: '审核技术方案章节内容', dueLabel: '今天 16:00', done: true },
  { id: 'T-003', title: '补充企业资质文件', dueLabel: '今天 14:00', done: false },
  { id: 'T-004', title: '确认报价清单及费用明细', dueLabel: '今天 12:00', done: false },
  { id: 'T-005', title: '提交标书最终版本', dueLabel: '今天 18:00', done: false }
];

export const demoNotices: Bid.NoticeVM[] = [
  { id: 'N-001', title: 'XX市智慧城市项目招标文件有更新', timeLabel: '10 分钟前', read: false, type: 'parse' },
  { id: 'N-002', title: '系统已完成标书生成任务 GD-2026-018', timeLabel: '2 小时前', read: false, type: 'generate' },
  { id: 'N-003', title: '您有一条审查报告待查看', timeLabel: '5 小时前', read: true, type: 'review' }
];

export const demoRiskAlerts: Bid.RiskAlertVM[] = [
  {
    id: 'R-001',
    title: '资质缺失',
    desc: 'XX医院医疗设备采购项目缺少企业资质文件',
    projectId: 'P-2026-003',
    dateLabel: '2026-09-20',
    level: 'high'
  },
  {
    id: 'R-002',
    title: '评分点覆盖不足',
    desc: 'XX区教育信息化项目技术评分点覆盖率不足70%',
    projectId: 'P-2026-002',
    dateLabel: '2026-09-19',
    level: 'medium'
  },
  {
    id: 'R-003',
    title: '报价异常',
    desc: 'XX集团办公系统升级项目报价低于市场均值30%',
    projectId: 'P-2026-004',
    dateLabel: '2026-09-18',
    level: 'medium'
  },
  {
    id: 'R-004',
    title: '关键附件未上传',
    desc: 'XX市道路改造工程项目缺少业绩证明附件',
    projectId: 'P-2026-005',
    dateLabel: '2026-09-18',
    level: 'low'
  }
];

/** 近 7 天任务趋势（演示数据） */
export const demoTrend: Bid.TrendPointVM[] = [
  { date: '09-21', parse: 18, generate: 12, review: 8 },
  { date: '09-22', parse: 22, generate: 16, review: 10 },
  { date: '09-23', parse: 15, generate: 18, review: 12 },
  { date: '09-24', parse: 26, generate: 14, review: 9 },
  { date: '09-25', parse: 20, generate: 22, review: 14 },
  { date: '09-26', parse: 24, generate: 18, review: 11 },
  { date: '09-27', parse: 28, generate: 20, review: 12 }
];
