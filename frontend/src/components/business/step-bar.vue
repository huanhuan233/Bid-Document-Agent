<script setup lang="ts">
defineProps<{
  steps: { title: string; desc: string }[];
  /** 当前步骤（0 基） */
  active: number;
}>();
</script>

<template>
  <div class="flex items-start gap-8px overflow-x-auto">
    <template v-for="(step, idx) in steps" :key="step.title">
      <div class="flex min-w-0 flex-1 items-center gap-10px">
        <div class="flex min-w-0 items-center gap-10px">
          <ElTag
            :type="idx < active ? 'success' : idx === active ? 'primary' : 'info'"
            :effect="idx === active ? 'dark' : 'plain'"
            size="large"
            round
            class="shrink-0"
          >
            {{ idx + 1 }}
          </ElTag>
          <div class="min-w-0">
            <div class="truncate text-14px font-500" :class="idx === active ? 'c-primary' : 'c-text'">{{ step.title }}</div>
            <div class="truncate text-12px c-secondary">{{ step.desc }}</div>
          </div>
        </div>
      </div>
      <SvgIcon v-if="idx < steps.length - 1" icon="mdi:chevron-right" class="mt-8px shrink-0 text-18px c-secondary" />
    </template>
  </div>
</template>
