<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useRouterPush } from '@/hooks/common/router';
import { useBidData, useEcharts } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import { conclusionTagMap, opinionStatusTagMap, riskTagMap } from '@/constants/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import EvidenceDrawer from '@/components/business/evidence-drawer.vue';
import NotIntegrated from '@/components/business/not-integrated.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'ReviewReportDetail' });

const route = useRoute();
const { routerPush } = useRouterPush();
const reportId = String(route.params.id);

const { data: report, state: reportState, message: reportMessage } = useBidData(() => bidProvider.getReport(reportId), null);
const { data: issues } = useBidData(() => bidProvider.getIssues(reportId), [] as Bid.ReviewIssueVM[]);
const { data: consistency } = useBidData(() => bidProvider.getConsistency(reportId), [] as Bid.ConsistencyItemVM[]);

const activeTab = ref('overview');
const issueRiskFilter = ref<'' | 'high' | 'medium' | 'low'>('');
const issueKeyword = ref('');

const filteredIssues = computed(() =>
  issues.value.filter(i => {
    if (issueRiskFilter.value && i.risk !== issueRiskFilter.value) return false;
    if (issueKeyword.value && !i.requirement.includes(issueKeyword.value) && !i.category.includes(issueKeyword.value)) return false;
    return true;
  })
);

const activeIssue = computed(() => filteredIssues.value[0] || null);

function filterByRisk(risk: '' | 'high' | 'medium' | 'low') {
  issueRiskFilter.value = risk;
  activeTab.value = 'issues';
}

function goRevise() {
  if (!report.value) return;
  routerPush({
    key: 'bid-generate',
    query: {
      projectId: report.value.projectId,
      reviewReportId: report.value.id,
      issueIds: (activeIssue.value ? [activeIssue.value.id] : []).join(','),
      mode: 'revision'
    }
  });
}

function goBack() {
  routerPush({ key: 'reports' });
}

async function shareLink() {
  const url = `${location.origin}/report-detail/${reportId}`;
  try {
    await navigator.clipboard.writeText(url);
    window.$message?.success('已复制站内详情链接（需登录且有权限才可访问）');
  } catch {
    window.$message?.info(`详情链接：${url}`);
  }
}

function downloadReport() {
  window.$message?.warning('导出 PDF/Word 依赖文档服务：暂未接入，未生成文件');
}

/** 证据抽屉 */
const evidenceVisible = ref(false);
const currentEvidence = ref<Bid.EvidenceRefVM | null>(null);
function openEvidence(i: Bid.ReviewIssueVM) {
  if (!i.evidence) {
    window.$message?.info('该问题未找到相关证据');
    return;
  }
  currentEvidence.value = i.evidence;
  evidenceVisible.value = true;
}

/** 图表（与摘要卡同一份数据计算） */
const { el: distEl, render: renderDist, resize: resizeDist } = useEcharts();
const { el: priorityEl, render: renderPriority, resize: resizePriority } = useEcharts();

function renderCharts() {
  if (!report.value) return;
  renderDist({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '44%'],
        label: { formatter: '{b}: {c}' },
        data: [
          { name: '高风险', value: report.value.high, itemStyle: { color: '#f56c6c' } },
          { name: '中风险', value: report.value.medium, itemStyle: { color: '#e6a23c' } },
          { name: '低风险', value: report.value.low, itemStyle: { color: '#67c23a' } }
        ]
      }
    ]
  });
  renderPriority({
    tooltip: { trigger: 'axis' },
    grid: { left: 90, right: 30, top: 8, bottom: 24 },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: ['低优先级(低风险)', '中优先级(中风险)', '高优先级(高风险)'],
      axisLabel: { fontSize: 11 }
    },
    series: [
      {
        type: 'bar',
        data: [
          { value: report.value.low, itemStyle: { color: '#67c23a' } },
          { value: report.value.medium, itemStyle: { color: '#e6a23c' } },
          { value: report.value.high, itemStyle: { color: '#f56c6c' } }
        ],
        barWidth: 16
      }
    ]
  });
}

