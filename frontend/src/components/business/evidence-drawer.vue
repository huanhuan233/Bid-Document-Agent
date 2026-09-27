<script setup lang="ts">
import { computed } from 'vue';
import type { Bid } from '@/typings/bid';

const props = defineProps<{
  visible: boolean;
  evidence: Bid.EvidenceRefVM | null;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
}>();

const localVisible = computed({
  get: () => props.visible,
  set: v => emit('update:visible', v)
});
</script>

<template>
  <ElDrawer v-model="localVisible" title="证据查看" size="440px">
    <template v-if="evidence">
      <ElDescriptions :column="1" border>
        <ElDescriptionsItem label="来源文件">{{ evidence.fileLabel }}</ElDescriptionsItem>
        <ElDescriptionsItem label="版本">{{ evidence.version }}</ElDescriptionsItem>
        <ElDescriptionsItem label="定位">
          <template v-if="evidence.page !== null && evidence.page !== undefined">
            第 {{ evidence.page }} 页
            <span v-if="evidence.location" class="c-secondary">（{{ evidence.location }}）</span>
          </template>
          <template v-else>
            <ElTag type="info" size="small">未找到相关证据页码</ElTag>
          </template>
        </ElDescriptionsItem>
      </ElDescriptions>

      <div class="mt-16px text-13px font-600 c-text">证据片段</div>
      <div class="mt-8px rounded-8px bg-primary/5 p-12px text-13px leading-22px c-text">
        {{ evidence.quote }}
      </div>

      <div class="mt-16px text-13px c-secondary">
        说明：证据回链基于演示样例数据；无页码的证据表示在原文中未能定位，不做推测。
      </div>
    </template>
    <ElEmpty v-else description="未找到相关证据" />
  </ElDrawer>
</template>
