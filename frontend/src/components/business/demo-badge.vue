<script setup lang="ts">
defineProps<{
  /** 可选：预留的标签前缀文案 */
  label?: string;
  /** 演示数据的标识文案 */
  text?: string;
}>();
</script>

<template>
  <ElTooltip content="当前为演示数据，非真实业务统计" placement="top">
    <ElTag size="small" type="warning" effect="plain" round>
      {{ text || '演示数据' }}
    </ElTag>
  </ElTooltip>
</template>
