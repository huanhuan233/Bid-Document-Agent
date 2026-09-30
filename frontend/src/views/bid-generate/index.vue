<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { onBeforeRouteLeave, useRoute } from 'vue-router';
import { useRouterPush } from '@/hooks/common/router';
import { useBidData } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import { chapterStatusMap } from '@/constants/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import StepBar from '@/components/business/step-bar.vue';
import MaterialSelectDrawer from '@/components/business/material-select-drawer.vue';
import RichEditor from './modules/rich-editor.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'BidGenerateCenter' });

const route = useRoute();
const { routerPush } = useRouterPush();

const { data: projects } = useBidData(() => bidProvider.getProjects(), [] as Bid.ProjectVM[]);
const projectId = ref((route.query.projectId as string) || 'P-2026-001');

/** 修订模式上下文（来自审查中心跳转：projectId + requirementsVersionId + documentVersionId + reviewReportId + issueIds） */
const mode = ref<'normal' | 'revision'>((route.query.mode as 'revision') || 'normal');
const reportId = ref((route.query.reviewReportId as string) || '');
const docVersionId = ref((route.query.documentVersionId as string) || '');
const incomingIssueIds = ((route.query.issueIds as string) || '').split(',').filter(Boolean);

const scope = ref<'商务标' | '技术标' | '整本草稿'>('技术标');
const requirementVersionId = ref((route.query.requirementsVersionId as string) || 'RV-2026-001');

const { data: requirementVersions } = useBidData(() => bidProvider.getRequirementVersions(projectId.value), []);
const { data: reportOptions } = useBidData(() => bidProvider.getReports(), [] as Bid.ReviewRecordVM[]);
const { data: docVersions } = useBidData(() => bidProvider.getBidDocVersions(projectId.value), []);
const { data: genTemplates } = useBidData(() => bidProvider.getGenTemplates(), [] as { id: string; name: string }[]);
const { data: opinions } = useBidData(() => bidProvider.getRevisionOpinions(reportId.value || undefined), [] as Bid.RevisionOpinionVM[]);
const { data: materials } = useBidData(() => bidProvider.getMaterials(), [] as Bid.MaterialVM[]);

watch(projectId, () => {
  // 项目切换后，旧项目的成果不能继续隐性选中
  requirementVersionId.value = '';
  reportId.value = '';
  docVersionId.value = '';
});

const stepActive = ref(2);
const steps = [
  { title: '选择依据', desc: '选择项目与关联结果' },
  { title: '配置内容', desc: '选择模板与章节，设置生成参数' },
  { title: '生成草稿', desc: 'AI 生成初稿内容' },
  { title: '人工润色', desc: '检查修改，完善内容' },
  { title: '导出定稿', desc: '生成完整投标文件' }
];

/** 演示工作副本 */
const draft = ref<Bid.GenerationDraftVM | null>(null);
const draftLoading = ref(false);

async function loadDraft() {
  draftLoading.value = true;
  const res = await bidProvider.getDraft();
  if (res.state === 'ok') draft.value = res.data;
  draftLoading.value = false;
}

onMounted(() => {
  loadDraft();
  if (incomingIssueIds.length) {
    window.$message?.success(`已按 ${incomingIssueIds.length} 条整改意见进入修订模式（演示）`);
  }
});

const templateId = computed({
  get: () => draft.value?.templateId || 'TP-005',
  set: v => {
    if (draft.value) {
      draft.value.templateId = v;
      draft.value.dirty = true;
    }
  }
});

const activeChapter = computed(() => draft.value?.chapters.find(c => c.id === draft.value?.activeChapterId) || null);
const activeContent = ref('');

watch(
  () => draft.value?.activeChapterId,
  id => {
    const chapter = draft.value?.chapters.find(c => c.id === id);
    if (chapter?.status === 'pending' || chapter?.status === 'unconfigured') {
      activeContent.value = `<h1>${chapter.name}</h1><p style="color:#909399">（本章节尚未生成内容，可在右侧查看生成建议后点击“智能生成”）</p>`;
    } else {
      activeContent.value = draft.value?.content || '';
    }
  },
  { immediate: true }
);

function onContentChange() {
  if (draft.value) draft.value.dirty = true;
}

function selectChapter(c: Bid.ChapterVM) {
  if (draft.value) draft.value.activeChapterId = c.id;
}

function toggleChapter(c: Bid.ChapterVM) {
  c.selected = !c.selected;
  if (draft.value) draft.value.dirty = true;
}

