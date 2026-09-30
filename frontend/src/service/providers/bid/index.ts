import type { Bid } from '@/typings/bid';
import {
  demoBidDocVersions,
  demoChapters,
  demoChapterContents,
  demoClauses,
  demoConsistencyItems,
  demoGenTemplates,
  demoGenerationRecords,
  demoLayoutObserved,
  demoLayoutRequired,
  demoMaterialDirectories,
  demoMaterialStats,
  demoMaterials,
  demoNotices,
  demoPage12Annotations,
  demoParseFiles,
  demoParseRecords,
  demoProjects,
  demoRequirementVersions,
  demoRevisionDiff,
  demoRevisionOpinions,
  demoReviewIssues,
  demoReviewRecords,
  demoReviewReport,
  demoRiskAlerts,
  demoTemplates,
  demoTemplateVersions,
  demoTodos,
  demoTrend,
  demoTypographyObserved,
  demoTypographyRequired,
  demoWorkbenchStats
} from '@/fixtures/bid';
import { loadDemoState, resetDemoState, saveDemoState } from './demo-state';
import { currentDataMode } from './data-mode';

/** 统一的数据返回包装：state 区分 ok / not-integrated / error，页面据此渲染状态 */
export type BidResult<T> =
  | { state: 'ok'; data: T }
  | { state: 'not-integrated'; message: string }
  | { state: 'error'; message: string };

export function ok<T>(data: T): BidResult<T> {
  return { state: 'ok', data };
}

export function notIntegrated(what: string): BidResult<never> {
  return { state: 'not-integrated', message: `「${what}」暂未接入后端，当前不可用` };
}

function clone<T>(v: T): T {
  return JSON.parse(JSON.stringify(v));
}

/** 模拟网络延迟，让 loading/skeleton 状态可见 */
function delay<T>(data: T, ms = 120): Promise<T> {
  return new Promise(resolve => {
    setTimeout(() => resolve(data), ms);
  });
}

function demoDefaultDraft(): Bid.GenerationDraftVM {
  return {
    id: 'GD-ws-001',
    projectId: 'P-2026-001',
    mode: 'normal',
    requirementVersionId: 'RV-2026-001',
    scope: '技术标',
    templateId: 'TP-005',
    chapters: clone(demoChapters),
    activeChapterId: 'CH-05',
    content: demoChapterContents['CH-05'] || '',
    savedLabel: '演示工作副本（未持久化到服务器）',
    dirty: false,
    materialRefs: [],
    opinionIds: []
  };
}

/**
 * 数据服务接口：页面只依赖此接口，不散落请求代码。
 */
