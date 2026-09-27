/**
 * 智标云业务 ViewModel 类型（全局命名空间 Bid）
 *
 * DTO 与 ViewModel 分离：versionId / projectId / fileObjectId 等一律按字符串处理。
 */
export declare namespace Bid {
  type DataMode = 'demo' | 'api';

  /** 项目阶段 */
  type ProjectStage = '资料整理' | '标书解析' | '标书生成' | '标书审查' | '投标递交' | '已结束';

  interface ProjectVM {
    id: string;
    name: string;
    owner: string;
    stage: ProjectStage;
    deadline: string;
    progress: number;
    riskLevel: 'high' | 'medium' | 'low' | 'none';
    riskDesc: string;
    updatedAt: string;
    desc?: string;
  }

  interface WorkbenchStatVM {
    inProgressProjects: number;
    weekNewTasks: number;
    pendingRisks: number;
    monthBids: number;
    /** 中标率是历史结果统计；null 表示未提供 */
    winRate: number | null;
    deltas: { projects?: string; tasks?: string; risks?: string; bids?: string; winRate?: string };
  }

  interface TodoVM {
    id: string;
    title: string;
    dueLabel: string;
    done: boolean;
  }

  interface NoticeVM {
    id: string;
    title: string;
    timeLabel: string;
    read: boolean;
    type: 'parse' | 'generate' | 'review' | 'system';
  }

  interface RiskAlertVM {
    id: string;
    title: string;
    desc: string;
    projectId: string;
    dateLabel: string;
    level: 'high' | 'medium' | 'low';
  }

  interface TrendPointVM {
    date: string;
    parse: number;
    generate: number;
    review: number;
  }

  type ParseFileStatus = 'done' | 'processing' | 'pending' | 'failed';

  interface ParseFileVM {
    id: string;
    projectId: string;
    name: string;
    type: string;
    sizeLabel: string;
    pages: number;
    status: ParseFileStatus;
    uploadedAt: string;
    durationLabel?: string;
  }

  type ClauseCategory = 'qualification' | 'technical' | 'commercial' | 'scoring' | 'disqualify' | 'delivery';

  interface ClauseVM {
    id: string;
    fileId: string;
    category: ClauseCategory;
    code: string;
    name: string;
    content: string;
    page: number;
    importance: '高' | '中' | '低';
    confidence: number;
    /** observed=原文观察到的格式 requirement=招标要求规定的格式 demo=演示样例 */
    sourceKind: 'observed' | 'requirement' | 'demo';
    confirmed: boolean;
  }

  interface TypographyVM {
    zhFont: string;
    enFont: string;
    h1Size: string;
    h2Size: string;
    bodySize: string;
    bold: string;
    italic: string;
    align: string;
    lineHeight: string;
    spaceBefore: string;
    spaceAfter: string;
    firstLineIndent: string;
  }

  interface LayoutVM {
    paper: string;
    orientation: string;
    marginTop: string;
    marginBottom: string;
    marginLeft: string;
    marginRight: string;
    columns: string;
    header: string;
    footer: string;
    pageNumPos: string;
    counts: { h1: number; h2: number; paragraph: number; table: number; image: number; list: number; other: number };
  }

  interface DocAnnotationVM {
    id: string;
    type: 'h1' | 'h2' | 'paragraph' | 'list' | 'table';
    label: string;
    /** 相对文档页的百分比坐标（0-100） */
    x: number;
    y: number;
    w: number;
    h: number;
    page: number;
  }

  interface EvidenceRefVM {
    id: string;
    fileLabel: string;
    version: string;
    page: number | null;
    quote: string;
    location?: string;
    bbox?: number[];
  }

  interface RequirementVersionVM {
    id: string;
    projectId: string;
    fileId: string;
    name: string;
    version: string;
    confirmed: boolean;
    confirmedAt?: string;
  }

  type ChapterStatus = 'generated' | 'generating' | 'revise' | 'pending' | 'unconfigured';

  interface ChapterVM {
    id: string;
    name: string;
    selected: boolean;
    status: ChapterStatus;
  }

  type RevisionOpinionStatus = '待修订' | '部分修订' | '已修订' | '已复核';

