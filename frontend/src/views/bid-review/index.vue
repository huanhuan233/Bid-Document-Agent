<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useRouterPush } from '@/hooks/common/router';
import { useBidData } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import { conclusionTagMap, riskTagMap } from '@/constants/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import StepBar from '@/components/business/step-bar.vue';
import EvidenceDrawer from '@/components/business/evidence-drawer.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'BidReviewCenter' });

const route = useRoute();
const { routerPush } = useRouterPush();

const { data: projects } = useBidData(() => bidProvider.getProjects(), [] as Bid.ProjectVM[]);
const projectId = ref((route.query.projectId as string) || 'P-2026-001');

/**
 * 审查依据 =「已解析招标要求版本」下拉框（不提供重新上传招标文件入口）。
 * 投标文件仍可上传或选择已有版本。
 */
const requirementVersionId = ref('');
const docVersionId = ref('');
const scope = ref<string[]>(['资格审查', '技术响应', '商务响应', '全量审查']);
const scopeOptions = ['资格审查', '技术响应', '商务响应', '价格审查', '全量审查'];

const { data: requirementVersions } = useBidData(() => bidProvider.getRequirementVersions(projectId.value), []);
const { data: docVersions } = useBidData(() => bidProvider.getBidDocVersions(projectId.value), []);

const stepActive = ref(2);
const steps = [
  { title: '选择审查依据', desc: '选择已解析的招标要求版本' },
  { title: '准备投标文件', desc: '上传或选择投标文件版本' },
  { title: '审查处理', desc: '逐项比对，识别问题' },
  { title: '查看结果', desc: '响应矩阵与问题详情' },
  { title: '整改建议', desc: '生成整改意见与审查报告' }
];

const reviewing = ref(false);

function startReview() {
  if (!requirementVersionId.value) {
    window.$message?.warning('请先在下拉框中选择“审查依据（已解析招标要求版本）”');
    return;
  }
  if (!docVersionId.value) {
    window.$message?.warning('请选择待审查的投标文件版本');
    return;
  }
  reviewing.value = true;
  window.$message?.info('审查为核心能力：暂未接入。下方展示演示样例审查结果（RR-2026-005）');
  setTimeout(() => {
    reviewing.value = false;
    stepActive.value = 4;
  }, 1200);
}

/** 演示结果（绑定 RR-2026-005） */
const { data: issues } = useBidData(() => bidProvider.getIssues('RR-2026-005'), [] as Bid.ReviewIssueVM[]);
const { data: consistency } = useBidData(() => bidProvider.getConsistency('RR-2026-005'), [] as Bid.ConsistencyItemVM[]);

/** 筛选 */
const filters = ref({ keyword: '', category: '全部问题', risk: '', conclusion: '' });
const categoryCounts = computed(() => {
  const map: Record<string, number> = { 全部问题: issues.value.length };
  issues.value.forEach(i => {
    map[i.category] = (map[i.category] || 0) + 1;
  });
  return map;
});

const filteredIssues = computed(() =>
  issues.value.filter(i => {
    const f = filters.value;
    if (f.keyword && !i.requirement.includes(f.keyword) && !i.response.includes(f.keyword)) return false;
    if (f.category !== '全部问题' && i.category !== f.category) return false;
    if (f.risk && i.risk !== f.risk) return false;
    if (f.conclusion && i.conclusion !== f.conclusion) return false;
    return true;
  })
);

const activeIssueId = ref('');
const activeIssue = computed(() => issues.value.find(i => i.id === activeIssueId.value) || filteredIssues.value[0] || null);

function selectIssue(i: Bid.ReviewIssueVM) {
  activeIssueId.value = i.id;
}

function prevIssue() {
  const idx = filteredIssues.value.findIndex(i => i.id === activeIssue?.value?.id);
  if (idx > 0) activeIssueId.value = filteredIssues.value[idx - 1].id;
}

function nextIssue() {
  const idx = filteredIssues.value.findIndex(i => i.id === activeIssue?.value?.id);
  if (idx >= 0 && idx < filteredIssues.value.length - 1) activeIssueId.value = filteredIssues.value[idx + 1].id;
}

function resetFilters() {
  filters.value = { keyword: '', category: '全部问题', risk: '', conclusion: '' };
}

function onDocUpload(file: File) {
  window.$message?.info(`演示环境：已选择「${file.name}」，上传文件不会触发真实审查`);
  return false;
}