export interface BidProvider {
  mode: Bid.DataMode;
  /** 工作台 */
  getWorkbenchStats(): Promise<BidResult<Bid.WorkbenchStatVM>>;
  getProjects(): Promise<BidResult<Bid.ProjectVM[]>>;
  getTodos(): Promise<BidResult<Bid.TodoVM[]>>;
  toggleTodo(id: string): Promise<BidResult<Bid.TodoVM[]>>;
  getNotices(): Promise<BidResult<Bid.NoticeVM[]>>;
  markNoticeRead(id: string): Promise<BidResult<Bid.NoticeVM[]>>;
  getRiskAlerts(): Promise<BidResult<Bid.RiskAlertVM[]>>;
  getTrend(): Promise<BidResult<Bid.TrendPointVM[]>>;
  /** 解析 */
  getParseFiles(projectId: string): Promise<BidResult<Bid.ParseFileVM[]>>;
  /** 上传文件入库：仅登记元数据并入队（status=pending），真实解析由后端能力完成 */
  createParseFile(input: { projectId: string; name: string; type: string; sizeLabel: string }): Promise<BidResult<Bid.ParseFileVM>>;
  getClauses(fileId: string): Promise<BidResult<Bid.ClauseVM[]>>;
  updateClause(clause: Bid.ClauseVM): Promise<BidResult<Bid.ClauseVM>>;
  getAnnotations(fileId: string): Promise<BidResult<Bid.DocAnnotationVM[]>>;
  getTypography(fileId: string): Promise<BidResult<{ observed: Bid.TypographyVM; required: Bid.TypographyVM }>>;
  getLayout(fileId: string): Promise<BidResult<{ observed: Bid.LayoutVM; required: Bid.LayoutVM }>>;
  getParseRecords(): Promise<BidResult<Bid.ParseRecordVM[]>>;
  getRequirementVersions(projectId?: string): Promise<BidResult<Bid.RequirementVersionVM[]>>;
  /** 生成 */
  getDraft(): Promise<BidResult<Bid.GenerationDraftVM>>;
  saveDraft(draft: Bid.GenerationDraftVM): Promise<BidResult<Bid.GenerationDraftVM>>;
  resetDraft(): Promise<BidResult<Bid.GenerationDraftVM>>;
  getGenTemplates(): Promise<BidResult<{ id: string; name: string }[]>>;
  getRevisionOpinions(reportId?: string): Promise<BidResult<Bid.RevisionOpinionVM[]>>;
  getRevisionDiff(opinionId: string): Promise<BidResult<{ before: string; after: string; note: string }>>;
  getBidDocVersions(projectId: string): Promise<BidResult<{ id: string; name: string; version: string; uploadedAt: string; sizeLabel: string }[]>>;
  getGenerationRecords(): Promise<BidResult<Bid.GenerationRecordVM[]>>;
  /** 审查与报告 */
  getIssues(reportId: string): Promise<BidResult<Bid.ReviewIssueVM[]>>;
  getReport(reportId: string): Promise<BidResult<Bid.ReviewReportVM>>;
  getReports(): Promise<BidResult<Bid.ReviewRecordVM[]>>;
  getConsistency(reportId: string): Promise<BidResult<Bid.ConsistencyItemVM[]>>;
  getReviewRecords(): Promise<BidResult<Bid.ReviewRecordVM[]>>;
  /** 模板与素材 */
  getTemplates(): Promise<BidResult<Bid.TemplateVM[]>>;
  saveTemplate(tpl: Bid.TemplateVM): Promise<BidResult<Bid.TemplateVM>>;
  getTemplateVersions(templateId: string): Promise<BidResult<Bid.TemplateVersionVM[]>>;
  getMaterials(): Promise<BidResult<Bid.MaterialVM[]>>;
  getMaterialDirectories(): Promise<BidResult<Bid.MaterialDirectoryVM[]>>;
  getMaterialStats(): Promise<BidResult<{ total: number; expiringSoon: number; hotRefs: number; monthNew: number }>>;
  saveMaterial(m: Bid.MaterialVM): Promise<BidResult<Bid.MaterialVM>>;
  /** 重置演示数据 */
  resetDemo(): void;
}

