<script setup lang="ts">
defineProps<{
  message: string;
  /** 按钮文案；为空则不显示按钮 */
  actionText?: string;
}>();

const emit = defineEmits<{
  action: [];
}>();
</script>

<template>
  <div class="flex-col-center gap-8px py-32px">
    <SvgIcon icon="mdi:plug-off-outline" class="text-40px c-secondary" />
    <p class="m-0 text-14px c-secondary">{{ message }}</p>
    <ElButton v-if="actionText" size="small" @click="emit('action')">{{ actionText }}</ElButton>
  </div>
</template>
