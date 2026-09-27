<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouterPush } from '@/hooks/common/router';
import { useBidData } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import { parseStatusMap } from '@/constants/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import PageHeader from '@/components/business/page-header.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'BidParseRecords' });

const { routerPush } = useRouterPush();
const { data: records, loading } = useBidData(() => bidProvider.getParseRecords(), [] as Bid.ParseRecordVM[]);
const { data: projects } = useBidData(() => bidProvider.getProjects(), [] as Bid.ProjectVM[]);

const filters = reactive({ keyword: '', projectId: '', status: '' });
const currentPage = ref(1);
const pageSize = ref(10);

const filtered = computed(() =>
  records.value.filter(r => {
    if (filters.keyword && !r.fileName.includes(filters.keyword) && !r.projectName.includes(filters.keyword)) return false;
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

function goDetail(row: Bid.ParseRecordVM) {
  if (!row.resultId) {
    window.$message?.info('该任务尚未完成解析，暂无结果详情');
    return;
  }
  routerPush({ key: 'parse-result', params: { id: row.resultId } });
}
</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px">
    <PageHeader title="解析记录" subtitle="查看历史解析任务的状态、结果与详情入口">
      <template #actions>
        <DemoBadge />
        <ElButton @click="routerPush({ key: 'bid-parse' })">返回解析中心</ElButton>
      </template>
    </PageHeader>

    <ElCard shadow="never">
      <div class="flex flex-wrap items-center gap-12px">
        <ElInput v-model="filters.keyword" placeholder="任务名 / 项目 / 文件" clearable class="w-220px" />
        <ElSelect v-model="filters.projectId" placeholder="项目" clearable class="w-220px">
          <ElOption v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </ElSelect>
        <ElSelect v-model="filters.status" placeholder="状态" clearable class="w-140px">
          <ElOption label="解析完成" value="done" />
          <ElOption label="解析中" value="processing" />
          <ElOption label="待解析" value="pending" />
          <ElOption label="解析失败" value="failed" />
        </ElSelect>
        <ElButton @click="resetFilters">重置</ElButton>
      </div>
    </ElCard>

    <ElCard shadow="never">
      <ElTable v-loading="loading" :data="paged" style="width: 100%">
        <ElTableColumn prop="id" label="任务号" width="130" />
        <ElTableColumn prop="projectName" label="项目" min-width="180" show-overflow-tooltip />
        <ElTableColumn prop="fileName" label="文件" min-width="220" show-overflow-tooltip />
        <ElTableColumn label="状态" width="100">
          <template #default="{ row }">
            <ElTag :type="parseStatusMap[row.status as keyof typeof parseStatusMap].type">
              {{ parseStatusMap[row.status as keyof typeof parseStatusMap].label }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="pages" label="页数" width="80" />
        <ElTableColumn prop="clauses" label="条款数" width="80" />
        <ElTableColumn prop="operator" label="操作者" width="90" />
        <ElTableColumn prop="startedAt" label="开始时间" width="160" />
        <ElTableColumn prop="finishedAt" label="完成时间" width="160" />
        <ElTableColumn label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <ElButton link type="primary" @click="goDetail(row)">详情</ElButton>
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
