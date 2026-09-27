<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useBidData } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import DemoBadge from '@/components/business/demo-badge.vue';
import PageHeader from '@/components/business/page-header.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'TemplateCenter' });

const { data: templates, loading } = useBidData(() => bidProvider.getTemplates(), [] as Bid.TemplateVM[]);

const filters = reactive({ keyword: '', category: '', industry: '', scene: '', status: '' });
const activeTab = ref<'all' | Bid.TemplateVM['category']>('all');

const industryOptions = ['建筑工程', '政府事务', '信息技术', '综合', '软件与信息化', 'IT 运维服务', '工程管理', '通用'];
const sceneOptions = ['工程投标', '政府采购', '智慧城市', '通用商务', '软件项目', '运维', '项目管理', '附件编制'];
const statusMap: Record<Bid.TemplateVM['status'], { label: string; type: 'success' | 'warning' | 'info' }> = {
  published: { label: '已发布', type: 'success' },
  draft: { label: '草稿', type: 'info' },
  reviewing: { label: '审核中', type: 'warning' }
};

const tabs = [
  { key: 'all', label: '全部' },
  { key: '标准投标模板', label: '标准投标模板' },
  { key: '商务标模板', label: '商务标' },
  { key: '技术标模板', label: '技术标' },
  { key: '附件模板', label: '附件' },
  { key: '自定义模板', label: '自定义' }
] as const;

const filtered = computed(() =>
  templates.value.filter(t => {
    if (activeTab.value !== 'all' && t.category !== activeTab.value) return false;
    if (filters.keyword && !t.name.includes(filters.keyword) && !t.desc.includes(filters.keyword)) return false;
    if (filters.category && t.category !== filters.category) return false;
    if (filters.industry && t.industry !== filters.industry) return false;
    if (filters.scene && t.scene !== filters.scene) return false;
    if (filters.status && t.status !== filters.status) return false;
    return true;
  })
);

const recommended = computed(() => templates.value.filter(t => t.recommended).slice(0, 3));

const { data: versions } = useBidData(() => bidProvider.getTemplateVersions('TP-005'), [] as Bid.TemplateVersionVM[]);

function resetFilters() {
  filters.keyword = '';
  filters.category = '';
  filters.industry = '';
  filters.scene = '';
  filters.status = '';
}

function batchImport() {
  window.$message?.info('批量导入依赖文件上传与模板解析接口：暂未接入');
}

/** 预览 / 编辑抽屉 */
const editVisible = ref(false);
const editing = reactive<Bid.TemplateVM>({
  id: '', name: '', category: '标准投标模板', version: 'V1.0', industry: '', scene: '', chapters: 0, usage: 0,
  updatedAt: '', creator: '', status: 'draft', desc: '', coverTone: 0
});

function openEdit(t: Bid.TemplateVM | null) {
  if (t) Object.assign(editing, t);
  else
    Object.assign(editing, {
      id: `TP-${Date.now()}`, name: '', category: '标准投标模板', version: 'V1.0', industry: '', scene: '',
      chapters: 0, usage: 0, updatedAt: new Date().toLocaleString(), creator: '张三', status: 'draft', desc: '', coverTone: 0
    } as Bid.TemplateVM);
  editVisible.value = true;
}

function saveTemplate() {
  if (!editing.name) {
    window.$message?.warning('请填写模板名称');
    return;
  }
  const idx = templates.value.findIndex(t => t.id === editing.id);
  const item = { ...editing, updatedAt: new Date().toLocaleString() };
  if (idx >= 0) templates.value[idx] = item;
  else templates.value.unshift(item);
  editVisible.value = false;
  window.$message?.success('模板已保存（演示工作副本，不提交后端）');
}

function duplicateTemplate(t: Bid.TemplateVM) {
  const copy: Bid.TemplateVM = {
    ...t,
    id: `TP-${Date.now()}`,
    name: `${t.name}（副本）`,
    status: 'draft',
    usage: 0,
    version: 'V1.0',
    updatedAt: new Date().toLocaleString()
  };
  templates.value.unshift(copy);
  window.$message?.success('已复制演示记录');
}

function publishTemplate(t: Bid.TemplateVM) {
  if (t.status === 'published') {
    window.$message?.info('该模板已是发布状态');
    return;
  }
  window.$messageBox?.confirm(`确认发布模板「${t.name}」？`, '发布确认', {
    confirmButtonText: '确认发布',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      t.status = 'published';
      window.$message?.success('已发布（演示工作副本）');
    })
    .catch(() => undefined);
}

/** 本轮不做 DOCX 排版引擎：预览抽屉展示章节与格式字段信息 */
const previewVisible = ref(false);
const previewTemplate = ref<Bid.TemplateVM | null>(null);

function openPreview(t: Bid.TemplateVM) {
  previewTemplate.value = t;
  previewVisible.value = true;
}