  interface RevisionOpinionVM {
    id: string;
    reportId: string;
    priority: 'high' | 'medium' | 'low';
    status: RevisionOpinionStatus;
    title: string;
    desc: string;
    targetChapterId: string;
  }

  interface GenerationDraftVM {
    id: string;
    projectId: string;
    mode: 'normal' | 'revision';
    requirementVersionId: string;
    reportId?: string;
    docVersionId?: string;
    scope: '商务标' | '技术标' | '整本草稿';
    templateId: string;
    chapters: ChapterVM[];
    activeChapterId: string;
    content: string;
    savedLabel: string;
    dirty: boolean;
    materialRefs: { id: string; name: string }[];
    opinionIds: string[];
  }

  interface GenerationRecordVM {
    id: string;
    projectId: string;
    projectName: string;
    mode: 'normal' | 'revision';
    scope: string;
    status: '已完成' | '生成中' | '待完善' | '失败';
    operator: string;
    startedAt: string;
    finishedAt: string;
    chaptersDone: number;
    chaptersTotal: number;
  }

  type ReviewConclusion = '满足' | '部分满足' | '不满足' | '待确认' | '证据不足';

  interface ReviewIssueVM {
    id: string;
    reportId: string;
    category: string;
    requirement: string;
    response: string;
    pages: string;
    conclusion: ReviewConclusion;
    risk: 'high' | 'medium' | 'low';
    opinion: string;
    relatedChapter: string;
    evidence: EvidenceRefVM | null;
    opinionStatus: RevisionOpinionStatus;
  }

  interface ConsistencyItemVM {
    field: string;
    tenderValue: string;
    bidValue: string;
    result: '一致' | '不一致';
    desc: string;
  }

  interface ReviewReportVM {
    id: string;
    projectId: string;
    projectName: string;
    requirementVersionLabel: string;
    docVersionLabel: string;
    reviewedAt: string;
    reviewer: string;
    scope: string[];
    total: number;
    high: number;
    medium: number;
    low: number;
    /** 评分点覆盖率（演示值/真实指标需注明来源） */
    coverageRate: number | null;
    consistencyRate: number | null;
    digest: string;
    categories: { name: string; count: number; percent: number }[];
    advices: { key: string; title: string; desc: string; priority: 'high' | 'medium' | 'low' }[];
  }

  interface ReviewRecordVM {
    id: string;
    projectId: string;
    projectName: string;
    basisLabel: string;
    docVersionLabel: string;
    status: '已完成' | '审查中' | '排队中';
    high: number;
    medium: number;
    low: number;
    operator: string;
    startedAt: string;
    finishedAt: string;
    reportId?: string;
  }

  interface ParseRecordVM {
    id: string;
    projectId: string;
    projectName: string;
    fileName: string;
    status: ParseFileStatus;
    pages: number;
    clauses: number;
    operator: string;
    startedAt: string;
    finishedAt: string;
    resultId?: string;
  }

  interface TemplateVM {
    id: string;
    name: string;
    category: '标准投标模板' | '商务标模板' | '技术标模板' | '附件模板' | '自定义模板';
    version: string;
    industry: string;
    scene: string;
    chapters: number;
    usage: number;
    updatedAt: string;
    creator: string;
    status: 'published' | 'draft' | 'reviewing';
    desc: string;
    recommended?: boolean;
    coverTone: number;
  }

  interface TemplateVersionVM {
    id: string;
    templateId: string;
    version: string;
    updatedAt: string;
    creator: string;
    note: string;
  }

  interface MaterialVM {
    id: string;
    name: string;
    type: string;
    directory: string;
    tags: string[];
    projects: string;
    updatedAt: string;
    expiry: string | null;
    refs: number;
    status: '有效' | '即将过期' | '已过期';
    recommended: boolean;
    sizeLabel: string;
    version: string;
    uploader: string;
    usage: { scene: string; user: string; time: string }[];
    previewKind: 'pdf' | 'image' | 'docx' | 'other';
  }

  interface MaterialDirectoryVM {
    id: string;
    name: string;
    count: number;
    children?: { id: string; name: string; count: number }[];
  }
}
