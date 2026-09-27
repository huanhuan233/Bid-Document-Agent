<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useBidData } from '@/hooks/business/use-bid-data';
import { bidProvider } from '@/service/providers/bid';
import { demoMaterialStats, materialCategoryTabs } from '@/fixtures/bid/materials';
import DemoBadge from '@/components/business/demo-badge.vue';
import PageHeader from '@/components/business/page-header.vue';
import MaterialSelectDrawer from '@/components/business/material-select-drawer.vue';
import type { Bid } from '@/typings/bid';

defineOptions({ name: 'MaterialLibrary' });

const { data: materials, loading } = useBidData(() => bidProvider.getMaterials(), [] as Bid.MaterialVM[]);
const { data: directories } = useBidData(() => bidProvider.getMaterialDirectories(), [] as Bid.MaterialDirectoryVM[]);

const filters = reactive({ keyword: '', category: 'all', directory: '', status: '', dateRange: [] as string[] });
const activeTab = ref<'all' | string>('all');
const viewMode = ref<'table' | 'card'>('table');
const activeDirectory = ref('');

/** 有效期状态按当前日期动态计算，避免演示数据与真实日期矛盾 */
function expiryStatus(m: Bid.MaterialVM): Bid.MaterialVM['status'] {
  if (!m.expiry) return '有效';
  const days = (new Date(m.expiry).getTime() - Date.now()) / 86400000;
  if (days < 0) return '已过期';
  if (days < 60) return '即将过期';
  return '有效';
}

const filtered = computed(() =>
  materials.value.filter(m => {
    if (activeTab.value !== 'all' && m.type !== activeTab.value) return false;
    if (filters.keyword && !m.name.includes(filters.keyword) && !m.tags.some(t => t.includes(filters.keyword))) return false;
    if (filters.directory && !m.directory.startsWith(filters.directory)) return false;
    if (filters.status && expiryStatus(m) !== filters.status) return false;
    return true;
  })
);

const activeMaterial = ref<Bid.MaterialVM | null>(null);

function selectMaterial(m: Bid.MaterialVM) {
  activeMaterial.value = m;
}

const statusTag = (s: Bid.MaterialVM['status']) => (s === '有效' ? 'success' : s === '即将过期' ? 'warning' : 'danger');

function resetFilters() {
  filters.keyword = '';
  filters.category = 'all';
  filters.directory = '';
  filters.status = '';
  filters.dateRange = [];
  activeDirectory.value = '';
}

/** 上传：本地选择与预览，不经 MinIO 密钥；真实上传走后端文件接口 */
function onLocalUpload(file: File) {
  window.$message?.info(`已选择本地文件「${file.name}」：演示环境仅本地预览，不上传服务器`);
  return false;
}

function createDirectory() {
  window.$message?.info('目录新建为演示写操作：已在新目录命名框中演示（核心存储暂未接入）');
}

/** 加入生成引用（写入生成中心演示引用列表的入口） */
const refDrawerVisible = ref(false);
const refMaterial = ref<Bid.MaterialVM | null>(null);

function openAddRef(m: Bid.MaterialVM) {
  refMaterial.value = m;
  refDrawerVisible.value = true;
}

function addToGenerate(selected: { id: string; name: string }[]) {
  if (!refMaterial.value) return;
  window.$message?.success(`已将「${refMaterial.value.name}」加入生成引用（演示工作副本），可在生成中心查看`);
}

function toggleRecommend(m: Bid.MaterialVM) {
  m.recommended = !m.recommended;
  window.$message?.success(m.recommended ? '已设为推荐（演示工作副本）' : '已取消推荐');
}

/** 本地关键词筛选：明确标注非向量检索 */
const stats = demoMaterialStats;

function previewNotIntegrated() {
  window.$message?.info('演示环境：在线预览走后端文件接口，暂未接入');
}

function downloadNotIntegrated() {
  window.$message?.warning('下载依赖后端授权下载 URL：暂未接入，不伪装下载成功');
}

