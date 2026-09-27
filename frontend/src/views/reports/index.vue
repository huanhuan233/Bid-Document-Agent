<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouterPush } from '@/hooks/common/router';
import { useBidData } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import PageHeader from '@/components/business/page-header.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'ReviewReportList' });

const { routerPush } = useRouterPush();
const { data: reports, loading } = useBidData(() => bidProvider.getReports(), [] as Bid.ReviewRecordVM[]);
const { data: projects } = useBidData(() => bidProvider.getProjects(), [] as Bid.ProjectVM[]);

const filters = reactive({ keyword: '', projectId: '' });
const currentPage = ref(1);
const pageSize = ref(10);

const filtered = computed(() =>
  reports.value.filter(r => {
    if (filters.keyword && !r.projectName.includes(filters.keyword) && !r.id.includes(filters.keyword)) return false;
    if (filters.projectId && r.projectId !== filters.projectId) return false;
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
  currentPage.value = 1;
}

function goDetail(row: Bid.ReviewRecordVM) {
  if (!row.reportId) return;
  routerPush({ key: 'report-detail', params: { id: row.reportId } });
}

/** 分享：复制站内受权限控制的详情链接（不含 token 与敏感正文） */
async function shareLink(row: Bid.ReviewRecordVM) {
  if (!row.reportId) return;
  const url = `${location.origin}/report-detail/${row.reportId}`;
  try {
    await navigator.clipboard.writeText(url);
    window.$message?.success('已复制站内详情链接（需登录且有权限才可访问）');
  } catch {
    window.$message?.info(`详情链接：${url}`);
  }
}

function exportReport() {
  window.$message?.warning('导出 PDF/Word 依赖文档服务：暂未接入，未生成文件');
}
</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px">
    <PageHeader title="审查报告" subtitle="审查报告列表，点击查看详情、分布与整改建议">
      <template #actions>
        <DemoBadge />
        <ElButton icon="mdi:file-pdf-box" @click="exportReport">导出报告</ElButton>
      </template>
    </PageHeader>

    <ElCard shadow="never">
      <div class="flex flex-wrap items-center gap-12px">
        <ElInput v-model="filters.keyword" placeholder="报告编号 / 项目" clearable class="w-220px" />
        <ElSelect v-model="filters.projectId" placeholder="项目" clearable class="w-220px">
          <ElOption v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </ElSelect>
        <ElButton @click="resetFilters">重置</ElButton>
      </div>
    </ElCard>

    <ElCard shadow="never">
      <ElTable v-loading="loading" :data="paged" style="width: 100%">
        <ElTableColumn prop="id" label="报告编号" width="130" />
        <ElTableColumn prop="projectName" label="项目" min-width="170" show-overflow-tooltip />
        <ElTableColumn prop="basisLabel" label="招标要求版本" min-width="180" show-overflow-tooltip />
        <ElTableColumn prop="docVersionLabel" label="投标文件版本" min-width="140" show-overflow-tooltip />
        <ElTableColumn prop="reviewedAt" label="审查时间" width="150" />
        <ElTableColumn label="高/中/低" width="100">
          <template #default="{ row }">
            <span class="c-danger">{{ row.high }}</span> / <span class="c-warning">{{ row.medium }}</span> /
            <span class="c-success">{{ row.low }}</span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <ElButton link type="primary" @click="goDetail(row)">查看</ElButton>
            <ElButton link @click="shareLink(row)">分享</ElButton>
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
