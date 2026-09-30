<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useRouterPush } from '@/hooks/common/router';
import { useBidData } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import { clauseCategoryLabelMap, parseStatusMap } from '@/constants/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import StepBar from '@/components/business/step-bar.vue';
import DocPreview from '@/components/business/doc-preview.vue';
import NotIntegrated from '@/components/business/not-integrated.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'BidParseCenter' });

const route = useRoute();
const { routerPush } = useRouterPush();

const { data: projects } = useBidData(() => bidProvider.getProjects(), [] as Bid.ProjectVM[]);
const projectId = ref((route.query.projectId as string) || 'P-2026-001');
const fileType = ref<'招标文件' | '标准文件' | '技术规范'>('招标文件');
const stepActive = ref(4);

const steps = [
  { title: '上传文件', desc: '上传招标文件或相关文件' },
  { title: 'OCR与版式解析', desc: '识别文字、表格、图表' },
  { title: '结构化抽取', desc: '提取条款、字段、关键要素' },
  { title: '规则识别', desc: '匹配业务规则，识别风险项' },
  { title: '结果确认', desc: '人工确认与修正' }
];

const { data: files, load: loadFiles } = useBidData(() => bidProvider.getParseFiles(projectId.value), [] as Bid.ParseFileVM[]);
watch(projectId, () => loadFiles());

const activeFile = ref<Bid.ParseFileVM | null>(null);
watch(
  files,
  list => {
    if (!activeFile.value || !list.find(f => f.id === activeFile.value?.id)) {
      activeFile.value = list[0] || null;
    }
  },
  { immediate: true }
);

const parsedFile = computed(() => files.value.find(f => f.status === 'done') || null);
const resultFile = computed(() => (activeFile.value?.status === 'done' ? activeFile.value : parsedFile.value));

const { data: clauses } = useBidData(() => bidProvider.getClauses(resultFile.value?.id || ''), [] as Bid.ClauseVM[]);
const { data: annotations } = useBidData(() => bidProvider.getAnnotations(resultFile.value?.id || ''), []);

const activeCategory = ref<Bid.ClauseCategory>('qualification');
const categoryTabs = (Object.keys(clauseCategoryLabelMap) as Bid.ClauseCategory[]).map(k => ({
  key: k,
  label: clauseCategoryLabelMap[k]
}));

const clausesOfCategory = computed(() => clauses.value.filter(c => c.category === activeCategory.value));

const activeClause = ref<Bid.ClauseVM | null>(null);
const clauseForm = ref({ name: '', content: '', importance: '中', page: 1 });
const clauseDirty = ref(false);

watch(
  clausesOfCategory,
  list => {
    if (list.length && (!activeClause.value || !list.find(c => c.id === activeClause.value?.id))) {
      selectClause(list[0]);
    }
    if (!list.length) {
      activeClause.value = null;
    }
  },
  { immediate: true }
);

function selectClause(c: Bid.ClauseVM) {
  activeClause.value = c;
  clauseForm.value = { name: c.name, content: c.content, importance: c.importance, page: c.page };
  clauseDirty.value = false;
}

function onClauseEdit() {
  clauseDirty.value = true;
}

async function saveClause() {
  if (!activeClause.value) return;
  await bidProvider.updateClause({
    ...activeClause.value,
    ...clauseForm.value,
    importance: clauseForm.value.importance as Bid.ClauseVM['importance']
  });
  clauseDirty.value = false;
  window.$message?.success('条款已更新（演示工作副本）');
}

/** 预览控制 */
const page = ref(12);
const scale = ref(0.8);
const showAnnotations = ref(true);
const activeAnnotationId = ref<string | null>(null);

function selectAnnotation(a: Bid.DocAnnotationVM | null) {
  if (!a) return;
  activeAnnotationId.value = a.id;
}

/** 条款联动：点击条款高亮来源页码 */
function locateClause(c: Bid.ClauseVM) {
  selectClause(c);
  if (c.page) page.value = c.page;
  window.$message?.info(`已定位到来源页：第 ${c.page} 页`);
}

const parseStarted = ref(false);

function startParse() {
  if (!activeFile.value) {
    window.$message?.warning('请先在文件列表中选择一个文件');
    return;
  }
  if (activeFile.value.status !== 'pending') {
    window.$message?.info('该文件已解析或解析中，无需重复解析');
    return;
  }
  parseStarted.value = true;
  window.$message?.info('演示环境：解析核心能力暂未接入，真实文件不会被解析');
  setTimeout(() => {
    parseStarted.value = false;
    stepActive.value = 4;
  }, 1200);
}

