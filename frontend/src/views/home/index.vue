<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue';
import { useRouterPush } from '@/hooks/common/router';
import { useBidData, useEcharts } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import { projectRiskMap } from '@/constants/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import StatCard from '@/components/business/stat-card.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'WorkbenchHome' });

const { routerPush } = useRouterPush();

const { data: stats } = useBidData(() => bidProvider.getWorkbenchStats(), {
  inProgressProjects: 0,
  weekNewTasks: 0,
  pendingRisks: 0,
  monthBids: 0,
  winRate: null,
  deltas: {}
});
const { data: projects, loading: projectsLoading } = useBidData(() => bidProvider.getProjects(), [] as Bid.ProjectVM[]);
const { data: todos, load: loadTodos } = useBidData(() => bidProvider.getTodos(), [] as Bid.TodoVM[]);
const { data: notices, load: loadNotices } = useBidData(() => bidProvider.getNotices(), [] as Bid.NoticeVM[]);
const { data: risks } = useBidData(() => bidProvider.getRiskAlerts(), [] as Bid.RiskAlertVM[]);
const { data: trend, load: loadTrend } = useBidData(() => bidProvider.getTrend(), [] as Bid.TrendPointVM[]);

const todayLabel = computed(() => {
  const d = new Date();
  const week = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${week[d.getDay()]}`;
});

const { el: trendEl, render: renderTrend, resize: resizeTrend } = useEcharts();

function renderTrendChart() {
  const points = trend.value;
  if (!points.length) return;
  renderTrend({
    tooltip: { trigger: 'axis' },
    legend: { data: ['解析任务', '生成任务', '审查任务'], bottom: 0 },
    grid: { left: 40, right: 16, top: 24, bottom: 40 },
    xAxis: { type: 'category', data: points.map(p => p.date) },
    yAxis: { type: 'value' },
    series: [
      { name: '解析任务', type: 'bar', data: points.map(p => p.parse), itemStyle: { color: '#409eff' } },
      { name: '生成任务', type: 'bar', data: points.map(p => p.generate), itemStyle: { color: '#67c23a' } },
      { name: '审查任务', type: 'bar', data: points.map(p => p.review), itemStyle: { color: '#9a6fe0' } }
    ]
  });
}

let resizeHandler: (() => void) | null = null;

onMounted(async () => {
  await Promise.all([loadTodos(), loadNotices(), loadTrend()]);
  renderTrendChart();
  resizeHandler = () => resizeTrend();
  window.addEventListener('resize', resizeHandler);
});

onUnmounted(() => {
  if (resizeHandler) window.removeEventListener('resize', resizeHandler);
});

function goProject(id: string) {
  routerPush({ key: 'project-detail', params: { id } });
}

function goProjectList() {
  routerPush({ key: 'projects' });
}

/** 快捷入口跳转，携带当前项目（默认取项目进度第一项） */
function goQuick(key: 'bid-parse' | 'bid-generate' | 'bid-review' | 'reports') {
  const projectId = projects.value[0]?.id;
  const query = projectId ? { projectId } : {};
  routerPush({ key, query });
}

async function handleTodoToggle(todo: Bid.TodoVM) {
  todo.done = !todo.done;
  await bidProvider.toggleTodo(todo.id);
  loadTodos();
}

async function handleReadNotice(notice: Bid.NoticeVM) {
  if (!notice.read) {
    await bidProvider.markNoticeRead(notice.id);
    loadNotices();
  }
}

/** 风险跳转：进入对应项目的审查中心 */
function goRisk(risk: Bid.RiskAlertVM) {
  routerPush({ key: 'bid-review', query: { projectId: risk.projectId } });
}

const progressStatus = (p: number) => (p >= 60 ? 'success' : p >= 30 ? 'warning' : 'info');
</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px lt-sm:p-12px">
    <!-- 标题行 -->
    <div class="flex flex-wrap items-end justify-between gap-16px">
      <div class="min-w-0">
        <h1 class="m-0 text-26px font-600 c-text">投标工作台</h1>
        <p class="mt-4px mb-0 text-13px c-secondary">集中查看项目进度、待办事项、风险预警与关键数据</p>
      </div>
      <div class="flex flex-wrap items-center gap-12px">
        <ElTag effect="plain" size="large">{{ todayLabel }}</ElTag>
        <span class="text-13px c-secondary">你好，张三，今天是高效工作的一天！</span>
      </div>
    </div>

    <!-- 五张统计卡 -->
    <div class="grid gap-16px grid-cols-2 lt-lg:grid-cols-2 xl:grid-cols-5">
      <StatCard label="进行中项目" :value="stats.inProgressProjects" :delta="stats.deltas.projects" :delta-up="true" icon="mdi:folder-multiple-outline" />
      <StatCard label="本周新增任务" :value="stats.weekNewTasks" :delta="stats.deltas.tasks" :delta-up="true" icon="mdi:checkbox-multiple-marked-outline" color="#67c23a" />
      <StatCard label="待处理风险" :value="stats.pendingRisks" :delta="stats.deltas.risks" :delta-up="false" icon="mdi:alert-outline" color="#e6a23c" />
      <StatCard label="本月投标数" :value="stats.monthBids" :delta="stats.deltas.bids" :delta-up="true" icon="mdi:poll" color="#9a6fe0" />
      <ElCard shadow="never">
        <div class="flex items-center gap-14px">
          <div class="h-48px w-48px flex shrink-0 items-center justify-center rounded-10px bg-primary/10">
            <SvgIcon icon="mdi:chart-pie" class="text-26px c-primary" />
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex items-center gap-6px">
              <span class="text-13px c-secondary">中标率</span>
              <DemoBadge />
            </div>
            <div class="mt-2px text-26px font-600 leading-32px c-text">
              <template v-if="stats.winRate !== null">{{ stats.winRate }}<span class="text-15px">%</span></template>
              <template v-else>未提供</template>
            </div>
            <div class="mt-2px text-12px c-secondary">历史结果统计，非 AI 预测</div>
          </div>
        </div>
      </ElCard>
    </div>

    <!-- 中部：项目进度 + 待办/通知 -->
    <div class="grid gap-16px lg:grid-cols-[1fr_360px]">
      <ElCard shadow="never" class="min-w-0">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-600">项目进度总览</span>
            <ElButton link type="primary" @click="goProjectList">查看全部</ElButton>
          </div>
        </template>
        <ElTable v-loading="projectsLoading" :data="projects" style="width: 100%">
          <ElTableColumn type="index" label="序号" width="64" />
          <ElTableColumn prop="name" label="项目名称" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <ElLink type="primary" @click="goProject(row.id)">{{ row.name }}</ElLink>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="stage" label="当前阶段" width="110">
            <template #default="{ row }">
              <ElTag effect="plain">{{ row.stage }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="owner" label="负责人" width="90" />
          <ElTableColumn prop="deadline" label="截止日期" width="110" />
          <ElTableColumn label="进度" min-width="160">
            <template #default="{ row }">
              <ElProgress :percentage="row.progress" :status="progressStatus(row.progress) as any" />
            </template>
          </ElTableColumn>
          <ElTableColumn label="风险" width="80" align="center">
            <template #default="{ row }">
              <ElTag :type="projectRiskMap[row.riskLevel as keyof typeof projectRiskMap].type">
                {{ projectRiskMap[row.riskLevel as keyof typeof projectRiskMap].label }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="80" align="center">
            <template #default="{ row }">
              <ElButton link type="primary" @click="goProject(row.id)">查看</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElCard>

      <div class="flex flex-col gap-16px">
        <ElCard shadow="never">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-600">今日待办（{{ todos.filter(t => !t.done).length }}）</span>
            </div>
          </template>
          <div class="flex flex-col gap-4px">
            <div v-for="todo in todos" :key="todo.id" class="flex items-center gap-8px py-4px">
              <ElCheckbox :model-value="todo.done" @change="handleTodoToggle(todo)" />
              <span class="min-w-0 flex-1 truncate text-14px" :class="todo.done ? 'line-through c-secondary' : 'c-text'">
                {{ todo.title }}
              </span>
              <span class="shrink-0 text-12px" :class="todo.done ? 'c-secondary' : 'c-danger'">{{ todo.dueLabel }}</span>
            </div>
          </div>
        </ElCard>

        <ElCard shadow="never">
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-600">消息通知（{{ notices.filter(n => !n.read).length }}）</span>
            </div>
          </template>
          <div class="flex flex-col gap-8px">
            <div
              v-for="notice in notices"
              :key="notice.id"
              class="flex cursor-pointer items-center gap-8px py-4px"
              :title="notice.read ? '已读' : '点击标为已读'"
              @click="handleReadNotice(notice)"
            >
              <ElBadge :is-dot="!notice.read" :offset="[2, 2]">
                <SvgIcon icon="mdi:bell-outline" class="text-18px c-secondary" />
              </ElBadge>
              <span class="min-w-0 flex-1 truncate text-14px" :class="notice.read ? 'c-secondary' : 'c-text'">{{ notice.title }}</span>
              <span class="shrink-0 text-12px c-secondary">{{ notice.timeLabel }}</span>
            </div>
          </div>
        </ElCard>
      </div>
    </div>

    <!-- 底部：趋势 + 快捷入口 + 风险预警 -->
    <div class="grid gap-16px lg:grid-cols-[1.2fr_1fr_1fr]">
      <ElCard shadow="never" class="min-w-0">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-600">近7天任务趋势</span>
            <DemoBadge />
          </div>
        </template>
        <div ref="trendEl" class="h-260px w-full" />
      </ElCard>

      <ElCard shadow="never" class="min-w-0">
        <template #header>
          <span class="font-600">流程快捷入口</span>
        </template>
        <div class="grid grid-cols-2 gap-12px">
          <div class="quick-entry bg-primary/5" @click="goQuick('bid-parse')">
            <SvgIcon icon="mdi:file-search-outline" class="text-24px c-primary" />
            <div class="min-w-0">
              <div class="text-14px font-500">新建解析任务</div>
              <div class="text-12px c-secondary">上传招标文件·智能解析</div>
            </div>
          </div>
          <div class="quick-entry bg-success/8" @click="goQuick('bid-generate')">
            <SvgIcon icon="mdi:file-document-edit-outline" class="text-24px c-success" />
            <div class="min-w-0">
              <div class="text-14px font-500">新建生成任务</div>
              <div class="text-12px c-secondary">基于模板生成标书</div>
            </div>
          </div>
          <div class="quick-entry bg-warning/8" @click="goQuick('bid-review')">
            <SvgIcon icon="mdi:check-decagram-outline" class="text-24px c-warning" />
            <div class="min-w-0">
              <div class="text-14px font-500">新建审查任务</div>
              <div class="text-12px c-secondary">多维度智能审查</div>
            </div>
          </div>
          <div class="quick-entry bg-danger/6" @click="goQuick('reports')">
            <SvgIcon icon="mdi:clipboard-text-search-outline" class="text-24px c-danger" />
            <div class="min-w-0">
              <div class="text-14px font-500">查看报告</div>
              <div class="text-12px c-secondary">查看历史审查报告</div>
            </div>
          </div>
        </div>
      </ElCard>

      <ElCard shadow="never" class="min-w-0">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-600">风险预警（{{ risks.length }}）</span>
          </div>
        </template>
        <div class="flex flex-col gap-10px overflow-auto max-h-240px">
          <div v-for="risk in risks" :key="risk.id" class="cursor-pointer rounded-8px px-8px py-6px hover:bg-gray-1" @click="goRisk(risk)">
            <div class="flex items-center gap-8px">
              <SvgIcon
                :icon="risk.level === 'high' ? 'mdi:alert-decagram' : 'mdi:alert-outline'"
                :class="risk.level === 'high' ? 'c-danger' : 'c-warning'"
                class="text-18px"
              />
              <span class="text-14px font-500 c-text">{{ risk.title }}</span>
              <span class="ml-auto text-12px c-secondary">{{ risk.dateLabel }}</span>
            </div>
            <div class="mt-2px pl-26px text-12px c-secondary">{{ risk.desc }}</div>
          </div>
        </div>
      </ElCard>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.quick-entry {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 10px rgb(0 21 41 / 10%);
  }
}
</style>