/** 模式切换保护：有未保存修改时先询问 */
async function switchMode(m: 'normal' | 'revision') {
  if (m === mode.value) return;
  if (draft.value?.dirty) {
    try {
      await window.$messageBox?.confirm('切换生成方式前，是否先保存当前草稿？', '提示', {
        confirmButtonText: '保存并切换',
        cancelButtonText: '直接切换',
        type: 'warning'
      });
      await doSave();
    } catch {
      // 用户选择直接切换
    }
  }
  mode.value = m;
  if (draft.value) {
    draft.value.mode = m;
    draft.value.dirty = true;
  }
  if (m === 'revision' && !reportId.value) {
    reportId.value = 'RR-2026-005';
  }
  window.$message?.info(m === 'revision' ? '已进入“按整改意见修订”模式（演示）' : '已切换为常规生成模式');
}

async function doSave() {
  if (!draft.value) return;
  const res = await bidProvider.saveDraft({ ...draft.value, mode: mode.value });
  if (res.state === 'ok') {
    draft.value = res.data;
    window.$message?.success('草稿已保存（演示工作副本，未写入服务器）');
  }
}

/** 主执行按钮：常规=智能生成；修订=按整改意见修订 */
const generating = ref(false);

function mainAction() {
  if (reportMismatch()) return;
  if (mode.value === 'revision') {
    const target = opinions.value.find(o => o.id === 'OP-004') || opinions.value[0];
    if (target) openDiff(target);
    window.$message?.info('演示修订：加载明确标注的预置版本（不调用真实模型）');
  } else {
    generating.value = true;
    window.$message?.info('智能生成为核心能力：暂未接入，当前编辑的是演示工作副本');
    setTimeout(() => {
      generating.value = false;
      if (draft.value) {
        const chapter = draft.value.chapters.find(c => c.status === 'pending');
        if (chapter) {
          chapter.status = 'generated';
          draft.value.dirty = true;
        }
      }
    }, 1000);
  }
}

function exportWord() {
  window.$message?.warning('导出 Word 依赖文档生成服务：暂未接入，未生成真实文件');
}

function exportDemoJson() {
  if (!draft.value) return;
  const payload = { draft: draft.value, exportedAt: new Date().toISOString(), note: '演示数据导出（JSON），非 Word 文件' };
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `draft-${draft.value.id}-demo.json`;
  a.click();
  URL.revokeObjectURL(url);
}

/** 素材选择 */
const materialDrawerVisible = ref(false);

function addMaterialRefs(selected: { id: string; name: string }[]) {
  if (!draft.value) return;
  selected.forEach(s => {
    if (!draft.value!.materialRefs.find(m => m.id === s.id)) draft.value!.materialRefs.push(s);
  });
  draft.value.dirty = true;
  window.$message?.success(`已加入 ${selected.length} 条素材引用（演示工作副本）`);
}

function removeMaterialRef(id: string) {
  if (!draft.value) return;
  draft.value.materialRefs = draft.value.materialRefs.filter(m => m.id !== id);
  draft.value.dirty = true;
}

/** 整改意见筛选、选择与章节定位 */
const opinionPriorityFilter = ref<'' | 'high' | 'medium' | 'low'>('');
const filteredOpinions = computed(() =>
  opinions.value.filter(o => (opinionPriorityFilter.value ? o.priority === opinionPriorityFilter.value : true))
);

function locateOpinion(o: Bid.RevisionOpinionVM) {
  if (draft.value) {
    draft.value.activeChapterId = o.targetChapterId;
    draft.value.opinionIds = Array.from(new Set([...draft.value.opinionIds, o.id]));
    draft.value.dirty = true;
  }
  window.$message?.success(`已定位到章节「${draft.value?.chapters.find(c => c.id === o.targetChapterId)?.name}」`);
}

/** 版本对比抽屉（预置前后版本，明确标注演示） */
const diffVisible = ref(false);
const diffData = ref<{ before: string; after: string; note: string } | null>(null);
const diffLoading = ref(false);

async function openDiff(o: Bid.RevisionOpinionVM) {
  diffVisible.value = true;
  diffLoading.value = true;
  const res = await bidProvider.getRevisionDiff(o.id);
  diffData.value = res.state === 'ok' ? res.data : null;
  diffLoading.value = false;
}

/** 审查报告与项目不匹配校验：不默默混用 */
function reportMismatch() {
  const report = reportOptions.value.find(r => r.id === reportId.value);
  if (report && report.projectId !== projectId.value) {
    window.$message?.error('所选审查报告不属于当前项目，请重新选择');
    return true;
  }
  return false;
}

