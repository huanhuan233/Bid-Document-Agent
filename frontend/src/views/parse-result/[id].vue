<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useRouterPush } from '@/hooks/common/router';
import { useBidData } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import { annotationTypeLabelMap, clauseCategoryLabelMap, parseStatusMap, sourceKindLabelMap } from '@/constants/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import DocPreview from '@/components/business/doc-preview.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'BidParseResultDetail' });

const route = useRoute();
const { routerPush } = useRouterPush();
const fileId = String(route.params.id);

const { data: files } = useBidData(() => bidProvider.getParseFiles('P-2026-001'), [] as Bid.ParseFileVM[]);
const file = computed(() => files.value.find(f => f.id === fileId));

const { data: clauses } = useBidData(() => bidProvider.getClauses(fileId), [] as Bid.ClauseVM[]);
const { data: annotations } = useBidData(() => bidProvider.getAnnotations(fileId), []);
const { data: typography } = useBidData(() => bidProvider.getTypography(fileId), null);
const { data: layout } = useBidData(() => bidProvider.getLayout(fileId), null);

const fontKeyLabelMap: Record<string, string> = {
  zhFont: '中文字体',
  enFont: '英文字体',
  h1Size: '一级标题字号',
  h2Size: '二级标题字号',
  bodySize: '正文字号',
  bold: '粗体',
  italic: '斜体',
  align: '对齐方式',
  lineHeight: '行距',
  spaceBefore: '段前',
  spaceAfter: '段后',
  firstLineIndent: '首行缩进'
};

function fontKeyLabel(k: string) {
  return fontKeyLabelMap[k] || k;
}

/** 左：可搜索目录/条款树 */
const keyword = ref('');
const activeClauseId = ref<string | null>(null);

const clauseTree = computed(() => {
  const groups: Record<string, Bid.ClauseVM[]> = {};
  clauses.value
    .filter(c => (keyword.value ? c.name.includes(keyword.value) || c.content.includes(keyword.value) || c.code.includes(keyword.value) : true))
    .forEach(c => {
      groups[c.category] = groups[c.category] || [];
      groups[c.category].push(c);
    });
  return Object.entries(groups).map(([cat, list]) => ({
    key: cat,
    label: clauseCategoryLabelMap[cat as Bid.ClauseCategory],
    children: list.map(c => ({ key: c.id, label: `${c.code} ${c.name}` }))
  }));
});

function selectNode(key: string) {
  const clause = clauses.value.find(c => c.id === key);
  if (!clause) return;
  activeClauseId.value = clause.id;
  page.value = clause.page;
}

const centerTab = ref('preview');
const rightTab = ref('clause');
const page = ref(12);
const scale = ref(0.75);
const showAnnotations = ref(true);

function goBack() {
  routerPush({ key: 'bid-parse', query: { projectId: 'P-2026-001' } });
}

function reparse() {
  window.$message?.info('重新解析依赖解析引擎：核心能力暂未接入');
}

function exportResult() {
  const payload = { fileId, exportedAt: new Date().toISOString(), note: '演示数据导出（JSON）', clauses: clauses.value };
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `parse-result-${fileId}-demo.json`;
  a.click();
  URL.revokeObjectURL(url);
  window.$message?.success('已导出演示数据 JSON');
}

function onAnnotationSelect(a: Bid.DocAnnotationVM | null) {
  if (a) activeClauseId.value = a.id;
}