const coverColors = ['#dbeafe', '#dcfce7', '#fef3c7', '#fee2e2', '#ede9fe', '#cffafe', '#fce7f3', '#f1f5f9'];
</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px">
    <PageHeader title="模板中心" subtitle="统一管理投标模板、章节版式与企业模板资产">
      <template #actions>
        <DemoBadge />
        <ElButton type="primary" icon="mdi:plus" @click="openEdit(null)">新建模板</ElButton>
        <ElButton icon="mdi:upload" @click="batchImport">批量导入</ElButton>
      </template>
    </PageHeader>

    <ElCard shadow="never">
      <div class="flex flex-wrap items-center gap-12px">
        <ElInput v-model="filters.keyword" placeholder="搜索模板名称、关键词" clearable class="w-240px">
          <template #prefix><SvgIcon icon="mdi:magnify" /></template>
        </ElInput>
        <ElSelect v-model="filters.category" placeholder="模板类型" clearable class="w-150px">
          <ElOption v-for="t in tabs.slice(1)" :key="t.key" :label="t.label" :value="t.key" />
        </ElSelect>
        <ElSelect v-model="filters.industry" placeholder="行业分类" clearable class="w-150px">
          <ElOption v-for="i in industryOptions" :key="i" :label="i" :value="i" />
        </ElSelect>
        <ElSelect v-model="filters.scene" placeholder="适用场景" clearable class="w-150px">
          <ElOption v-for="s in sceneOptions" :key="s" :label="s" :value="s" />
        </ElSelect>
        <ElSelect v-model="filters.status" placeholder="状态" clearable class="w-120px">
          <ElOption label="已发布" value="published" />
          <ElOption label="草稿" value="draft" />
          <ElOption label="审核中" value="reviewing" />
        </ElSelect>
        <ElButton @click="resetFilters">重置</ElButton>
      </div>
    </ElCard>

    <div class="grid gap-16px xl:grid-cols-[1fr_300px]">
      <div class="min-w-0">
        <ElCard shadow="never">
          <ElTabs v-model="activeTab">
            <ElTabPane v-for="t in tabs" :key="t.key" :label="t.label" :name="t.key" />
          </ElTabs>
          <div v-loading="loading" class="grid gap-14px sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
            <div
              v-for="t in filtered"
              :key="t.id"
              class="flex flex-col rounded-12px border border-solid border-gray-2 p-12px transition hover:shadow-md"
            >
              <div class="flex items-start gap-10px">
                <div class="h-64px w-48px shrink-0 rounded-6px" :style="{ background: coverColors[t.coverTone % coverColors.length] }"></div>
                <div class="min-w-0 flex-1">
                  <div class="flex items-center gap-6px">
                    <span class="min-w-0 flex-1 truncate text-14px font-600">{{ t.name }}</span>
                    <ElTag size="small" effect="plain">{{ t.version }}</ElTag>
                  </div>
                  <div class="mt-4px truncate text-12px c-secondary">{{ t.desc }}</div>
                </div>
              </div>
              <div class="mt-8px grid grid-cols-2 gap-x-8px gap-y-2px text-12px c-secondary">
                <span class="truncate">行业：{{ t.industry }}</span>
                <span>章节：{{ t.chapters }}</span>
                <span>使用：{{ t.usage }} 次</span>
                <span class="truncate">创建人：{{ t.creator }}</span>
              </div>
              <div class="mt-8px flex items-center justify-between">
                <ElTag size="small" :type="statusMap[t.status].type">{{ statusMap[t.status].label }}</ElTag>
                <span class="text-12px c-secondary">{{ t.updatedAt }}</span>
              </div>
              <div class="mt-8px flex flex-wrap gap-6px">
                <ElButton size="small" plain type="primary" @click="openPreview(t)">预览</ElButton>
                <ElButton size="small" plain @click="openEdit(t)">编辑</ElButton>
                <ElButton size="small" plain @click="duplicateTemplate(t)">复制</ElButton>
                <ElButton v-if="t.status !== 'published'" size="small" plain type="warning" @click="publishTemplate(t)">发布</ElButton>
              </div>
            </div>
          </div>
          <ElEmpty v-if="!loading && !filtered.length" description="未找到匹配模板" />
        </ElCard>

        <ElCard shadow="never" class="mt-16px">
          <template #header><span class="font-600">最近编辑模板</span></template>
          <ElTable :data="templates.slice(0, 6)" size="small" style="width: 100%">
            <ElTableColumn prop="name" label="模板名称" min-width="180" show-overflow-tooltip />
            <ElTableColumn prop="category" label="类型" width="120" />
            <ElTableColumn prop="creator" label="修改人" width="90" />
            <ElTableColumn prop="updatedAt" label="更新时间" width="160" />
            <ElTableColumn label="状态" width="90">
              <template #default="{ row }">
                <ElTag size="small" :type="statusMap[row.status as keyof typeof statusMap].type">
                  {{ statusMap[row.status as keyof typeof statusMap].label }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="160">
              <template #default="{ row }">
                <ElButton link type="primary" size="small" @click="openPreview(row)">预览</ElButton>
                <ElButton link size="small" @click="openEdit(row)">编辑</ElButton>
                <ElButton link size="small" @click="duplicateTemplate(row)">复制</ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElCard>
      </div>

      <div class="flex flex-col gap-16px">
        <ElCard shadow="never">
          <template #header><span class="font-600">推荐模板</span></template>
          <div class="flex flex-col gap-10px">
            <div
              v-for="t in recommended"
              :key="t.id"
              class="cursor-pointer rounded-8px border border-solid border-gray-2 p-10px hover:border-primary/50"
              @click="openPreview(t)"
            >
              <div class="flex items-center gap-6px">
                <span class="min-w-0 flex-1 truncate text-14px font-500">{{ t.name }}</span>
                <ElTag size="small" type="warning" effect="plain">推荐</ElTag>
              </div>
              <div class="mt-4px text-12px c-secondary">{{ t.desc }}</div>
              <div class="mt-4px text-12px c-secondary">使用次数 {{ t.usage }}</div>
            </div>
            <p class="mb-0 text-12px c-secondary">推荐结果为预置样例，未实现推荐算法。</p>
          </div>
        </ElCard>

        <ElCard shadow="never">
          <template #header><span class="font-600">模板版本记录</span></template>
          <ElTimeline>
            <ElTimelineItem v-for="v in versions" :key="v.id" :timestamp="`${v.version} · ${v.updatedAt} · ${v.creator}`">
              {{ v.note }}
            </ElTimelineItem>
          </ElTimeline>
        </ElCard>
      </div>
    </div>

    <!-- 新建/编辑抽屉 -->
    <ElDrawer v-model="editVisible" :title="editing.name ? '编辑模板' : '新建模板'" size="520px">
      <ElForm label-width="90px">
        <ElFormItem label="模板名称" required>
          <ElInput v-model="editing.name" placeholder="请输入模板名称" />
        </ElFormItem>
        <ElFormItem label="分类">
          <ElSelect v-model="editing.category" class="w-full">
            <ElOption v-for="t in tabs.slice(1)" :key="t.key" :label="t.label" :value="t.key" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="行业 / 场景">
          <div class="flex w-full gap-8px">
            <ElSelect v-model="editing.industry" placeholder="行业" class="flex-1">
              <ElOption v-for="i in industryOptions" :key="i" :label="i" :value="i" />
            </ElSelect>
            <ElSelect v-model="editing.scene" placeholder="场景" class="flex-1">
              <ElOption v-for="s in sceneOptions" :key="s" :label="s" :value="s" />
            </ElSelect>
          </div>
        </ElFormItem>
        <ElFormItem label="章节数">
          <ElInputNumber v-model="editing.chapters" :min="0" :max="99" />
        </ElFormItem>
        <ElFormItem label="描述">
          <ElInput v-model="editing.desc" type="textarea" :rows="3" />
        </ElFormItem>
        <ElFormItem label="格式属性">
          <ElAlert type="info" :closable="false" title="章节与版式字段编辑为前端数据编辑；DOCX 排版引擎本轮不做。" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="editVisible = false">取消</ElButton>
        <ElButton type="primary" @click="saveTemplate">保存</ElButton>
      </template>
    </ElDrawer>

    <!-- 预览抽屉 -->
    <ElDrawer v-model="previewVisible" :title="`模板预览：${previewTemplate?.name || ''}`" size="480px">
      <template v-if="previewTemplate">
        <ElDescriptions :column="1" border>
          <ElDescriptionsItem label="版本">{{ previewTemplate.version }}</ElDescriptionsItem>
          <ElDescriptionsItem label="分类">{{ previewTemplate.category }}</ElDescriptionsItem>
          <ElDescriptionsItem label="行业 / 场景">{{ previewTemplate.industry }} · {{ previewTemplate.scene }}</ElDescriptionsItem>
          <ElDescriptionsItem label="章节数">{{ previewTemplate.chapters }}</ElDescriptionsItem>
          <ElDescriptionsItem label="使用次数">{{ previewTemplate.usage }}</ElDescriptionsItem>
          <ElDescriptionsItem label="状态">{{ statusMap[previewTemplate.status].label }}</ElDescriptionsItem>
          <ElDescriptionsItem label="描述">{{ previewTemplate.desc }}</ElDescriptionsItem>
        </ElDescriptions>
        <p class="mb-0 mt-12px text-12px c-secondary">
          本轮不做 DOCX 排版渲染；招标要求成果与投标排版模板是两种对象，本页仅管理投标排版模板。
        </p>
      </template>
    </ElDrawer>


  </div>
</template>