let resizeHandler: (() => void) | null = null;
onMounted(() => {
  const timer = window.setInterval(() => {
    if (report.value) {
      renderCharts();
      window.clearInterval(timer);
    }
  }, 100);
  resizeHandler = () => {
    resizeDist();
    resizePriority();
  };
  window.addEventListener('resize', resizeHandler);
});
onUnmounted(() => {
  if (resizeHandler) window.removeEventListener('resize', resizeHandler);
});
</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px">
    <NotIntegrated v-if="reportState === 'not-integrated'" :message="reportMessage || '该报告不可用'" action-text="返回列表" @action="goBack" />

    <template v-else-if="report">
      <ElCard shadow="never">
        <div class="flex flex-wrap items-center gap-16px">
          <div class="min-w-0">
            <h2 class="m-0 text-22px font-600 c-text">审查报告详情</h2>
            <p class="mb-0 mt-4px text-12px c-secondary">{{ report.id }} · 汇总展示投标文件审查结果、风险分级与整改建议</p>
          </div>
          <div class="ml-auto flex flex-wrap items-center gap-10px">
            <ElButton @click="goBack"><SvgIcon icon="mdi:arrow-left" class="mr-4px" /> 返回</ElButton>
            <ElButton icon="mdi:download" @click="downloadReport">导出PDF</ElButton>
            <ElButton icon="mdi:file-word-box" @click="downloadReport">导出Word</ElButton>
            <ElButton icon="mdi:share-variant-outline" @click="shareLink">分享报告</ElButton>
            <DemoBadge />
          </div>
        </div>
        <div class="mt-12px flex flex-wrap items-center gap-x-32px gap-y-8px">
          <div><span class="text-13px c-secondary">项目名称：</span><b>{{ report.projectName }}</b></div>
          <div><span class="text-13px c-secondary">招标要求：</span>{{ report.requirementVersionLabel }}</div>
          <div><span class="text-13px c-secondary">投标文件：</span>{{ report.docVersionLabel }}</div>
          <div><span class="text-13px c-secondary">审查时间：</span>{{ report.reviewedAt }}</div>
          <div><span class="text-13px c-secondary">审查人：</span>{{ report.reviewer }}</div>
          <div class="flex items-center gap-6px">
            <span class="text-13px c-secondary">审查范围：</span>
            <ElTag v-for="s in report.scope" :key="s" size="small" effect="plain">{{ s }}</ElTag>
          </div>
        </div>
      </ElCard>

      <div class="grid gap-16px grid-cols-2 xl:grid-cols-6">
        <ElCard shadow="never">
          <div class="text-13px c-secondary">总问题数</div>
          <div class="text-26px font-600">{{ report.total }}</div>
          <div class="text-12px c-secondary">示例问题 {{ issues.length }} 条已展开</div>
        </ElCard>
        <ElCard shadow="never" class="cursor-pointer" :class="issueRiskFilter === 'high' ? 'ring-2 ring-danger' : ''" @click="filterByRisk('high')">
          <div class="text-13px c-secondary">高风险</div>
          <div class="text-26px font-600 c-danger">{{ report.high }}</div>
        </ElCard>
        <ElCard shadow="never" class="cursor-pointer" :class="issueRiskFilter === 'medium' ? 'ring-2 ring-warning' : ''" @click="filterByRisk('medium')">
          <div class="text-13px c-secondary">中风险</div>
          <div class="text-26px font-600 c-warning">{{ report.medium }}</div>
        </ElCard>
        <ElCard shadow="never" class="cursor-pointer" :class="issueRiskFilter === 'low' ? 'ring-2 ring-success' : ''" @click="filterByRisk('low')">
          <div class="text-13px c-secondary">低风险</div>
          <div class="text-26px font-600 c-success">{{ report.low }}</div>
        </ElCard>
        <ElCard shadow="never">
          <div class="text-13px c-secondary">评分点覆盖率</div>
          <div class="text-26px font-600">{{ report.coverageRate ?? '未提供' }}<span v-if="report.coverageRate !== null" class="text-14px">%</span></div>
          <div class="text-12px c-warning">{{ report.coverageRate !== null ? '演示值' : '' }}</div>
        </ElCard>
        <ElCard shadow="never">
          <div class="text-13px c-secondary">一致性通过率</div>
          <div class="text-26px font-600">{{ report.consistencyRate ?? '未提供' }}<span v-if="report.consistencyRate !== null" class="text-14px">%</span></div>
        </ElCard>
      </div>

      <ElCard shadow="never">
        <ElTabs v-model="activeTab">
          <ElTabPane label="总览" name="overview" />
          <ElTabPane label="问题清单" name="issues" />
          <ElTabPane label="评分点覆盖" name="coverage" />
          <ElTabPane label="一致性校验" name="consistency" />
          <ElTabPane label="证据回链" name="evidence" />
        </ElTabs>

        <template v-if="activeTab === 'overview'">
          <div class="grid gap-16px lg:grid-cols-[1fr_1fr_1.2fr]">
            <div>
              <div class="mb-6px text-14px font-600">问题分布统计</div>
              <div ref="distEl" class="h-260px w-full" />
            </div>
            <div>
              <div class="mb-6px text-14px font-600">整改优先级</div>
              <div ref="priorityEl" class="h-260px w-full" />
            </div>
            <div class="min-w-0">
              <div class="mb-6px text-14px font-600">问题类别统计</div>
              <ElTable :data="report.categories" size="small" max-height="260" style="width: 100%">
                <ElTableColumn type="index" label="#" width="44" />
                <ElTableColumn prop="name" label="问题类型" min-width="120" />
                <ElTableColumn prop="count" label="数量" width="60" />
                <ElTableColumn label="占比" width="90">
                  <template #default="{ row }">
                    <ElProgress :percentage="row.percent" :stroke-width="8" />
                  </template>
                </ElTableColumn>
              </ElTable>
            </div>
          </div>
          <div class="mt-14px grid gap-16px lg:grid-cols-2">
            <ElCard shadow="never">
              <template #header><span class="font-600">报告摘要</span></template>
              <p class="m-0 text-14px leading-24px">{{ report.digest }}</p>
            </ElCard>
            <ElCard shadow="never">
              <template #header>
                <div class="flex items-center justify-between">
                  <span class="font-600">整改建议汇总</span>
                  <ElButton link type="primary" @click="goRevise">去整改生成</ElButton>
                </div>
              </template>
              <div class="flex flex-col gap-8px">
                <div v-for="(a, i) in report.advices" :key="a.key" class="flex items-start gap-8px">
                  <ElTag size="small" round :type="a.priority === 'high' ? 'danger' : a.priority === 'medium' ? 'warning' : 'success'">{{ i + 1 }}</ElTag>
                  <div class="min-w-0">
                    <div class="text-14px font-500">{{ a.title }}</div>
                    <div class="text-12px c-secondary">{{ a.desc }}</div>
                  </div>
                </div>
              </div>
            </ElCard>
          </div>
        </template>

        <template v-else-if="activeTab === 'issues'">
          <div class="mb-10px flex flex-wrap items-center gap-10px">
            <ElInput v-model="issueKeyword" placeholder="搜索问题 / 类别关键词" clearable class="w-240px" size="small" />
            <ElSelect v-model="issueRiskFilter" placeholder="全部风险" size="small" class="w-130px" clearable>
              <ElOption label="高风险" value="high" />
              <ElOption label="中风险" value="medium" />
              <ElOption label="低风险" value="low" />
            </ElSelect>
            <span class="text-12px c-secondary">共 {{ filteredIssues.length }} 条（示例问题清单）</span>
          </div>
          <ElTable :data="filteredIssues" size="small" style="width: 100%">
            <ElTableColumn type="index" label="#" width="46" />
            <ElTableColumn prop="category" label="问题类型" width="120" />
            <ElTableColumn prop="requirement" label="对应要求" min-width="170" show-overflow-tooltip />
            <ElTableColumn prop="response" label="当前响应" min-width="150" show-overflow-tooltip />
            <ElTableColumn prop="pages" label="出处章节" width="90" />
            <ElTableColumn label="风险等级" width="90">
              <template #default="{ row }">
                <ElTag size="small" :type="riskTagMap[row.risk as keyof typeof riskTagMap].type">
                  {{ riskTagMap[row.risk as keyof typeof riskTagMap].label }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="relatedChapter" label="整改建议章节" width="110" />
            <ElTableColumn label="整改状态" width="100">
              <template #default="{ row }">
                <ElTag size="small" :type="opinionStatusTagMap[row.opinionStatus]?.type">{{ row.opinionStatus }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="110" fixed="right">
              <template #default="{ row }">
                <ElButton link type="primary" size="small" @click="openEvidence(row)">查看</ElButton>
                <ElButton link size="small" @click="goRevise">去整改</ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </template>



        <div v-else-if="activeTab === 'coverage'" class="py-16px">
          <div class="mb-10px flex flex-wrap items-center gap-12px">
            <span class="text-14px">评分点覆盖率：</span>
            <ElProgress class="w-300px" :percentage="report.coverageRate ?? 0" />
            <ElTag v-if="report.coverageRate !== null" size="small" type="warning" effect="plain">演示值，非真实接口指标</ElTag>
            <span v-else class="text-13px c-secondary">未提供</span>
          </div>
          <ElTable :data="issues.filter(i => i.category.includes('关键技术指标') || i.category.includes('技术方案'))" size="small" style="width: 100%">
            <ElTableColumn prop="requirement" label="评分点 / 要求" min-width="220" show-overflow-tooltip />
            <ElTableColumn prop="response" label="当前响应" min-width="200" show-overflow-tooltip />
            <ElTableColumn label="结论" width="100">
              <template #default="{ row }">
                <ElTag size="small" :type="conclusionTagMap[row.conclusion]?.type">{{ row.conclusion }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="relatedChapter" label="相关章节" width="110" />
          </ElTable>
        </div>

        <div v-else-if="activeTab === 'consistency'" class="py-16px">
          <ElTable :data="consistency" size="small" style="width: 100%">
            <ElTableColumn prop="field" label="校验项" width="110" />
            <ElTableColumn prop="tenderValue" label="招标文件" min-width="150" show-overflow-tooltip />
            <ElTableColumn prop="bidValue" label="投标文件" min-width="150" show-overflow-tooltip />
            <ElTableColumn label="校验结果" width="100">
              <template #default="{ row }">
                <ElTag :type="row.result === '一致' ? 'success' : 'danger'" size="small">{{ row.result }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="desc" label="说明" min-width="140" show-overflow-tooltip />
          </ElTable>
        </div>

        <div v-else-if="activeTab === 'evidence'" class="py-16px">
          <ElTable :data="issues.filter(i => i.evidence)" size="small" style="width: 100%">
            <ElTableColumn prop="requirement" label="对应要求" min-width="180" show-overflow-tooltip />
            <ElTableColumn label="匹配证据内容" min-width="240" show-overflow-tooltip>
              <template #default="{ row }">{{ row.evidence?.quote }}</template>
            </ElTableColumn>
            <ElTableColumn label="来源" width="150" show-overflow-tooltip>
              <template #default="{ row }">{{ row.evidence?.fileLabel }} {{ row.evidence?.version }}</template>
            </ElTableColumn>
            <ElTableColumn label="页码" width="70">
              <template #default="{ row }">{{ row.evidence?.page ?? '—' }}</template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="80">
              <template #default="{ row }">
                <ElButton link type="primary" size="small" @click="openEvidence(row)">查看</ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </div>

      </ElCard>
    </template>
  </div>

  <EvidenceDrawer v-model:visible="evidenceVisible" :evidence="currentEvidence" />
</template>