/** Demo provider：所有数据来自 fixtures + 独立演示状态，不请求后端 */
function createDemoProvider(): BidProvider {
  const st = loadDemoState();
  let todos: Bid.TodoVM[] = st.todos ? clone(st.todos) : clone(demoTodos);
  let notices: Bid.NoticeVM[] = st.notices ? clone(st.notices) : clone(demoNotices);
  let parseFiles: Bid.ParseFileVM[] = st.parseFiles ? clone(st.parseFiles) : clone(demoParseFiles);
  let draft: Bid.GenerationDraftVM | null = st.draft === undefined ? null : st.draft ? clone(st.draft) : null;

  function persistDraft() {
    saveDemoState({ draft });
  }

  return {
    mode: 'demo',
    getWorkbenchStats: () => delay(ok(clone(demoWorkbenchStats))),
    getProjects: () => delay(ok(clone(demoProjects))),
    getTodos: () => delay(ok(clone(todos))),
    toggleTodo: id => {
      todos = todos.map(t => (t.id === id ? { ...t, done: !t.done } : t));
      saveDemoState({ todos });
      return delay(ok(clone(todos)));
    },
    getNotices: () => delay(ok(clone(notices))),
    markNoticeRead: id => {
      notices = notices.map(n => (n.id === id ? { ...n, read: true } : n));
      saveDemoState({ notices });
      return delay(ok(clone(notices)));
    },
    getRiskAlerts: () => delay(ok(clone(demoRiskAlerts))),
    getTrend: () => delay(ok(clone(demoTrend))),

    getParseFiles: projectId => delay(ok(clone(parseFiles.filter(f => f.projectId === projectId)))),
    createParseFile: input => {
      const d = new Date();
      const p2 = (n: number) => String(n).padStart(2, '0');
      const file: Bid.ParseFileVM = {
        id: `PF-U${d.getTime()}`,
        projectId: input.projectId,
        name: input.name,
        type: input.type,
        sizeLabel: input.sizeLabel,
        pages: 0,
        status: 'pending',
        uploadedAt: `${d.getFullYear()}-${p2(d.getMonth() + 1)}-${p2(d.getDate())} ${p2(d.getHours())}:${p2(d.getMinutes())}`
      };
      parseFiles = [file, ...parseFiles];
      saveDemoState({ parseFiles });
      return delay(ok(clone(file)), 300);
    },
    getClauses: fileId => delay(ok(clone(demoClauses.filter(c => c.fileId === fileId)))),
    updateClause: clause => {
      const idx = demoClauses.findIndex(c => c.id === clause.id);
      if (idx >= 0) Object.assign(demoClauses[idx], clause);
      return delay(ok(clone(clause)));
    },
    getAnnotations: () => delay(ok(clone(demoPage12Annotations))),
    getTypography: () => delay(ok({ observed: clone(demoTypographyObserved), required: clone(demoTypographyRequired) })),
    getLayout: () => delay(ok({ observed: clone(demoLayoutObserved), required: clone(demoLayoutRequired) })),
    getParseRecords: () => delay(ok(clone(demoParseRecords))),
    getRequirementVersions: projectId =>
      delay(ok(clone(projectId ? demoRequirementVersions.filter(v => v.projectId === projectId) : demoRequirementVersions))),

    getDraft: () => {
      if (!draft) draft = demoDefaultDraft();
      return delay(ok(clone(draft)));
    },
    saveDraft: d => {
      draft = clone(d);
      draft.dirty = false;
      draft.savedLabel = `已保存（演示工作副本 · ${new Date().toLocaleTimeString()}）`;
      persistDraft();
      return delay(ok(clone(draft)));
    },
    resetDraft: () => {
      draft = demoDefaultDraft();
      persistDraft();
      return delay(ok(clone(draft)));
    },
    getGenTemplates: () => delay(ok(clone(demoGenTemplates))),
    getRevisionOpinions: reportId =>
      delay(ok(clone(reportId ? demoRevisionOpinions.filter(o => o.reportId === reportId) : demoRevisionOpinions))),
    getRevisionDiff: opinionId => {
      const diff = demoRevisionDiff[opinionId];
      return diff ? delay(ok(clone(diff))) : delay({ state: 'not-integrated', message: '该意见暂无预置修订版本（演示数据未覆盖）' });
    },
    getBidDocVersions: projectId => delay(ok(clone(demoBidDocVersions.filter(v => v.projectId === projectId)))),
    getGenerationRecords: () => delay(ok(clone(demoGenerationRecords))),

    getIssues: reportId => delay(ok(clone(demoReviewIssues.filter(i => i.reportId === reportId)))),
    getReport: reportId => {
      if (reportId === demoReviewReport.id) return delay(ok(clone(demoReviewReport)));
      return delay({ state: 'not-integrated', message: '演示数据中不存在该报告，详情仅提供 RR-2026-005' });
    },
    getReports: () => delay(ok(clone(demoReviewRecords.filter(r => r.status === '已完成')))),
    getConsistency: () => delay(ok(clone(demoConsistencyItems))),
    getReviewRecords: () => delay(ok(clone(demoReviewRecords))),

    getTemplates: () => delay(ok(clone(demoTemplates))),
    saveTemplate: tpl => {
      const idx = demoTemplates.findIndex(t => t.id === tpl.id);
      if (idx >= 0) Object.assign(demoTemplates[idx], tpl);
      return delay(ok(clone(tpl)));
    },
    getTemplateVersions: templateId => delay(ok(clone(demoTemplateVersions.filter(v => v.templateId === templateId)))),
    getMaterials: () => delay(ok(clone(demoMaterials))),
    getMaterialDirectories: () => delay(ok(clone(demoMaterialDirectories))),
    getMaterialStats: () => delay(ok(clone(demoMaterialStats))),
    saveMaterial: m => {
      const idx = demoMaterials.findIndex(x => x.id === m.id);
      if (idx >= 0) Object.assign(demoMaterials[idx], m);
      return delay(ok(clone(m)));
    },

    resetDemo: () => {
      todos = clone(demoTodos);
      notices = clone(demoNotices);
      parseFiles = clone(demoParseFiles);
      draft = demoDefaultDraft();
      saveDemoState({ todos, notices, draft, parseFiles });
    }
  };
}

