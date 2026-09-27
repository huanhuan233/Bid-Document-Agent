<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useRouterPush } from '@/hooks/common/router';
import { useBidData } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import { projectRiskMap } from '@/constants/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'ProjectDetail' });

const route = useRoute();
const { routerPush } = useRouterPush();
const projectId = String(route.params.id);

const { data: projects } = useBidData(() => bidProvider.getProjects(), [] as Bid.ProjectVM[]);
const project = computed(() => projects.value.find(p => p.id === projectId));

const { data: parseFiles } = useBidData(() => bidProvider.getParseFiles(projectId), [] as Bid.ParseFileVM[]);
const { data: requirementVersions } = useBidData(() => bidProvider.getRequirementVersions(projectId), []);
const { data: reports } = useBidData(() => bidProvider.getReports(), [] as Bid.ReviewRecordVM[]);
const { data: genRecords } = useBidData(() => bidProvider.getGenerationRecords(), [] as Bid.GenerationRecordVM[]);

const genOfProject = computed(() => genRecords.value.filter(g => g.projectId === projectId));
const reportsOfProject = computed(() => reports.value.filter(r => r.projectId === projectId));

const activeTab = ref('overview');

function goBack() {
  routerPush({ key: 'projects' });
}

function goTab(key: 'bid-parse' | 'bid-generate' | 'bid-review' | 'reports') {
  routerPush({ key, query: { projectId } });
}

const riskOf = computed(() => (project.value ? projectRiskMap[project.value.riskLevel] : null));
</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px">
    <div class="flex items-center justify-between gap-12px">
      <div class="min-w-0">
        <ElButton link @click="goBack">
          <SvgIcon icon="mdi:arrow-left" class="mr-4px" /> 返回列表
        </ElButton>
        <h2 class="m-0 mt-6px truncate text-22px font-600 c-text">{{ project?.name || projectId }}</h2>
      </div>
      <DemoBadge />
    </div>

    <ElEmpty v-if="!project && projects.length" :description="`未找到项目 ${projectId}`" />

    <template v-else-if="project">
      <ElCard shadow="never">
        <ElDescriptions :column="3" border>
          <ElDescriptionsItem label="项目编号">{{ project.id }}</ElDescriptionsItem>
          <ElDescriptionsItem label="负责人">{{ project.owner }}</ElDescriptionsItem>
          <ElDescriptionsItem label="当前阶段">{{ project.stage }}</ElDescriptionsItem>
          <ElDescriptionsItem label="截止日期">{{ project.deadline }}</ElDescriptionsItem>
          <ElDescriptionsItem label="进度">
            <ElProgress class="w-200px" :percentage="project.progress" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="风险状态">
            <ElTag :type="riskOf?.type">{{ riskOf?.label }}</ElTag>
            <span class="ml-8px text-13px c-secondary">{{ project.riskDesc }}</span>
          </ElDescriptionsItem>
        </ElDescriptions>
        <p v-if="project.desc" class="mb-0 mt-12px text-13px c-secondary">{{ project.desc }}</p>
      </ElCard>

      <ElCard shadow="never">
        <ElTabs v-model="activeTab">
          <ElTabPane label="关联解析文件" name="parse">
            <ElTable :data="parseFiles" style="width: 100%">
              <ElTableColumn prop="name" label="文件名" min-width="240" show-overflow-tooltip />
              <ElTableColumn prop="type" label="类型" width="110" />
              <ElTableColumn prop="pages" label="页数" width="80" />
              <ElTableColumn prop="uploadedAt" label="上传时间" width="170" />
              <ElTableColumn label="操作" width="120">
                <template #default>
                  <ElButton link type="primary" @click="goTab('bid-parse')">去解析</ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
          </ElTabPane>
          <ElTabPane label="招标要求版本" name="versions">
            <ElTable :data="requirementVersions" style="width: 100%">
              <ElTableColumn prop="name" label="版本名称" min-width="240" />
              <ElTableColumn prop="version" label="版本" width="90" />
              <ElTableColumn label="确认状态" width="120">
                <template #default="{ row }">
                  <ElTag :type="row.confirmed ? 'success' : 'info'">{{ row.confirmed ? '已确认' : '未确认' }}</ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="confirmedAt" label="确认时间" width="170" />
            </ElTable>
          </ElTabPane>
          <ElTabPane label="生成记录" name="gen">
            <ElTable :data="genOfProject" style="width: 100%">
              <ElTableColumn prop="id" label="任务号" width="140" />
              <ElTableColumn label="方式" width="140">
                <template #default="{ row }">
                  <ElTag :type="row.mode === 'revision' ? 'warning' : 'primary'" effect="plain">
                    {{ row.mode === 'revision' ? '按整改意见修订' : '常规生成' }}
                  </ElTag>
                </template>
              </ElTableColumn>
              <ElTableColumn prop="scope" label="范围" width="100" />
              <ElTableColumn prop="status" label="状态" width="100" />
              <ElTableColumn label="章节" width="100">
                <template #default="{ row }">{{ row.chaptersDone }}/{{ row.chaptersTotal }}</template>
              </ElTableColumn>
              <ElTableColumn label="操作">
                <template #default>
                  <ElButton link type="primary" @click="goTab('bid-generate')">去生成</ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
          </ElTabPane>
          <ElTabPane label="审查报告" name="reports">
            <ElTable :data="reportsOfProject" style="width: 100%">
              <ElTableColumn prop="id" label="报告编号" width="140" />
              <ElTableColumn prop="basisLabel" label="审查依据" min-width="220" />
              <ElTableColumn prop="docVersionLabel" label="投标文件" min-width="160" />
              <ElTableColumn label="高/中/低" width="110">
                <template #default="{ row }">
                  <span class="c-danger">{{ row.high }}</span> /
                  <span class="c-warning">{{ row.medium }}</span> /
                  <span class="c-success">{{ row.low }}</span>
                </template>
              </ElTableColumn>
              <ElTableColumn label="操作">
                <template #default="{ row }">
                  <ElButton v-if="row.reportId" link type="primary" @click="routerPush({ key: 'report-detail', params: { id: row.reportId } })">
                    查看报告
                  </ElButton>
                  <span v-else class="text-12px c-secondary">未生成</span>
                </template>
              </ElTableColumn>
            </ElTable>
          </ElTabPane>

        </ElTabs>
      </ElCard>
    </template>
  </div>
</template>
