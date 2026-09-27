<script setup lang="ts">
defineProps<{
  label: string;
  value: string | number;
  delta?: string;
  deltaUp?: boolean;
  icon?: string;
  color?: string;
  /** 值后缀（如 %） */
  unit?: string;
}>();
</script>

<template>
  <ElCard shadow="never" class="stat-card">
    <div class="flex items-center gap-14px">
      <div
        class="h-48px w-48px flex shrink-0 items-center justify-center rounded-10px"
        :style="{ background: `color-mix(in srgb, ${color || 'var(--el-color-primary)'} 12%, transparent)` }"
      >
        <SvgIcon :icon="icon || 'mdi:chart-box-outline'" :style="{ color: color || 'var(--el-color-primary)' }" class="text-26px" />
      </div>
      <div class="min-w-0 flex-1">
        <div class="text-13px c-secondary">{{ label }}</div>
        <div class="mt-2px text-26px font-600 leading-32px c-text">
          {{ value }}<span v-if="unit" class="text-15px">{{ unit }}</span>
        </div>
        <div v-if="delta" class="mt-2px text-12px" :class="deltaUp ? 'c-success' : 'c-danger'">
          {{ delta }}
          <span :class="deltaUp ? 'c-success' : 'c-danger'">{{ deltaUp ? '↑' : '↓' }}</span>
        </div>
      </div>
    </div>
  </ElCard>
</template>

<style lang="scss" scoped>
.stat-card {
  --el-card-border-radius: 12px;
}
</style>