/** API provider：仅接已有基础接口；业务数据未实现时明确返回 not-integrated，不静默回退 Demo */
function createApiProvider(): BidProvider {
  const n = (what: string) => Promise.resolve(notIntegrated(what));

  return {
    mode: 'api',
    getWorkbenchStats: () => n('工作台统计'),
    getProjects: () => n('项目列表'),
    getTodos: () => n('待办任务'),
    toggleTodo: () => n('待办更新'),
    getNotices: () => n('消息通知'),
    markNoticeRead: () => n('消息已读'),
    getRiskAlerts: () => n('风险预警'),
    getTrend: () => n('任务趋势'),
    getParseFiles: () => n('解析文件列表'),
    createParseFile: () => n('文件上传入库'),
    getClauses: () => n('结构化条款'),
    updateClause: () => n('条款更新'),
    getAnnotations: () => n('版式标注'),
    getTypography: () => n('字体样式分析'),
    getLayout: () => n('页面布局分析'),
    getParseRecords: () => n('解析记录'),
    getRequirementVersions: () => n('招标要求版本'),
    getDraft: () => n('生成草稿'),
    saveDraft: () => n('草稿保存'),
    resetDraft: () => n('草稿重置'),
    getGenTemplates: () => n('投标模板选项'),
    getRevisionOpinions: () => n('整改意见'),
    getRevisionDiff: () => n('修订版本对比'),
    getBidDocVersions: () => n('投标文件版本'),
    getGenerationRecords: () => n('生成记录'),
    getIssues: () => n('审查问题'),
    getReport: () => n('审查报告'),
    getReports: () => n('报告列表'),
    getConsistency: () => n('一致性校验'),
    getReviewRecords: () => n('审查记录'),
    getTemplates: () => n('模板列表'),
    saveTemplate: () => n('模板保存'),
    getTemplateVersions: () => n('模板版本'),
    getMaterials: () => n('企业素材'),
    getMaterialDirectories: () => n('素材目录'),
    getMaterialStats: () => n('素材统计'),
    saveMaterial: () => n('素材保存'),
    resetDemo: () => {
      // api 模式无演示状态
    }
  };
}

/**
 * 运行时按当前数据模式（demo/api）转发调用的 Provider。
 * 头部开关或登录页切换模式后立即生效（无需重启）；每次调用实时解析当前模式。
 */
function createBidProviderProxy(): BidProvider {
  let demoProvider: BidProvider | null = null;
  let apiProvider: BidProvider | null = null;

  const resolve = (): BidProvider => {
    if (currentDataMode() === 'api') {
      apiProvider ??= createApiProvider();
      return apiProvider;
    }
    demoProvider ??= createDemoProvider();
    return demoProvider;
  };

  return new Proxy({} as BidProvider, {
    get(_target, prop: string | symbol) {
      const provider = resolve();
      const value = Reflect.get(provider as object, prop);
      return typeof value === 'function' ? (value as (...args: unknown[]) => unknown).bind(provider) : value;
    }
  });
}

export const bidProvider: BidProvider = createBidProviderProxy();

/** 当前数据模式（demo=Mock / api=真实接口），供 UI 展示 */
export { currentDataMode } from './data-mode';
export type { BidDataMode } from './data-mode';
export { isDemoActive, readStoredDataMode, setDataMode } from './data-mode';

/** 供外部读取演示状态（重置按钮等） */
export { resetDemoState };



