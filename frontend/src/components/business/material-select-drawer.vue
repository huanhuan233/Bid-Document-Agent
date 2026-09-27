<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import type { Bid } from '@/typings/bid';

const props = defineProps<{
  visible: boolean;
  materials: Bid.MaterialVM[];
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  confirm: [selected: { id: string; name: string }[]];
}>();

const localVisible = computed({
  get: () => props.visible,
  set: v => emit('update:visible', v)
});

const keyword = ref('');
const checkedIds = ref<string[]>([]);

watch(
  () => props.visible,
  v => {
    if (v) {
      keyword.value = '';
      checkedIds.value = [];
    }
  }
);

const filtered = computed(() =>
  props.materials.filter(m => (keyword.value ? m.name.includes(keyword.value) || m.tags.some(t => t.includes(keyword.value)) : true))
);

function handleConfirm() {
  const selected = props.materials
    .filter(m => checkedIds.value.includes(m.id))
    .map(m => ({ id: m.id, name: m.name }));
  emit('confirm', selected);
  localVisible.value = false;
}
</script>

<template>
  <ElDrawer v-model="localVisible" title="导入企业素材" size="520px">
    <ElInput v-model="keyword" placeholder="按名称或标签筛选" clearable class="mb-12px">
      <template #prefix>
        <SvgIcon icon="mdi:magnify" />
      </template>
    </ElInput>

    <ElEmpty v-if="filtered.length === 0" description="未找到匹配素材" />

    <ElCheckboxGroup v-else v-model="checkedIds" class="flex flex-col gap-8px">
      <div
        v-for="m in filtered"
        :key="m.id"
        class="flex items-center gap-8px rounded-8px border border-solid border-gray-200 px-12px py-8px"
      >
        <ElCheckbox :value="m.id">
          <span class="text-14px">{{ m.name }}</span>
          <ElTag size="small" type="info" class="ml-8px">{{ m.type }}</ElTag>
          <span class="ml-8px text-12px c-secondary">引用 {{ m.refs }} 次</span>
        </ElCheckbox>
      </div>
    </ElCheckboxGroup>

    <template #footer>
      <div class="flex justify-end gap-8px">
        <ElButton @click="localVisible = false">取消</ElButton>
        <ElButton type="primary" :disabled="checkedIds.length === 0" @click="handleConfirm">
          加入引用（{{ checkedIds.length }}）
        </ElButton>
      </div>
    </template>
  </ElDrawer>
</template>