function exportResult() {
  if (!resultFile.value) {
    window.$message?.warning('尚无已解析完成的文件，无法导出');
    return;
  }
  const payload = {
    fileId: resultFile.value.id,
    file: resultFile.value.name,
    exportedAt: new Date().toISOString(),
    note: '演示数据导出（JSON），非真实解析引擎结果',
    clauses: clauses.value
  };
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `parse-result-${resultFile.value.id}-demo.json`;
  a.click();
  URL.revokeObjectURL(url);
  window.$message?.success('已导出演示数据 JSON');
}

/** 解析中心上传入库：仅支持 PDF / Word，选择后登记元数据并加入解析队列 */
const uploadBusy = ref(false);

async function onParseUpload(file: File) {
  const ext = (file.name.split('.').pop() || '').toLowerCase();
  if (!['pdf', 'doc', 'docx'].includes(ext)) {
    window.$message?.error('仅支持 PDF / Word（.pdf、.doc、.docx）文件');
    return false;
  }
  if (file.size > 100 * 1024 * 1024) {
    window.$message?.error('文件大小不能超过 100MB');
    return false;
  }
  uploadBusy.value = true;
  try {
    const res = await bidProvider.createParseFile({
      projectId: projectId.value,
      name: file.name,
      type: fileType.value,
      sizeLabel: `${(file.size / 1024 / 1024).toFixed(1)} MB`
    });
    if (res.state === 'ok') {
      await loadFiles();
      activeFile.value = res.data;
      stepActive.value = 0;
      window.$message?.success(`「${file.name}」已上传入库，可在下方点击「开始解析」`);
    } else {
      window.$message?.warning(res.message);
    }
  } catch {
    window.$message?.error('上传入库失败，请重试');
  } finally {
    uploadBusy.value = false;
  }
  return false; // 阻止 ElUpload 默认上传行为（入库由 provider 完成）
}

function goRecords() {
  routerPush({ key: 'parse-records' });
}

function goResultDetail() {
  if (!resultFile.value) {
    window.$message?.warning('尚无已解析完成的文件');
    return;
  }
  routerPush({ key: 'parse-result', params: { id: resultFile.value.id } });
}