/** 未保存离开提醒 */
onBeforeRouteLeave(async () => {
  if (draft.value?.dirty) {
    try {
      await window.$messageBox?.confirm('当前章节内容尚未保存，确定离开吗？', '未保存提醒', {
        confirmButtonText: '离开',
        cancelButtonText: '留下',
        type: 'warning'
      });
      return true;
    } catch {
      return false;
    }
  }
  return true;
});

const progressPercent = computed(() => {
  if (!draft.value) return 0;
  const done = draft.value.chapters.filter(c => c.status === 'generated').length;
  return Math.round((done / draft.value.chapters.length) * 100);
});

function goRecords() {
  routerPush({ key: 'generate-records' });
}


</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px [&>*]:shrink-0">
    <div class="flex flex-wrap items-end justify-between gap-12px">
      <div class="min-w-0">
        <h1 class="m-0 text-26px font-600 c-text">标书生成中心</h1>
        <p class="mt-4px mb-0 text-13px c-secondary">基于招标要求、企业素材与模板智能生成投标文件，支持基于审查意见的修订生成</p>
      </div>
      <div class="flex items-center gap-12px">
        <DemoBadge />
        <ElButton @click="goRecords"><SvgIcon icon="mdi:history" class="mr-4px" /> 生成记录</ElButton>
      </div>
    </div>

    <!-- 顶部配置 -->
    <ElCard shadow="never">
      <div class="flex flex-wrap items-center gap-12px">
        <span class="text-14px c-secondary">所属项目</span>
        <ElSelect v-model="projectId" class="w-240px">
          <ElOption v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </ElSelect>
        <span class="text-14px c-secondary">关联解析结果</span>
        <ElSelect v-model="requirementVersionId" placeholder="选择已解析的招标要求版本" class="w-280px">
          <ElOption v-for="v in requirementVersions" :key="v.id" :label="`${v.name} ${v.version}${v.confirmed ? '（已确认）' : ''}`" :value="v.id" />
        </ElSelect>
        <span class="text-14px c-secondary">生成方式</span>
        <ElRadioGroup :model-value="mode" @change="(v: any) => switchMode(v)">
          <ElRadioButton value="normal">常规生成</ElRadioButton>
          <ElRadioButton value="revision">
            按整改意见修订
            <ElTag class="ml-4px" size="small" type="warning" effect="light">基于审查意见</ElTag>
          </ElRadioButton>
        </ElRadioGroup>
        <template v-if="mode === 'revision'">
          <span class="text-14px c-secondary">关联审查报告</span>
          <ElSelect v-model="reportId" placeholder="选择审查报告" class="w-240px">
            <ElOption v-for="r in reportOptions" :key="r.id" :label="`${r.id}（${r.projectName}）`" :value="r.id" />
          </ElSelect>
          <span class="text-14px c-secondary">投标文件版本</span>
          <ElSelect v-model="docVersionId" placeholder="选择需要修订的版本" class="w-220px">
            <ElOption v-for="v in docVersions" :key="v.id" :label="`${v.name} ${v.version}`" :value="v.id" />
          </ElSelect>
        </template>
        <span class="text-14px c-secondary">内容范围</span>
        <ElRadioGroup v-model="scope">
          <ElRadioButton value="商务标">商务标</ElRadioButton>
          <ElRadioButton value="技术标">技术标</ElRadioButton>
          <ElRadioButton value="整本草稿">整本草稿</ElRadioButton>
        </ElRadioGroup>
      </div>
      <div class="mt-10px flex flex-wrap items-center gap-10px">
        <ElButton icon="mdi:bookshelf" @click="materialDrawerVisible = true">导入企业素材</ElButton>
        <ElButton type="primary" icon="mdi:creation" :loading="generating" @click="mainAction">
          {{ mode === 'revision' ? '按整改意见修订' : '智能生成' }}
        </ElButton>
        <ElButton icon="mdi:content-save" @click="doSave">保存草稿</ElButton>
        <ElButton icon="mdi:file-word-box" @click="exportWord">导出 Word</ElButton>
        <ElButton link type="info" icon="mdi:code-json" @click="exportDemoJson">导出演示数据(JSON)</ElButton>
        <span v-if="draft?.dirty" class="text-12px c-warning">有未保存修改</span>
        <span v-else-if="draft" class="text-12px c-secondary">{{ draft.savedLabel }}</span>
      </div>
    </ElCard>

    <ElCard shadow="never">
      <StepBar :steps="steps" :active="stepActive" />
    </ElCard>

    <!-- 三栏主体 -->
    <div class="grid gap-16px xl:grid-cols-[300px_1fr_340px]">
      <!-- 左：模板与章节大纲 -->
      <ElCard shadow="never" class="min-w-0">
        <template #header><span class="font-600">章节大纲 / 生成配置</span></template>
        <div class="mb-10px flex items-center gap-8px">
          <span class="shrink-0 text-13px c-secondary">投标模板</span>
          <ElSelect v-model="templateId" size="small">
            <ElOption v-for="t in genTemplates" :key="t.id" :label="t.name" :value="t.id" />
          </ElSelect>
        </div>
        <div class="mb-6px flex items-center justify-between text-12px c-secondary">
          <span>章节</span>
          <span>已选 {{ draft?.chapters.filter(c => c.selected).length || 0 }} / {{ draft?.chapters.length || 0 }} 章</span>
        </div>
        <div class="flex flex-col gap-4px overflow-auto max-h-480px">
          <div
            v-for="c in draft?.chapters"
            :key="c.id"
            class="flex cursor-pointer items-center gap-8px rounded-8px px-8px py-8px"
            :class="draft?.activeChapterId === c.id ? 'bg-primary/10' : 'hover:bg-gray-1'"
            @click="selectChapter(c)"
          >
            <ElCheckbox :model-value="c.selected" @click.stop @change="toggleChapter(c)" />
            <span class="min-w-0 flex-1 truncate text-14px">{{ c.name }}</span>
            <span class="shrink-0 text-12px" :style="{ color: chapterStatusMap[c.status].color }">● {{ chapterStatusMap[c.status].label }}</span>
          </div>
        </div>
      </ElCard>

      <!-- 中：文档编辑 -->
      <ElCard v-loading="draftLoading" shadow="never" class="min-w-0">
        <div class="mb-6px flex items-center gap-8px">
          <ElTag v-if="activeChapter" effect="plain">{{ activeChapter.name }}</ElTag>
          <span v-if="activeChapter" class="text-12px" :style="{ color: chapterStatusMap[activeChapter.status].color }">
            {{ chapterStatusMap[activeChapter.status].label }}
          </span>
          <span v-if="draft?.dirty" class="ml-auto text-12px c-warning">未保存</span>
          <span v-else class="ml-auto text-12px c-secondary">{{ draft?.savedLabel }}</span>
        </div>
        <RichEditor v-if="activeChapter" v-model="activeContent" @change="onContentChange" />
        <ElSkeleton v-else :rows="6" animated />
      </ElCard>

      <!-- 右：要求与素材辅助 -->
      <ElCard shadow="never" class="min-w-0">
        <ElTabs model-value="opinions">
          <ElTabPane label="整改意见" name="opinions">
            <div class="mb-8px flex items-center justify-between">
              <span class="text-13px font-600">审查整改意见（{{ filteredOpinions.length }}）</span>
              <ElSelect v-model="opinionPriorityFilter" size="small" class="w-110px" placeholder="优先级">
                <ElOption label="全部" value="" />
                <ElOption label="高" value="high" />
                <ElOption label="中" value="medium" />
                <ElOption label="低" value="low" />
              </ElSelect>
            </div>
            <div class="flex flex-col gap-8px overflow-auto max-h-420px">
              <div v-for="o in filteredOpinions" :key="o.id" class="rounded-8px border border-solid border-gray-2 p-10px">
                <div class="text-14px font-500">{{ o.title }}</div>
                <div class="mt-4px text-12px c-secondary">{{ o.desc }}</div>
                <div class="mt-6px flex flex-wrap items-center gap-6px">
                  <ElTag size="small" :type="o.priority === 'high' ? 'danger' : o.priority === 'medium' ? 'warning' : 'info'">
                    {{ o.priority === 'high' ? '高优先级' : o.priority === 'medium' ? '中优先级' : '低优先级' }}
                  </ElTag>
                  <ElTag size="small" :type="o.status === '已复核' ? 'success' : o.status === '部分修订' ? 'primary' : 'warning'">
                    {{ o.status }}
                  </ElTag>
                  <ElButton link type="primary" size="small" @click="locateOpinion(o)">定位章节</ElButton>
                  <ElButton link size="small" @click="openDiff(o)">查看修订版本</ElButton>
                </div>
              </div>
              <ElEmpty v-if="!filteredOpinions.length" description="暂无整改意见（常规生成模式可不填审查报告）" />
            </div>
            <div v-if="opinions.length" class="mt-8px text-12px c-secondary">
              说明：「已修订」≠「已复核通过」；修订效果由明确标注的预置版本演示，不调用模型。
            </div>
          </ElTabPane>

          <ElTabPane label="企业素材" name="materials">
            <ElButton size="small" class="mb-8px" @click="materialDrawerVisible = true">+ 选择素材</ElButton>
            <div class="flex flex-col gap-6px">
              <div v-for="m in draft?.materialRefs" :key="m.id" class="flex items-center gap-6px rounded-6px bg-gray-1 px-8px py-6px text-13px">
                <SvgIcon icon="mdi:file-document-outline" class="c-primary" />
                <span class="min-w-0 flex-1 truncate">{{ m.name }}</span>
                <ElButton link type="danger" size="small" @click="removeMaterialRef(m.id)">移除</ElButton>
              </div>
              <ElEmpty v-if="!draft?.materialRefs.length" description="尚未引用素材" />
            </div>
          </ElTabPane>

          <ElTabPane label="生成建议" name="advice">
            <ul class="m-0 pl-18px text-13px leading-24px">
              <li>建议在项目经理章节引用企业资质库相关证书（对应整改意见 OP-001）</li>
              <li>建议补充近三年同类项目案例并关联验收证明</li>
              <li>建议补充数据安全管理制度与应急预案</li>
              <li>建议对照评分标准检查并补充未覆盖章节内容</li>
            </ul>
            <p class="mt-8px text-12px c-secondary">生成建议为演示样例，智能建议核心能力暂未接入。</p>
          </ElTabPane>

          <ElTabPane label="引用证据" name="evidence">
            <div class="flex flex-col gap-6px">
              <div v-for="m in draft?.materialRefs" :key="m.id" class="rounded-6px bg-gray-1 px-8px py-6px text-13px">
                引用素材：<b>{{ m.name }}</b>
              </div>
              <ElEmpty v-if="!draft?.materialRefs.length" description="暂无引用证据" />
            </div>
          </ElTabPane>
        </ElTabs>
      </ElCard>
    </div>


    <!-- 底部摘要 -->
    <ElCard shadow="never">
      <div class="flex flex-wrap items-center gap-32px">
        <div class="flex items-center gap-10px">
          <ElProgress type="dashboard" :percentage="progressPercent" :width="72" />
          <div>
            <div class="text-14px font-600">生成进度</div>
            <div class="text-12px c-secondary">演示状态机</div>
          </div>
        </div>
        <div><b class="text-20px">{{ draft?.chapters.filter(c => c.status === 'generated').length || 0 }}</b><div class="text-12px c-secondary">已生成章节</div></div>
        <div><b class="text-20px c-warning">{{ draft?.chapters.filter(c => c.status === 'revise').length || 0 }}</b><div class="text-12px c-secondary">待整改章节</div></div>
        <div><b class="text-20px">{{ opinions.length }}</b><div class="text-12px c-secondary">整改意见</div></div>
        <div><b class="text-20px">{{ draft?.materialRefs.length || 0 }}</b><div class="text-12px c-secondary">引用素材</div></div>
        <div class="ml-auto max-w-320px text-right text-12px c-secondary">
          真实生成、修订与 Word 导出为待接入核心能力；当前页面均为演示工作副本。
        </div>
      </div>
    </ElCard>
  </div>

  <MaterialSelectDrawer v-model:visible="materialDrawerVisible" :materials="materials" @confirm="addMaterialRefs" />

  <ElDrawer v-model="diffVisible" title="修订前后版本对比（演示预置版本）" size="640px">
    <ElSkeleton v-if="diffLoading" :rows="8" animated />
    <template v-else-if="diffData">
      <ElAlert type="warning" :closable="false" :title="diffData.note" class="mb-12px" />
      <div class="grid grid-cols-2 gap-10px">
        <div>
          <div class="mb-4px text-13px font-600 c-secondary">修订前</div>
          <div class="whitespace-pre-wrap rounded-8px bg-danger/6 p-10px text-13px leading-22px">{{ diffData.before }}</div>
        </div>
        <div>
          <div class="mb-4px text-13px font-600 c-success">修订后</div>
          <div class="whitespace-pre-wrap rounded-8px bg-success/8 p-10px text-13px leading-22px">{{ diffData.after }}</div>
        </div>
      </div>
    </template>
    <ElEmpty v-else description="该意见暂无预置修订版本（演示数据未覆盖）" />
  </ElDrawer>
</template>

