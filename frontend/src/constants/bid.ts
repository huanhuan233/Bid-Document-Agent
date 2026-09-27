import type { Bid } from '@/typings/bid';

/** 风险等级 → Element Plus Tag 类型与文案 */
export const riskTagMap: Record<Bid.ReviewIssueVM['risk'], { type: 'danger' | 'warning' | 'success'; label: string }> = {
  high: { type: 'danger', label: '高风险' },
  medium: { type: 'warning', label: '中风险' },
  low: { type: 'success', label: '低风险' }
};

/** 项目风险状态映射 */
export const projectRiskMap: Record<Bid.ProjectVM['riskLevel'], { type: 'danger' | 'warning' | 'success' | 'info'; label: string }> = {
  high: { type: 'danger', label: '高' },
  medium: { type: 'warning', label: '中' },
  low: { type: 'success', label: '低' },
  none: { type: 'info', label: '无' }
};

/** 审查结论映射（结论与风险是独立字段） */
export const conclusionTagMap: Record<string, { type: 'success' | 'warning' | 'danger' | 'info' | 'primary'; label: string }> = {
  满足: { type: 'success', label: '满足' },
  部分满足: { type: 'warning', label: '部分满足' },
  不满足: { type: 'danger', label: '不满足' },
  待确认: { type: 'primary', label: '待确认' },
  证据不足: { type: 'info', label: '证据不足' }
};

/** 整改状态映射（“已修订”≠“已复核通过”） */
export const opinionStatusTagMap: Record<string, { type: 'info' | 'warning' | 'success' | 'primary'; label: string }> = {
  待修订: { type: 'warning', label: '待修订' },
  部分修订: { type: 'primary', label: '部分修订' },
  已修订: { type: 'info', label: '已修订（待复核）' },
  已复核: { type: 'success', label: '已复核通过' }
};

/** 章节状态映射 */
export const chapterStatusMap: Record<Bid.ChapterStatus, { label: string; color: string }> = {
  generated: { label: '已生成', color: '#67c23a' },
  generating: { label: '生成中', color: '#409eff' },
  revise: { label: '待整改', color: '#e6a23c' },
  pending: { label: '待生成', color: '#909399' },
  unconfigured: { label: '未配置', color: '#c0c4cc' }
};

/** 解析状态映射 */
export const parseStatusMap: Record<Bid.ParseFileStatus, { type: 'success' | 'warning' | 'info' | 'danger'; label: string }> = {
  done: { type: 'success', label: '解析完成' },
  processing: { type: 'warning', label: '解析中' },
  pending: { type: 'info', label: '待解析' },
  failed: { type: 'danger', label: '解析失败' }
};

/** 版式标注配色（与原文预览联动） */
export const annotationStyleMap: Record<Bid.DocAnnotationVM['type'], { border: string; bg: string; labelBg: string }> = {
  h1: { border: '#409eff', bg: 'rgba(64,158,255,0.08)', labelBg: '#409eff' },
  h2: { border: '#67c23a', bg: 'rgba(103,194,58,0.08)', labelBg: '#67c23a' },
  paragraph: { border: '#e6a23c', bg: 'rgba(230,162,60,0.08)', labelBg: '#e6a23c' },
  list: { border: '#9a6fe0', bg: 'rgba(154,111,224,0.08)', labelBg: '#9a6fe0' },
  table: { border: '#f56c6c', bg: 'rgba(245,108,108,0.08)', labelBg: '#f56c6c' }
};

/** 条款分类 → 中文名 */
export const clauseCategoryLabelMap: Record<Bid.ClauseCategory, string> = {
  qualification: '资格条件',
  technical: '技术参数',
  commercial: '商务条款',
  scoring: '评分办法',
  disqualify: '废标风险条款',
  delivery: '交付要求'
};

/** sourceKind 展示 */
export const sourceKindLabelMap: Record<Bid.ClauseVM['sourceKind'], string> = {
  observed: '原文观察到',
  requirement: '招标要求规定',
  demo: '演示样例'
};

/** 版式标注类型名 */
export const annotationTypeLabelMap: Record<Bid.DocAnnotationVM['type'], string> = {
  h1: '标题（H1）',
  h2: '标题（H2）',
  paragraph: '段落',
  list: '列表',
  table: '表格'
};
