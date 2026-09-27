<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouterPush } from '@/hooks/common/router';
import { useBidData } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import PageHeader from '@/components/business/page-header.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'BidGenerateRecords' });

const { routerPush } = useRouterPush();
const { data: records, loading } = useBidData(() => bidProvider.getGenerationRecords(), [] as Bid.GenerationRecordVM[]);
const { data: projects } = useBidData(() => bidProvider.getProjects(), [] as Bid.ProjectVM[]);

const filters = reactive({ keyword: '', projectId: '', mode: '', status: '' });
const currentPage = ref(1);
const pageSize = ref(10);

const filtered = computed(() =>
  records.value.filter(r => {
    if (filters.keyword && !r.projectName.includes(filters.keyword) && !r.id.includes(filters.keyword)) return false;
    if (filters.projectId && r.projectId !== filters.projectId) return false;
    if (filters.mode && r.mode !== filters.mode) return false;
    if (filters.status && r.status !== filters.status) return false;
    return true;
  })
);

const paged = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filtered.value.slice(start, start + pageSize.value);
});

function resetFilters() {
  filters.keyword = '';
  filters.projectId = '';
  filters.mode = '';
  filters.status = '';
  currentPage.value = 1;
}

const statusType = (s: string) => (s === '已完成' ? 'success' : s === '生成中' ? 'primary' : s === '失败' ? 'danger' : 'warning');
</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px">
    <PageHeader title="生成记录" subtitle="查看常规生成与整改修订任务">
      <template #actions>
        <DemoBadge />
        <ElButton @click="routerPush({ key: 'bid-generate' })">返回生成中心</ElButton>
      </template>
    </PageHeader>

    <ElCard shadow="never">
      <div class="flex flex-wrap items-center gap-12px">
        <ElInput v-model="filters.keyword" placeholder="任务号 / 项目" clearable class="w-200px" />
        <ElSelect v-model="filters.projectId" placeholder="项目" clearable class="w-220px">
          <ElOption v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </ElSelect>
        <ElSelect v-model="filters.mode" placeholder="生成方式" clearable class="w-160px">
          <ElOption label="常规生成" value="normal" />
          <ElOption label="按整改意见修订" value="revision" />
        </ElSelect>
        <ElSelect v-model="filters.status" placeholder="状态" clearable class="w-130px">
          <ElOption label="已完成" value="已完成" />
          <ElOption label="生成中" value="生成中" />
          <ElOption label="待完善" value="待完善" />
          <ElOption label="失败" value="失败" />
        </ElSelect>
        <ElButton @click="resetFilters">重置</ElButton>
      </div>
    </ElCard>

    <ElCard shadow="never">
      <ElTable v-loading="loading" :data="paged" style="width: 100%">
        <ElTableColumn prop="id" label="任务号" width="130" />
        <ElTableColumn prop="projectName" label="项目" min-width="180" show-overflow-tooltip />
        <ElTableColumn label="方式" width="140">
          <template #default="{ row }">
            <ElTag :type="row.mode === 'revision' ? 'warning' : 'primary'" effect="plain">
              {{ row.mode === 'revision' ? '按整改意见修订' : '常规生成' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="scope" label="范围" width="100" />
        <ElTableColumn label="状态" width="90">
          <template #default="{ row }">
            <ElTag :type="statusType(row.status) as any">{{ row.status }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="章节" width="90">
          <template #default="{ row }">{{ row.chaptersDone }}/{{ row.chaptersTotal }}</template>
        </ElTableColumn>
        <ElTableColumn prop="operator" label="操作者" width="90" />
        <ElTableColumn prop="startedAt" label="开始时间" width="160" />
        <ElTableColumn prop="finishedAt" label="完成时间" width="160" />
        <ElTableColumn label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <ElButton link type="primary" @click="routerPush({ key: 'bid-generate', query: { projectId: row.projectId } })">打开</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      <div class="mt-16px flex justify-end">
        <ElPagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="filtered.length"
          layout="total, prev, pager, next, sizes"
          :page-sizes="[10, 20, 50]"
        />
      </div>
    </ElCard>
  </div>
</template>