/** 证据 */
const evidenceVisible = ref(false);
function openEvidence(i: Bid.ReviewIssueVM | null) {
  if (!i) return;
  if (!i.evidence) {
    window.$message?.info('未找到相关证据，无法定位页码');
    return;
  }
  evidenceVisible.value = true;
}

/** 选中整改意见后跳转生成中心（修订模式），携带完整上下文 */
const selectedIssueIds = ref<string[]>([]);

function goRevise() {
  const ids = selectedIssueIds.value.length ? selectedIssueIds.value : activeIssue.value ? [activeIssue.value.id] : [];
  if (!ids.length) {
    window.$message?.warning('请先选择需要整改的问题');
    return;
  }
  routerPush({
    key: 'bid-generate',
    query: {
      projectId: projectId.value,
      requirementsVersionId: requirementVersionId.value,
      documentVersionId: docVersionId.value,
      reviewReportId: 'RR-2026-005',
      issueIds: ids.join(','),
      mode: 'revision'
    }
  });
}

function goReport() {
  routerPush({ key: 'report-detail', params: { id: 'RR-2026-005' } });
}

function goRecords() {
  routerPush({ key: 'review-records' });
}

onMounted(() => {
  if (projectId.value !== 'P-2026-001') {
    window.$message?.info('演示审查结果仅覆盖项目 P-2026-001（XX市智慧城市建设项目）');
  }
});
</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px [&>*]:shrink-0">
    <div class="flex flex-wrap items-end justify-between gap-12px">
      <div class="min-w-0">
        <h1 class="m-0 text-26px font-600 c-text">标书审查中心</h1>
        <p class="mt-4px mb-0 text-13px c-secondary">对照招标要求审查投标文件，识别风险、遗漏与不一致</p>
      </div>
      <div class="flex items-center gap-12px">
        <DemoBadge />
        <ElButton @click="goRecords"><SvgIcon icon="mdi:history" class="mr-4px" /> 审查记录</ElButton>
      </div>
    </div>

    <!-- 顶部配置：审查依据为“已解析招标要求版本”下拉框 -->
    <ElCard shadow="never">
      <div class="flex flex-wrap items-center gap-12px">
        <span class="text-14px c-secondary">所属项目</span>
        <ElSelect v-model="projectId" class="w-230px">
          <ElOption v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </ElSelect>
        <span class="text-14px c-secondary">审查依据</span>
        <ElSelect v-model="requirementVersionId" placeholder="选择已解析并确认的招标要求版本" class="w-300px">
          <ElOption v-for="v in requirementVersions" :key="v.id" :label="`${v.name} ${v.version}${v.confirmed ? ' · 已确认' : ''}`" :value="v.id" />
        </ElSelect>
        <span class="text-14px c-secondary">投标文件</span>
        <ElSelect v-model="docVersionId" placeholder="选择已有版本" class="w-240px">
          <ElOption v-for="v in docVersions" :key="v.id" :label="`${v.name} ${v.version}`" :value="v.id" />
        </ElSelect>
        <ElUpload :show-file-list="false" accept=".pdf,.doc,.docx" :before-upload="onDocUpload">
          <ElButton link type="primary" icon="mdi:upload">上传新版本</ElButton>
        </ElUpload>
        <span class="text-14px c-secondary">审查范围</span>
        <ElSelect v-model="scope" multiple collapse-tags class="min-w-220px">
          <ElOption v-for="s in scopeOptions" :key="s" :label="s" :value="s" />
        </ElSelect>
        <div class="ml-auto flex items-center gap-10px">
          <ElButton type="primary" icon="mdi:play" :loading="reviewing" @click="startReview">开始审查</ElButton>
          <ElButton icon="mdi:clipboard-text" @click="goReport">生成审查报告</ElButton>
          <ElButton icon="mdi:format-list-checks" @click="goRevise">按整改意见修订</ElButton>
        </div>
      </div>
      <ElAlert
        class="mt-8px"
        type="info"
        :closable="false"
        title="审查依据来自「已解析招标要求版本」下拉框；如需新版本，请先在标书解析中心解析并确认。"
      />
    </ElCard>

    <ElCard shadow="never">
      <StepBar :steps="steps" :active="stepActive" />
    </ElCard>

    <!-- 结果摘要 -->
    <div class="grid gap-16px grid-cols-2 xl:grid-cols-4">
      <ElCard shadow="never">
        <div class="flex items-center gap-12px">
          <div class="h-44px w-44px flex items-center justify-center rounded-10px bg-danger/8"><SvgIcon icon="mdi:alert-octagon" class="text-24px c-danger" /></div>
          <div>
            <div class="text-13px c-secondary">高风险项</div>
            <div class="text-24px font-600">{{ issues.filter(i => i.risk === 'high').length }}</div>
          </div>
        </div>
      </ElCard>
      <ElCard shadow="never">
        <div class="flex items-center gap-12px">
          <div class="h-44px w-44px flex items-center justify-center rounded-10px bg-warning/8"><SvgIcon icon="mdi:alert" class="text-24px c-warning" /></div>
          <div>
            <div class="text-13px c-secondary">中风险项</div>
            <div class="text-24px font-600">{{ issues.filter(i => i.risk === 'medium').length }}</div>
          </div>
        </div>
      </ElCard>
      <ElCard shadow="never">
        <div class="flex items-center gap-12px">
          <div class="h-44px w-44px flex items-center justify-center rounded-10px bg-success/8"><SvgIcon icon="mdi:check-circle" class="text-24px c-success" /></div>
          <div>
            <div class="text-13px c-secondary">低风险项</div>
            <div class="text-24px font-600">{{ issues.filter(i => i.risk === 'low').length }}</div>
          </div>
        </div>
      </ElCard>
      <ElCard shadow="never">
        <div class="flex items-center gap-12px">
          <div class="h-44px w-44px flex items-center justify-center rounded-10px bg-primary/8"><SvgIcon icon="mdi:target" class="text-24px c-primary" /></div>
          <div>
            <div class="text-13px c-secondary">评分点覆盖率</div>
            <div class="text-24px font-600">92.5%<ElTag class="ml-6px" size="small" type="warning" effect="plain">演示值</ElTag></div>
          </div>
        </div>
      </ElCard>
    </div>

    <!-- 三栏主体 -->
    <div class="grid gap-16px xl:grid-cols-[240px_1fr_360px]">
      <!-- 左：问题分类 -->
      <ElCard shadow="never" class="min-w-0">
        <template #header><span class="font-600">问题分类 / 筛选</span></template>
        <div class="flex flex-col gap-4px">
          <div
            v-for="(count, cat) in categoryCounts"
            :key="cat"
            class="flex cursor-pointer items-center justify-between rounded-8px px-10px py-8px text-14px"
            :class="filters.category === cat ? 'bg-primary/10 c-primary font-500' : 'hover:bg-gray-1'"
            @click="filters.category = cat"
          >
            <span class="min-w-0 truncate">{{ cat }}</span>
            <ElTag size="small" round>{{ count }}</ElTag>
          </div>
        </div>
        <ElDivider />
        <div class="flex flex-col gap-8px">
          <ElSelect v-model="filters.risk" placeholder="风险等级" clearable size="small">
            <ElOption label="高风险" value="high" />
            <ElOption label="中风险" value="medium" />
            <ElOption label="低风险" value="low" />
          </ElSelect>
          <ElSelect v-model="filters.conclusion" placeholder="审查结论" clearable size="small">
            <ElOption v-for="(v, k) in conclusionTagMap" :key="k" :label="v.label" :value="k" />
          </ElSelect>
          <ElButton size="small" @click="resetFilters">重置</ElButton>
        </div>
      </ElCard>

      <!-- 中：响应矩阵 -->
      <ElCard shadow="never" class="min-w-0">
        <template #header>
          <div class="flex flex-wrap items-center justify-between gap-8px">
            <span class="font-600">响应矩阵 / 审查结果（{{ filteredIssues.length }}）</span>
            <ElInput v-model="filters.keyword" placeholder="搜索问题或关键词" clearable size="small" class="w-200px" />
          </div>
        </template>
        <ElTable :data="filteredIssues" style="width: 100%" size="small" highlight-current-row @row-click="selectIssue">
          <ElTableColumn type="index" label="序号" width="60" />
          <ElTableColumn label="招标要求" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <ElCheckbox
                :model-value="selectedIssueIds.includes(row.id)"
                @click.stop
                @change="(v: any) => { selectedIssueIds = v ? [...selectedIssueIds, row.id] : selectedIssueIds.filter(id => id !== row.id); }"
              >
                {{ row.requirement }}
              </ElCheckbox>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="response" label="投标响应" min-width="150" show-overflow-tooltip />
          <ElTableColumn prop="pages" label="页码" width="70" />
          <ElTableColumn label="审查结论" width="90">
            <template #default="{ row }">
              <ElTag size="small" :type="conclusionTagMap[row.conclusion]?.type">{{ row.conclusion }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="风险等级" width="90">
            <template #default="{ row }">
              <ElTag size="small" :type="riskTagMap[row.risk as keyof typeof riskTagMap].type">
                {{ riskTagMap[row.risk as keyof typeof riskTagMap].label }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="70">
            <template #default="{ row }">
              <ElButton link type="primary" size="small" @click.stop="selectIssue(row); openEvidence(row)">证据</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElCard>

      <!-- 右：问题详情与整改建议 -->
      <ElCard shadow="never" class="min-w-0">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-600">问题详情与整改建议</span>
            <div class="flex gap-4px">
              <ElButton size="small" @click="prevIssue">上一条</ElButton>
              <ElButton size="small" @click="nextIssue">下一条</ElButton>
            </div>
          </div>
        </template>
        <template v-if="activeIssue">
          <div class="mb-8px flex flex-wrap items-center gap-6px">
            <ElTag :type="riskTagMap[activeIssue.risk].type" size="small">{{ riskTagMap[activeIssue.risk].label }}</ElTag>
            <ElTag size="small" effect="plain">{{ activeIssue.category }}</ElTag>
            <ElTag size="small" :type="conclusionTagMap[activeIssue.conclusion]?.type">{{ activeIssue.conclusion }}</ElTag>
          </div>
          <ElDescriptions :column="1" border size="small">
            <ElDescriptionsItem label="招标要求">{{ activeIssue.requirement }}</ElDescriptionsItem>
            <ElDescriptionsItem label="投标响应">{{ activeIssue.response }}</ElDescriptionsItem>
            <ElDescriptionsItem label="证据位置">
              <template v-if="activeIssue.evidence && activeIssue.evidence.page !== null">第 {{ activeIssue.evidence.page }} 页</template>
              <template v-else>
                <ElTag size="small" type="info">未找到相关证据页码</ElTag>
              </template>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="相关章节">{{ activeIssue.relatedChapter }}</ElDescriptionsItem>
            <ElDescriptionsItem label="证据片段">
              <span v-if="activeIssue.evidence">{{ activeIssue.evidence.quote }}</span>
              <ElTag v-else size="small" type="info">未找到相关证据</ElTag>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="整改建议">{{ activeIssue.opinion }}</ElDescriptionsItem>
            <ElDescriptionsItem label="整改状态">
              <ElTag size="small" :type="activeIssue.opinionStatus === '已复核' ? 'success' : 'warning'">{{ activeIssue.opinionStatus }}</ElTag>
            </ElDescriptionsItem>
          </ElDescriptions>
          <div class="mt-10px flex gap-8px">
            <ElButton size="small" type="primary" plain @click="openEvidence(activeIssue)">查看证据</ElButton>
            <ElButton size="small" @click="goRevise">根据整改意见修订</ElButton>
          </div>
        </template>
        <ElEmpty v-else description="选择左侧问题查看详情" />
      </ElCard>

    </div>


    <!-- 底部：一致性校验 + 证据回链 -->
    <div class="grid gap-16px lg:grid-cols-2">
      <ElCard shadow="never" class="min-w-0">
        <template #header><span class="font-600">一致性校验</span></template>
        <ElTable :data="consistency" size="small" style="width: 100%">
          <ElTableColumn prop="field" label="校验项" width="100" />
          <ElTableColumn prop="tenderValue" label="招标文件" min-width="130" show-overflow-tooltip />
          <ElTableColumn prop="bidValue" label="投标文件" min-width="130" show-overflow-tooltip />
          <ElTableColumn label="校验结果" width="90">
            <template #default="{ row }">
              <ElTag :type="row.result === '一致' ? 'success' : 'danger'" size="small">{{ row.result }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="desc" label="说明" min-width="120" show-overflow-tooltip />
        </ElTable>
      </ElCard>

      <ElCard shadow="never" class="min-w-0">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-600">证据回链</span>
            <span class="text-12px c-secondary">无页码的证据不做定位推测</span>
          </div>
        </template>
        <ElTable :data="issues.filter(i => i.evidence)" size="small" style="width: 100%">
          <ElTableColumn prop="requirement" label="招标要求" min-width="180" show-overflow-tooltip />
          <ElTableColumn label="匹配证据内容" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">{{ row.evidence?.quote }}</template>
          </ElTableColumn>
          <ElTableColumn label="页码" width="70">
            <template #default="{ row }">{{ row.evidence?.page ?? '—' }}</template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="80">
            <template #default="{ row }">
              <ElButton link type="primary" size="small" @click="selectIssue(row); openEvidence(row)">定位</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElCard>
    </div>


    <EvidenceDrawer v-model:visible="evidenceVisible" :evidence="activeIssue?.evidence || null" />
  </div>
</template>