const evidenceRows = computed(() =>
  clauses.value.slice(0, 6).map(c => ({
    id: c.id,
    code: c.code,
    name: c.name,
    page: c.page,
    content: c.content,
    confidence: c.confidence,
    category: clauseCategoryLabelMap[c.category]
  }))
);
</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px [&>*]:shrink-0">
    <div class="flex flex-wrap items-end justify-between gap-12px shrink-0">
      <div class="min-w-0">
        <h1 class="m-0 text-26px font-600 c-text">标书解析中心</h1>
        <p class="mt-4px mb-0 text-13px c-secondary">解析招标文件、标准文件并提取结构化要求</p>
      </div>
      <div class="flex items-center gap-12px">
        <DemoBadge />
        <ElUpload :show-file-list="false" :before-upload="onParseUpload" accept=".pdf,.doc,.docx">
          <ElButton type="primary" icon="mdi:upload" :loading="uploadBusy">文件解析</ElButton>
        </ElUpload>
        <ElButton @click="goRecords">
          <SvgIcon icon="mdi:history" class="mr-4px" /> 解析记录
        </ElButton>
      </div>
    </div>

    <NotIntegrated
      v-if="!files.length"
      :message="`项目 ${projectId} 下暂无解析文件`"
      action-text="切换到演示项目"
      @action="projectId = 'P-2026-001'"
    />

    <template v-else>
      <!-- 顶部操作条 -->
      <ElCard shadow="never" class="shrink-0">
        <div class="flex flex-wrap items-center gap-12px">
          <span class="text-14px c-secondary">所属项目</span>
          <ElSelect v-model="projectId" class="w-260px">
            <ElOption v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </ElSelect>
          <ElRadioGroup v-model="fileType">
            <ElRadioButton value="招标文件">招标文件</ElRadioButton>
            <ElRadioButton value="标准文件">标准文件</ElRadioButton>
            <ElRadioButton value="技术规范">技术规范</ElRadioButton>
          </ElRadioGroup>
          <div class="ml-auto flex flex-wrap items-center gap-10px">
            <ElUpload :show-file-list="false" :before-upload="onParseUpload" accept=".pdf,.doc,.docx">
              <ElButton icon="mdi:upload">上传文件</ElButton>
            </ElUpload>
            <ElButton type="primary" icon="mdi:play" :loading="parseStarted" @click="startParse">开始解析</ElButton>
            <ElButton icon="mdi:download" @click="exportResult">导出结构化结果</ElButton>
          </div>
        </div>
      </ElCard>

      <ElCard shadow="never" class="shrink-0">
        <StepBar :steps="steps" :active="stepActive" />
      </ElCard>

      <!-- 三栏主体 -->
      <div class="grid gap-16px xl:grid-cols-[320px_1fr_360px] shrink-0">
        <!-- 左：文件列表 -->
        <ElCard shadow="never" class="min-w-0">
          <template #header>
            <span class="font-600">文件列表（{{ files.length }}）</span>
          </template>
          <div class="flex flex-col gap-10px overflow-auto max-h-560px">
            <div
              v-for="f in files"
              :key="f.id"
              class="cursor-pointer rounded-10px border border-solid p-10px"
              :class="activeFile?.id === f.id ? 'border-primary bg-primary/5' : 'border-gray-2 hover:border-primary/50'"
              @click="activeFile = f"
            >
              <div class="flex items-center gap-8px">
                <SvgIcon
                  :icon="f.name.endsWith('.pdf') ? 'mdi:file-pdf-box' : 'mdi:file-word-box'"
                  :class="f.name.endsWith('.pdf') ? 'c-danger' : 'c-primary'"
                  class="text-22px shrink-0"
                />
                <div class="min-w-0 flex-1">
                  <div class="truncate text-13px font-500 c-text">{{ f.name }}</div>
                  <div class="text-12px c-secondary">{{ f.uploadedAt }} · {{ f.sizeLabel }}</div>
                </div>
              </div>
              <div class="mt-6px flex items-center justify-between">
                <ElTag size="small" :type="parseStatusMap[f.status].type">{{ parseStatusMap[f.status].label }}</ElTag>
                <span class="text-12px c-secondary">共 {{ f.pages }} 页</span>
              </div>
            </div>
          </div>
        </ElCard>

        <!-- 中：原文预览 -->
        <ElCard shadow="never" class="min-w-0">
          <template #header>
            <div class="flex flex-wrap items-center justify-between gap-8px">
              <span class="font-600">原文预览 / 解析标注</span>
              <div class="flex flex-wrap items-center gap-8px">
                <ElInputNumber v-model="page" :min="1" :max="resultFile?.pages || 152" size="small" controls-position="right" class="w-100px" />
                <ElButton size="small" @click="scale = Math.max(0.5, +(scale - 0.1).toFixed(1))">
                  <SvgIcon icon="mdi:magnify-minus" />
                </ElButton>
                <span class="text-12px c-secondary">{{ Math.round(scale * 100) }}%</span>
                <ElButton size="small" @click="scale = Math.min(2, +(scale + 0.1).toFixed(1))">
                  <SvgIcon icon="mdi:magnify-plus" />
                </ElButton>
                <ElButton size="small" @click="scale = 0.8">适应宽度</ElButton>
                <ElSwitch v-model="showAnnotations" active-text="显示标注" size="small" />
              </div>
            </div>
          </template>

          <div v-if="activeFile?.status !== 'done'" class="flex-col-center gap-8px py-40px">
            <SvgIcon icon="mdi:file-alert-outline" class="text-40px c-secondary" />
            <p class="m-0 text-14px c-secondary">该文件尚未解析完成，暂无解析结果</p>
            <p class="m-0 max-w-90% text-center text-12px c-secondary">
              演示样例结果仅绑定已解析完成的招标文件；真实文件不会被标注演示结果
            </p>
          </div>
          <DocPreview
            v-else
            :page="page"
            :annotations="page === 12 ? annotations : []"
            :scale="scale"
            :show-annotations="showAnnotations"
            :active-id="activeAnnotationId"
            show-margin-guides
            @select="selectAnnotation"
          />
        </ElCard>

        <!-- 右：结构化结果 -->
        <ElCard shadow="never" class="min-w-0">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-600">结构化结果</span>
              <ElButton link type="primary" @click="goResultDetail">查看详情</ElButton>
            </div>
          </template>

          <div v-if="activeFile?.status !== 'done'" class="py-24px">
            <ElEmpty description="尚无解析结果" />
          </div>
          <template v-else>
            <ElTabs v-model="activeCategory">
              <ElTabPane v-for="t in categoryTabs" :key="t.key" :label="t.label" :name="t.key" />
            </ElTabs>

            <div class="mt-8px flex flex-col gap-8px overflow-auto max-h-200px">
              <div
                v-for="c in clausesOfCategory"
                :key="c.id"
                class="cursor-pointer rounded-8px border border-solid p-8px text-13px"
                :class="activeClause?.id === c.id ? 'border-primary bg-primary/5' : 'border-gray-2 hover:border-primary/40'"
                @click="locateClause(c)"
              >
                <div class="flex items-center gap-6px">
                  <ElTag size="small" effect="plain">{{ c.code }}</ElTag>
                  <span class="min-w-0 flex-1 truncate font-500">{{ c.name }}</span>
                  <ElTag size="small" :type="c.importance === '高' ? 'danger' : c.importance === '中' ? 'warning' : 'info'">
                    {{ c.importance }}
                  </ElTag>
                </div>
                <div class="mt-4px text-12px c-secondary">{{ c.content }}</div>
                <div class="mt-4px text-12px c-secondary">来源页码：第 {{ c.page }} 页 · 置信度 {{ c.confidence }}</div>
              </div>
              <ElEmpty v-if="!clausesOfCategory.length" description="该分类暂无条款" />
            </div>

            <ElDivider>条款编辑</ElDivider>
            <ElForm v-if="activeClause" label-width="76px" size="small">
              <ElFormItem label="条款名称">
                <ElInput v-model="clauseForm.name" @input="onClauseEdit" />
              </ElFormItem>
              <ElFormItem label="条款内容">
                <ElInput v-model="clauseForm.content" type="textarea" :rows="3" @input="onClauseEdit" />
              </ElFormItem>
              <ElFormItem label="重要程度">
                <ElRadioGroup v-model="clauseForm.importance" @change="onClauseEdit">
                  <ElRadio value="高">高</ElRadio>
                  <ElRadio value="中">中</ElRadio>
                  <ElRadio value="低">低</ElRadio>
                </ElRadioGroup>
              </ElFormItem>
              <ElFormItem>
                <div class="flex w-full items-center justify-between gap-8px">
                  <span v-if="clauseDirty" class="text-12px c-warning">有未保存的修改</span>
                  <ElButton type="primary" size="small" :disabled="!clauseDirty" @click="saveClause">保存修改</ElButton>
                </div>
              </ElFormItem>
            </ElForm>
          </template>
        </ElCard>

      </div>

      <!-- 底部：解析统计 + 证据回链 -->
      <div class="grid gap-16px lg:grid-cols-[1fr_1.4fr] shrink-0">
        <ElCard shadow="never">
          <template #header>
            <span class="font-600">解析统计</span>
          </template>
          <div class="grid grid-cols-4 gap-12px">
            <div class="flex-col-center rounded-10px bg-primary/5 py-12px">
              <span class="text-22px font-600 c-primary">{{ resultFile?.pages || 0 }}</span>
              <span class="text-12px c-secondary">页面数</span>
            </div>
            <div class="flex-col-center rounded-10px bg-success/8 py-12px">
              <span class="text-22px font-600 c-success">286</span>
              <span class="text-12px c-secondary">提取条款数</span>
            </div>
            <div class="flex-col-center rounded-10px bg-warning/8 py-12px">
              <span class="text-22px font-600 c-warning">24</span>
              <span class="text-12px c-secondary">识别表格数</span>
            </div>
            <div class="flex-col-center rounded-10px bg-danger/6 py-12px">
              <span class="text-22px font-600 c-danger">8</span>
              <span class="text-12px c-secondary">风险规则数</span>
            </div>
          </div>
          <p class="mb-0 mt-12px text-12px c-secondary">以上统计为演示样例数据，仅对演示解析文件展示。</p>
        </ElCard>

        <ElCard shadow="never" class="min-w-0">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-600">规则命中与证据回链</span>
              <ElButton link type="primary" @click="goResultDetail">查看更多</ElButton>
            </div>
          </template>
          <ElTable :data="evidenceRows" size="small" style="width: 100%">
            <ElTableColumn prop="code" label="条款" width="70" />
            <ElTableColumn prop="name" label="识别要求" min-width="140" show-overflow-tooltip />
            <ElTableColumn prop="page" label="来源页码" width="90">
              <template #default="{ row }">第 {{ row.page }} 页</template>
            </ElTableColumn>
            <ElTableColumn prop="category" label="分类" width="120" />
            <ElTableColumn prop="confidence" label="置信度" width="80" />
            <ElTableColumn label="操作" width="70">
              <template #default="{ row }">
                <ElButton
                  link
                  type="primary"
                  size="small"
                  @click="() => { page = row.page; const c = clauses.find(x => x.id === row.id); if (c) activeCategory = c.category; }"
                >
                  查看
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElCard>
      </div>


    </template>
  </div>
</template>