const activeAnnotation = computed(() => annotations.value.find(a => a.id === activeClauseId.value) || null);
const activeClause = computed(() => clauses.value.find(c => c.id === activeClauseId.value) || null);
</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px">
    <ElCard shadow="never">
      <div class="flex flex-wrap items-center gap-16px">
        <ElButton link @click="goBack"><SvgIcon icon="mdi:arrow-left" class="mr-4px" /> 返回</ElButton>
        <div class="flex min-w-0 items-center gap-10px">
          <SvgIcon icon="mdi:file-pdf-box" class="shrink-0 text-34px c-danger" />
          <div class="min-w-0">
            <div class="truncate text-16px font-600">{{ file?.name || fileId }}</div>
            <div class="text-12px c-secondary">{{ file?.uploadedAt }} · {{ file?.sizeLabel }} · 演示样例文件</div>
          </div>
        </div>
        <div class="flex flex-wrap items-center gap-24px">
          <div>
            <div class="text-12px c-secondary">解析状态</div>
            <ElTag size="small" :type="file ? parseStatusMap[file.status].type : 'info'">
              {{ file ? parseStatusMap[file.status].label : '未知' }}
            </ElTag>
          </div>
          <div><div class="text-12px c-secondary">总页数</div><b class="text-16px">{{ file?.pages || '—' }}</b> 页</div>
          <div><div class="text-12px c-secondary">条款数量</div><b class="text-16px">286</b> 条</div>
          <div><div class="text-12px c-secondary">表格数量</div><b class="text-16px">24</b> 个</div>
          <div><div class="text-12px c-secondary">识别耗时</div><b class="text-16px">{{ file?.durationLabel || '—' }}</b></div>
        </div>
        <div class="ml-auto flex items-center gap-10px">
          <DemoBadge />
          <ElButton icon="mdi:refresh" @click="reparse">重新解析</ElButton>
          <ElButton type="primary" icon="mdi:download" @click="exportResult">导出结果</ElButton>
        </div>
      </div>
    </ElCard>

    <div class="grid gap-16px xl:grid-cols-[300px_1fr_340px]">
      <ElCard shadow="never" class="min-w-0">
        <template #header><span class="font-600">目录导航 / 条款树</span></template>
        <ElInput v-model="keyword" placeholder="搜索章节或条款名称" clearable class="mb-10px">
          <template #prefix><SvgIcon icon="mdi:magnify" /></template>
        </ElInput>
        <div class="overflow-auto max-h-600px">
          <div v-for="group in clauseTree" :key="group.key" class="mb-8px">
            <div class="rounded-6px bg-gray-1 px-8px py-6px text-13px font-600">
              {{ group.label }}
              <ElTag class="ml-6px" size="small" round>{{ group.children.length }}</ElTag>
            </div>
            <div
              v-for="child in group.children"
              :key="child.key"
              class="cursor-pointer truncate rounded-6px px-14px py-6px text-13px hover:bg-primary/5"
              :class="activeClauseId === child.key ? 'bg-primary/10 c-primary font-500' : ''"
              @click="selectNode(child.key)"
            >
              {{ child.label }}
            </div>
          </div>
          <ElEmpty v-if="!clauseTree.length" description="未找到匹配条款" />
        </div>
      </ElCard>


      <ElCard shadow="never" class="min-w-0">
        <ElTabs v-model="centerTab">
          <ElTabPane label="原文预览" name="preview" />
          <ElTabPane label="OCR结果" name="ocr" />
          <ElTabPane label="版式结构" name="structure" />
          <ElTabPane label="字体样式" name="font" />
          <ElTabPane label="页面布局" name="layout" />
          <ElTabPane label="结构化图谱" name="graph" />
        </ElTabs>

        <template v-if="centerTab === 'preview'">
          <div class="mb-8px flex flex-wrap items-center justify-end gap-8px">
            <ElInputNumber v-model="page" :min="1" :max="file?.pages || 152" size="small" controls-position="right" class="w-100px" />
            <ElButton size="small" @click="scale = Math.max(0.4, +(scale - 0.1).toFixed(2))">－</ElButton>
            <span class="text-12px c-secondary">{{ Math.round(scale * 100) }}%</span>
            <ElButton size="small" @click="scale = Math.min(2, +(scale + 0.1).toFixed(2))">＋</ElButton>
            <ElSwitch v-model="showAnnotations" active-text="显示标注" size="small" />
          </div>
          <DocPreview
            :page="page"
            :annotations="page === 12 ? annotations : []"
            :scale="scale"
            :show-annotations="showAnnotations"
            :active-id="activeClauseId"
            show-margin-guides
            @select="onAnnotationSelect"
          />
          <p class="mb-0 mt-8px text-12px c-secondary">
            标注绑定文档页容器与同一缩放因子，缩放与适应宽度后标注不偏移；点击标注可在右侧查看属性。
          </p>
        </template>

        <div v-else-if="centerTab === 'ocr'" class="py-16px text-14px leading-26px">
          <p class="font-600">第二章 投标人资格要求（OCR 识别结果 · 演示样例）</p>
          <p>2.1 投标人应具备独立法人资格，具有有效的营业执照。</p>
          <p>2.2 投标人应具备计算机信息系统集成二级及以上资质，财务状况良好。</p>
          <p>2.3 近三年内（2024年1月1日至2026年12月31日）具有类似项目业绩，并提供合同复印件及验收证明。</p>
          <p>2.4 本项目不接受联合体投标。</p>
          <p class="text-12px c-secondary">OCR 算法核心能力暂未接入；本页内容为演示样例，非真实 OCR 结果。</p>
        </div>

        <div v-else-if="centerTab === 'structure'" class="py-16px">
          <ElTable :data="annotations" style="width: 100%">
            <ElTableColumn label="结构类型" width="130">
              <template #default="{ row }">{{ annotationTypeLabelMap[row.type as keyof typeof annotationTypeLabelMap] }}</template>
            </ElTableColumn>
            <ElTableColumn prop="label" label="标注" width="120" />
            <ElTableColumn prop="page" label="页码" width="80" />
            <ElTableColumn label="位置（页面百分比）">
              <template #default="{ row }">x={{ row.x }}%, y={{ row.y }}%, w={{ row.w }}%, h={{ row.h }}%</template>
            </ElTableColumn>
          </ElTable>
        </div>

        <div v-else-if="centerTab === 'font'" class="py-16px">
          <ElAlert
            type="info"
            :closable="false"
            title="当前展示“原文观察到的格式”；招标文件要求投标文件使用的格式见右侧“版式与样式分析”，两者不可混淆。"
          />
          <ElDescriptions v-if="typography" class="mt-12px" :column="2" border title="字体样式（观察值）">
            <ElDescriptionsItem v-for="(v, k) in typography.observed" :key="k" :label="fontKeyLabel(k as string)">{{ v }}</ElDescriptionsItem>
          </ElDescriptions>
        </div>

        <div v-else-if="centerTab === 'layout'" class="py-16px">
          <ElDescriptions v-if="layout" :column="2" border title="页面布局（观察值）">
            <ElDescriptionsItem label="纸张尺寸">{{ layout.observed.paper }}</ElDescriptionsItem>
            <ElDescriptionsItem label="方向">{{ layout.observed.orientation }}</ElDescriptionsItem>
            <ElDescriptionsItem label="页边距">
              上 {{ layout.observed.marginTop }} / 下 {{ layout.observed.marginBottom }} /
              左 {{ layout.observed.marginLeft }} / 右 {{ layout.observed.marginRight }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="栏数">{{ layout.observed.columns }}</ElDescriptionsItem>
            <ElDescriptionsItem label="页眉">{{ layout.observed.header }}</ElDescriptionsItem>
            <ElDescriptionsItem label="页脚">{{ layout.observed.footer }}（页码位置：{{ layout.observed.pageNumPos }}）</ElDescriptionsItem>
          </ElDescriptions>
        </div>

        <div v-else-if="centerTab === 'graph'" class="flex-col-center py-24px">
          <SvgIcon icon="mdi:graph-outline" class="text-48px c-secondary" />
          <p class="m-0 mt-8px text-14px c-secondary">结构化图谱仅展示给定样例数据；抽取、检索与图数据库核心能力暂未接入</p>
          <p class="m-0 text-12px c-secondary">原子单元仅展示外部 ID / 父级关系 / 来源引用，不在前端切分</p>
        </div>
      </ElCard>


      <ElCard shadow="never" class="min-w-0">
        <ElTabs v-model="rightTab">
          <ElTabPane label="条款详情" name="clause" />
          <ElTabPane label="版式与样式分析" name="style" />
        </ElTabs>

        <template v-if="rightTab === 'clause'">
          <template v-if="activeClause">
            <div class="mb-8px flex flex-wrap items-center gap-6px">
              <ElTag effect="plain">{{ activeClause.code }}</ElTag>
              <ElTag size="small" :type="activeClause.importance === '高' ? 'danger' : activeClause.importance === '中' ? 'warning' : 'info'">
                {{ activeClause.importance }}
              </ElTag>
              <ElTag size="small" type="info">{{ sourceKindLabelMap[activeClause.sourceKind] }}</ElTag>
              <span v-if="activeClause.confirmed" class="text-12px c-success">已确认</span>
            </div>
            <ElDescriptions :column="1" border size="small">
              <ElDescriptionsItem label="条款名称">{{ activeClause.name }}</ElDescriptionsItem>
              <ElDescriptionsItem label="条款内容">{{ activeClause.content }}</ElDescriptionsItem>
              <ElDescriptionsItem label="来源页码">第 {{ activeClause.page }} 页</ElDescriptionsItem>
              <ElDescriptionsItem label="置信度">{{ activeClause.confidence }}（模型参考值，非抽取准确率）</ElDescriptionsItem>
            </ElDescriptions>
            <ElAlert
              v-if="activeAnnotation"
              class="mt-10px"
              type="info"
              :closable="false"
              :title="`已联动标注：${activeAnnotation.label}（第 ${activeAnnotation.page} 页）`"
            />
          </template>
          <ElEmpty v-else description="在左侧选择条款查看详情" />
        </template>

        <template v-else>
          <ElCard v-if="typography" shadow="never" class="mb-12px">
            <template #header>
              <div class="flex items-center justify-between">
                <span class="text-14px font-600">字体样式分析</span>
                <ElTag size="small" type="warning" effect="plain">观察值</ElTag>
              </div>
            </template>
            <ElDescriptions :column="2" size="small" border>
              <ElDescriptionsItem v-for="(v, k) in typography.observed" :key="k" :label="fontKeyLabel(k as string)">{{ v }}</ElDescriptionsItem>
            </ElDescriptions>
          </ElCard>

          <ElCard v-if="layout" shadow="never">
            <template #header>
              <div class="flex items-center justify-between">
                <span class="text-14px font-600">页面布局分析</span>
                <ElTag size="small" type="warning" effect="plain">观察值</ElTag>
              </div>
            </template>
            <ElDescriptions :column="1" size="small" border>
              <ElDescriptionsItem label="纸张尺寸">{{ layout.observed.paper }} · {{ layout.observed.orientation }}</ElDescriptionsItem>
              <ElDescriptionsItem label="页边距">
                上 {{ layout.observed.marginTop }} / 下 {{ layout.observed.marginBottom }} /
                左 {{ layout.observed.marginLeft }} / 右 {{ layout.observed.marginRight }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="栏数">{{ layout.observed.columns }}</ElDescriptionsItem>
              <ElDescriptionsItem label="页眉 / 页脚">{{ layout.observed.header }}；{{ layout.observed.footer }}，页码{{ layout.observed.pageNumPos }}</ElDescriptionsItem>
            </ElDescriptions>

            <div class="mt-10px text-13px font-600">版式结构统计</div>
            <div class="mt-6px grid grid-cols-3 gap-8px">
              <div class="rounded-8px bg-primary/5 py-6px text-center"><b class="c-primary">{{ layout.observed.counts.h1 }}</b><div class="text-12px c-secondary">标题 H1</div></div>
              <div class="rounded-8px bg-success/8 py-6px text-center"><b class="c-success">{{ layout.observed.counts.h2 }}</b><div class="text-12px c-secondary">标题 H2</div></div>
              <div class="rounded-8px bg-warning/8 py-6px text-center"><b class="c-warning">{{ layout.observed.counts.paragraph }}</b><div class="text-12px c-secondary">段落</div></div>
              <div class="rounded-8px bg-danger/6 py-6px text-center"><b class="c-danger">{{ layout.observed.counts.table }}</b><div class="text-12px c-secondary">表格</div></div>
              <div class="rounded-8px bg-primary/5 py-6px text-center"><b class="c-primary">{{ layout.observed.counts.image }}</b><div class="text-12px c-secondary">图片</div></div>
              <div class="rounded-8px bg-success/8 py-6px text-center"><b class="c-success">{{ layout.observed.counts.list }}</b><div class="text-12px c-secondary">列表</div></div>
            </div>

            <ElDivider />
            <div class="text-13px font-600">招标要求规定的格式</div>
            <ElDescriptions class="mt-6px" :column="1" size="small" border>
              <ElDescriptionsItem label="要求字体">{{ typography?.required.zhFont }} / {{ typography?.required.enFont }}</ElDescriptionsItem>
              <ElDescriptionsItem label="要求字号">标题 {{ typography?.required.h1Size }}；正文 {{ typography?.required.bodySize }}</ElDescriptionsItem>
              <ElDescriptionsItem label="要求页边距">
                上 {{ layout.required.marginTop }} / 下 {{ layout.required.marginBottom }} /
                左 {{ layout.required.marginLeft }} / 右 {{ layout.required.marginRight }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="页眉要求">{{ layout.required.header }}</ElDescriptionsItem>
            </ElDescriptions>
            <p class="mb-0 mt-6px text-12px c-secondary">
              以上为招标文件要求投标文件使用的格式（requirement 来源）；与上方“观察值”（observed 来源）区分展示，数据模型以 sourceKind 字段标识。
            </p>
          </ElCard>
        </template>
      </ElCard>

    </div>

    <ElCard shadow="never">
      <template #header>
        <div class="flex flex-wrap items-center justify-between gap-8px">
          <span class="font-600">条款确认与校验</span>
          <span class="text-12px c-secondary">演示样例数据 · 抽取准确率未提供（模型置信度仅供参考）</span>
        </div>
      </template>
      <div class="mb-10px flex flex-wrap gap-24px text-14px">
        <span>已确认：<b class="c-success">{{ clauses.filter(c => c.confirmed).length }}</b></span>
        <span>待确认：<b class="c-warning">{{ clauses.filter(c => !c.confirmed).length }}</b></span>
        <span>冲突：<b class="c-danger">2</b></span>
        <span>未分类：<b>0</b></span>
      </div>
      <ElTable :data="clauses" size="small" style="width: 100%">
        <ElTableColumn prop="code" label="条款编号" width="90" />
        <ElTableColumn prop="content" label="原文片段" min-width="260" show-overflow-tooltip />
        <ElTableColumn prop="name" label="抽取结果" min-width="150" show-overflow-tooltip />
        <ElTableColumn label="校验状态" width="110">
          <template #default="{ row }">
            <ElTag v-if="row.confirmed" type="success" size="small">通过</ElTag>
            <ElTag v-else type="warning" size="small">待确认</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="120">
          <template #default="{ row }">
            <ElButton link type="primary" size="small" @click="() => { selectNode(row.id); centerTab = 'preview'; }">查看</ElButton>
            <ElButton link size="small" @click="() => { rightTab = 'clause'; activeClauseId = row.id; }">编辑</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

  </div>
</template>