function editTagsNotIntegrated() {
  window.$message?.info('标签/有效期编辑依赖素材写接口：暂未接入');
}

</script>

<template>
  <div class="flex-col gap-16px overflow-auto p-16px">
    <PageHeader title="企业素材库" subtitle="集中管理企业资质、案例、人员、证书、制度文档与可复用投标素材">
      <template #actions>
        <DemoBadge />
        <ElUpload :show-file-list="false" :before-upload="onLocalUpload" accept=".pdf,.png,.jpg,.doc,.docx,.xlsx">
          <ElButton type="primary" icon="mdi:upload">上传素材</ElButton>
        </ElUpload>
        <ElButton icon="mdi:folder-plus" @click="createDirectory">新建目录</ElButton>
      </template>
    </PageHeader>

    <ElCard shadow="never">
      <div class="flex flex-wrap items-center gap-12px">
        <ElInput v-model="filters.keyword" placeholder="搜索素材名称、标签、内容" clearable class="w-240px">
          <template #prefix><SvgIcon icon="mdi:magnify" /></template>
        </ElInput>
        <ElSelect v-model="filters.directory" placeholder="素材分类" clearable class="w-170px">
          <ElOption v-for="d in directories" :key="d.id" :label="d.name" :value="d.name" />
        </ElSelect>
        <ElSelect v-model="filters.status" placeholder="有效状态" clearable class="w-130px">
          <ElOption label="有效" value="有效" />
          <ElOption label="即将过期" value="即将过期" />
          <ElOption label="已过期" value="已过期" />
        </ElSelect>
        <ElDatePicker v-model="filters.dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" class="w-240px" />
        <ElButton @click="resetFilters">重置</ElButton>
      </div>
    </ElCard>

    <div class="grid gap-16px grid-cols-2 xl:grid-cols-4">
      <ElCard shadow="never"><div class="text-13px c-secondary">素材总数</div><div class="text-24px font-600">{{ stats.total.toLocaleString() }}</div><div class="text-12px c-secondary">演示样例 {{ materials.length }} 条已展开</div></ElCard>
      <ElCard shadow="never"><div class="text-13px c-secondary">即将过期</div><div class="text-24px font-600 c-warning">{{ materials.filter(m => expiryStatus(m) === '即将过期').length }}</div></ElCard>
      <ElCard shadow="never"><div class="text-13px c-secondary">高频引用</div><div class="text-24px font-600 c-success">{{ materials.filter(m => m.refs >= 100).length }}</div></ElCard>
      <ElCard shadow="never"><div class="text-13px c-secondary">本月新增</div><div class="text-24px font-600 c-primary">{{ stats.monthNew }}</div></ElCard>
    </div>

    <ElCard shadow="never">
      <ElTabs v-model="activeTab">
        <ElTabPane v-for="t in materialCategoryTabs" :key="t.key" :label="t.label" :name="t.key" />
      </ElTabs>

      <div class="grid gap-16px xl:grid-cols-[230px_1fr_320px]">
        <!-- 目录树 -->
        <div class="min-w-0">
          <div class="mb-6px text-13px font-600 c-secondary">分类导航</div>
          <div
            class="flex cursor-pointer items-center justify-between rounded-8px px-10px py-7px text-14px"
            :class="!activeDirectory ? 'bg-primary/10 c-primary font-500' : 'hover:bg-gray-1'"
            @click="activeDirectory = ''; filters.directory = ''"
          >
            <span>全部素材</span>
            <ElTag size="small" round>{{ materials.length }}</ElTag>
          </div>
          <template v-for="d in directories" :key="d.id">
            <div
              class="flex cursor-pointer items-center justify-between rounded-8px px-10px py-7px text-14px"
              :class="activeDirectory === d.name ? 'bg-primary/10 c-primary font-500' : 'hover:bg-gray-1'"
              @click="activeDirectory = d.name; filters.directory = d.name"
            >
              <span class="truncate"><SvgIcon icon="mdi:folder-outline" class="mr-4px" />{{ d.name }}</span>
              <ElTag size="small" round>{{ d.count }}</ElTag>
            </div>
            <div
              v-for="c in d.children || []"
              :key="c.id"
              class="flex cursor-pointer items-center justify-between rounded-8px px-20px py-6px text-13px"
              :class="activeDirectory === `${d.name}/${c.name}` ? 'bg-primary/10 c-primary font-500' : 'hover:bg-gray-1'"
              @click="activeDirectory = `${d.name}/${c.name}`; filters.directory = `${d.name}/${c.name}`"
            >
              <span class="truncate">{{ c.name }}</span>
              <ElTag size="small" round>{{ c.count }}</ElTag>
            </div>
          </template>
        </div>

        <!-- 素材列表 -->
        <div class="min-w-0">
          <div class="mb-8px flex flex-wrap items-center justify-between gap-8px">
            <span class="text-13px c-secondary">素材列表（{{ filtered.length }} 条 · 本地关键词筛选，非向量检索）</span>
            <ElRadioGroup v-model="viewMode" size="small">
              <ElRadioButton value="table"><SvgIcon icon="mdi:view-list" /></ElRadioButton>
              <ElRadioButton value="card"><SvgIcon icon="mdi:view-grid" /></ElRadioButton>
            </ElRadioGroup>
          </div>

          <ElTable
            v-if="viewMode === 'table'"
            v-loading="loading"
            :data="filtered"
            size="small"
            style="width: 100%"
            highlight-current-row
            @row-click="selectMaterial"
          >
            <ElTableColumn prop="name" label="素材名称" min-width="200" show-overflow-tooltip />
            <ElTableColumn prop="type" label="类型" width="90" />
            <ElTableColumn label="标签" min-width="130">
              <template #default="{ row }">
                <ElTag v-for="t in row.tags.slice(0, 2)" :key="t" size="small" class="mr-4px">{{ t }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="updatedAt" label="更新时间" width="150" />
            <ElTableColumn label="有效期" width="100">
              <template #default="{ row }">
                <ElTag size="small" :type="statusTag(expiryStatus(row))">{{ expiryStatus(row) }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="refs" label="引用" width="60" />
            <ElTableColumn label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <ElButton link type="primary" size="small" @click.stop="selectMaterial(row)">查看</ElButton>
                <ElButton link size="small" @click.stop="openAddRef(row)">引用</ElButton>
              </template>
            </ElTableColumn>
          </ElTable>

          <div v-else class="grid gap-10px sm:grid-cols-2 lg:grid-cols-3">
            <div
              v-for="m in filtered"
              :key="m.id"
              class="cursor-pointer rounded-10px border border-solid border-gray-2 p-10px hover:border-primary/50"
              :class="activeMaterial?.id === m.id ? 'border-primary bg-primary/5' : ''"
              @click="selectMaterial(m)"
            >
              <div class="flex items-center gap-6px">
                <SvgIcon
                  :icon="m.previewKind === 'pdf' ? 'mdi:file-pdf-box' : m.previewKind === 'image' ? 'mdi:file-image-box' : 'mdi:file-word-box'"
                  class="text-18px c-primary"
                />
                <span class="min-w-0 flex-1 truncate text-13px font-500">{{ m.name }}</span>
              </div>
              <div class="mt-4px text-12px c-secondary">{{ m.type }} · 引用 {{ m.refs }} 次</div>
              <div class="mt-4px"><ElTag size="small" :type="statusTag(expiryStatus(m))">{{ expiryStatus(m) }}</ElTag></div>
            </div>
          </div>
        </div>



        <!-- 素材详情 -->
        <div class="min-w-0">
          <div class="mb-6px text-13px font-600 c-secondary">素材详情</div>
          <template v-if="activeMaterial">
            <ElCard shadow="never">
              <div class="flex items-center gap-8px">
                <SvgIcon icon="mdi:file-pdf-box" class="text-26px c-danger" />
                <div class="min-w-0 flex-1">
                  <div class="truncate text-14px font-600">{{ activeMaterial.name }}</div>
                  <div class="text-12px c-secondary">{{ activeMaterial.updatedAt }} · {{ activeMaterial.sizeLabel }}</div>
                </div>
                <ElButton link type="warning" size="small" @click="toggleRecommend(activeMaterial)">
                  {{ activeMaterial.recommended ? '取消推荐' : '设为推荐' }}
                </ElButton>
              </div>

              <!-- 预览区：pdf/image 显示示意框；docx 无转换预览时显示文件信息 -->
              <div
                v-if="activeMaterial.previewKind === 'pdf' || activeMaterial.previewKind === 'image'"
                class="mt-10px flex-col-center gap-6px rounded-10px border border-dashed border-solid border-gray-3 py-40px"
              >
                <SvgIcon :icon="activeMaterial.previewKind === 'image' ? 'mdi:image-area' : 'mdi:file-pdf-box'" class="text-48px c-secondary" />
                <span class="text-12px c-secondary">预览样例（{{ activeMaterial.previewKind === 'image' ? '图片' : 'PDF' }}）· 实际预览走后端文件接口</span>
                <div class="flex gap-8px">
                  <ElButton size="small" @click="previewNotIntegrated">打开预览</ElButton>
                  <ElButton size="small" @click="downloadNotIntegrated">下载</ElButton>
                </div>
              </div>
              <ElAlert v-else class="mt-10px" type="info" :closable="false" title="该格式暂无可预览文件（未接入转换服务），可查看文件信息与下载入口。" />

              <ElDescriptions class="mt-10px" :column="1" border size="small">
                <ElDescriptionsItem label="素材类型">{{ activeMaterial.type }}</ElDescriptionsItem>
                <ElDescriptionsItem label="所属分类">{{ activeMaterial.directory }}</ElDescriptionsItem>
                <ElDescriptionsItem label="适用项目">{{ activeMaterial.projects }}</ElDescriptionsItem>
                <ElDescriptionsItem label="有效期至">{{ activeMaterial.expiry || '长期有效' }}</ElDescriptionsItem>
                <ElDescriptionsItem label="引用次数">{{ activeMaterial.refs }}</ElDescriptionsItem>
                <ElDescriptionsItem label="版本">{{ activeMaterial.version }}</ElDescriptionsItem>
                <ElDescriptionsItem label="上传人">{{ activeMaterial.uploader }}</ElDescriptionsItem>
                <ElDescriptionsItem label="标签">
                  <ElTag v-for="t in activeMaterial.tags" :key="t" size="small" class="mr-4px">{{ t }}</ElTag>
                </ElDescriptionsItem>
              </ElDescriptions>

              <div class="mt-10px text-13px font-600">最近使用记录</div>
              <div class="mt-6px flex flex-col gap-6px">
                <div v-for="(u, i) in activeMaterial.usage" :key="i" class="rounded-8px bg-gray-1 px-10px py-8px text-12px">
                  <div>{{ u.scene }}</div>
                  <div class="mt-2px c-secondary">{{ u.user }} · {{ u.time }}</div>
                </div>
                <span v-if="!activeMaterial.usage.length" class="text-12px c-secondary">暂无使用记录</span>
              </div>

              <div class="mt-12px flex gap-8px">
                <ElButton type="primary" size="small" @click="openAddRef(activeMaterial)">加入生成引用</ElButton>
                <ElButton size="small" @click="editTagsNotIntegrated">编辑标签/有效期</ElButton>
              </div>
            </ElCard>
          </template>
          <ElEmpty v-else description="选择左侧素材查看详情" />
        </div>

      </div>
    </ElCard>

    <MaterialSelectDrawer v-model:visible="refDrawerVisible" :materials="materials" @confirm="addToGenerate" />
  </div>
</template>
