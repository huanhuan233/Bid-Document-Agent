<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouterPush } from '@/hooks/common/router';
import { useBidData } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import { projectRiskMap } from '@/constants/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import PageHeader from '@/components/business/page-header.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'ProjectList' });

const { routerPush } = useRouterPush();
const { data: projects, loading } = useBidData(() => bidProvider.getProjects(), [] as Bid.ProjectVM[]);

const filters = reactive({ keyword: '', stage: '', risk: '' });

const filtered = computed(() =>
  projects.value.filter(p => {
    if (filters.keyword && !p.name.includes(filters.keyword)) return false;
    if (filters.stage && p.stage !== filters.stage) return false;
    if (filters.risk && p.riskLevel !== filters.risk) return false;
    return true;
  })
);

const stageOptions = ['资料整理', '标书解析', '标书生成', '标书审查', '投标递交', '已结束'];

function resetFilters() {
  filters.keyword = '';
  filters.stage = '';
  filters.risk = '';
}

function goDetail(id: string) {
  routerPush({ key: 'project-detail', params: { id } });
}

/** 项目编辑抽屉（演示写操作：仅改本地工作副本） */
const editVisible = ref(false);
const editing = reactive<Bid.ProjectVM>({
  id: '', name: '', owner: '', stage: '资料整理', deadline: '', progress: 0, riskLevel: 'none', riskDesc: '', updatedAt: ''
});

function openEdit(p: Bid.ProjectVM) {
  Object.assign(editing, p);
  editVisible.value = true;
}

function openCreate() {
  Object.assign(editing, {
    id: `P-${Date.now()}`, name: '', owner: '张三', stage: '资料整理', deadline: '', progress: 0,
    riskLevel: 'none', riskDesc: '', updatedAt: ''
  } as Bid.ProjectVM);
  editVisible.value = true;
}

function saveEdit() {
  if (!editing.name) {
    window.$message?.warning('请填写项目名称');
    return;
  }
  const idx = projects.value.findIndex(p => p.id === editing.id);
  const item = { ...editing, updatedAt: new Date().toLocaleString() };
  if (idx >= 0) projects.value[idx] = item;
  else projects.value.push(item);
  editVisible.value = false;
  window.$message?.success('已保存（演示工作副本，不提交真实后端）');
}
</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px [&>*]:shrink-0">
    <PageHeader title="项目列表" subtitle="管理投标项目，进入项目详情查看解析、生成、审查进展">
      <template #actions>
        <DemoBadge />
        <ElButton type="primary" icon="mdi:plus" @click="openCreate">新建项目</ElButton>
      </template>
    </PageHeader>

    <ElCard shadow="never">
      <div class="flex flex-wrap items-center gap-12px">
        <ElInput v-model="filters.keyword" placeholder="项目名称" clearable class="w-220px" />
        <ElSelect v-model="filters.stage" placeholder="当前阶段" clearable class="w-160px">
          <ElOption v-for="s in stageOptions" :key="s" :label="s" :value="s" />
        </ElSelect>
        <ElSelect v-model="filters.risk" placeholder="风险状态" clearable class="w-140px">
          <ElOption label="高" value="high" />
          <ElOption label="中" value="medium" />
          <ElOption label="低" value="low" />
        </ElSelect>
        <ElButton @click="resetFilters">重置</ElButton>
      </div>
    </ElCard>

    <ElCard shadow="never">
      <ElTable v-loading="loading" :data="filtered" style="width: 100%">
        <ElTableColumn type="index" label="序号" width="64" />
        <ElTableColumn prop="name" label="项目名称" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <ElLink type="primary" @click="goDetail(row.id)">{{ row.name }}</ElLink>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="owner" label="负责人" width="90" />
        <ElTableColumn prop="stage" label="当前阶段" width="110">
          <template #default="{ row }">
            <ElTag effect="plain">{{ row.stage }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="deadline" label="截止日期" width="110" />
        <ElTableColumn label="进度" min-width="150">
          <template #default="{ row }">
            <ElProgress :percentage="row.progress" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="风险" width="80" align="center">
          <template #default="{ row }">
            <ElTag :type="projectRiskMap[row.riskLevel as keyof typeof projectRiskMap].type">
              {{ projectRiskMap[row.riskLevel as keyof typeof projectRiskMap].label }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="updatedAt" label="更新时间" width="170" />
        <ElTableColumn label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <ElButton link type="primary" @click="goDetail(row.id)">详情</ElButton>
            <ElButton link @click="openEdit(row)">编辑</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <ElDrawer v-model="editVisible" :title="editing.name ? '编辑项目' : '新建项目'" size="480px">
      <ElForm label-width="90px">
        <ElFormItem label="项目名称" required>
          <ElInput v-model="editing.name" placeholder="请输入项目名称" />
        </ElFormItem>
        <ElFormItem label="负责人">
          <ElInput v-model="editing.owner" />
        </ElFormItem>
        <ElFormItem label="当前阶段">
          <ElSelect v-model="editing.stage" class="w-full">
            <ElOption v-for="s in stageOptions" :key="s" :label="s" :value="s" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="截止日期">
          <ElDatePicker v-model="editing.deadline" type="date" value-format="YYYY-MM-DD" class="w-full" />
        </ElFormItem>
        <ElFormItem label="进度">
          <ElSlider v-model="editing.progress" show-input />
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="editing.desc" type="textarea" :rows="3" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="editVisible = false">取消</ElButton>
        <ElButton type="primary" @click="saveEdit">保存</ElButton>
      </template>
    </ElDrawer>
  </div>
</template>
