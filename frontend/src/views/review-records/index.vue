<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouterPush } from '@/hooks/common/router';
import { useBidData } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import PageHeader from '@/components/business/page-header.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'BidReviewRecords' });

const { routerPush } = useRouterPush();
const { data: records, loading } = useBidData(() => bidProvider.getReviewRecords(), [] as Bid.ReviewRecordVM[]);
const { data: projects } = useBidData(() => bidProvider.getProjects(), [] as Bid.ProjectVM[]);

const filters = reactive({ keyword: '', projectId: '', status: '' });
const currentPage = ref(1);
const pageSize = ref(10);

const filtered = computed(() =>
  records.value.filter(r => {
    if (filters.keyword && !r.projectName.includes(filters.keyword) && !r.id.includes(filters.keyword)) return false;
    if (filters.projectId && r.projectId !== filters.projectId) return false;
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
  filters.status = '';
  currentPage.value = 1;
}

const statusType = (s: string) => (s === '已完成' ? 'success' : s === '审查中' ? 'primary' : 'info');
</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px [&>*]:shrink-0">
    <PageHeader title="审查记录" subtitle="查看历史审查任务与报告入口">
      <template #actions>
        <DemoBadge />
        <ElButton @click="routerPush({ key: 'bid-review' })">返回审查中心</ElButton>
      </template>
    </PageHeader>

    <ElCard shadow="never">
      <div class="flex flex-wrap items-center gap-12px">
        <ElInput v-model="filters.keyword" placeholder="任务号 / 项目" clearable class="w-200px" />
        <ElSelect v-model="filters.projectId" placeholder="项目" clearable class="w-220px">
          <ElOption v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </ElSelect>
        <ElSelect v-model="filters.status" placeholder="状态" clearable class="w-130px">
          <ElOption label="已完成" value="已完成" />
          <ElOption label="审查中" value="审查中" />
          <ElOption label="排队中" value="排队中" />
        </ElSelect>
        <ElButton @click="resetFilters">重置</ElButton>
      </div>
    </ElCard>

    <ElCard shadow="never">
      <ElTable v-loading="loading" :data="paged" style="width: 100%">
        <ElTableColumn prop="id" label="任务号" width="130" />
        <ElTableColumn prop="projectName" label="项目" min-width="170" show-overflow-tooltip />
        <ElTableColumn prop="basisLabel" label="审查依据" min-width="180" show-overflow-tooltip />
        <ElTableColumn prop="docVersionLabel" label="投标文件" min-width="130" show-overflow-tooltip />
        <ElTableColumn label="状态" width="90">
          <template #default="{ row }">
            <ElTag :type="statusType(row.status) as any">{{ row.status }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="高/中/低" width="100">
          <template #default="{ row }">
            <span class="c-danger">{{ row.high }}</span> / <span class="c-warning">{{ row.medium }}</span> /
            <span class="c-success">{{ row.low }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="operator" label="操作者" width="90" />
        <ElTableColumn prop="startedAt" label="开始时间" width="160" />
        <ElTableColumn label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <ElButton v-if="row.reportId" link type="primary" @click="routerPush({ key: 'report-detail', params: { id: row.reportId } })">报告</ElButton>
            <span v-else class="text-12px c-secondary">未生成</span>
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
