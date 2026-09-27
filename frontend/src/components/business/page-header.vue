<script setup lang="ts">
defineProps<{
  title: string;
  subtitle?: string;
}>();
</script>

<template>
  <div class="mb-16px flex items-start justify-between gap-16px">
    <div class="min-w-0">
      <h1 class="m-0 text-24px font-600 c-text">{{ title }}</h1>
      <p v-if="subtitle" class="mt-6px mb-0 text-13px c-secondary">{{ subtitle }}</p>
      <slot name="extra"></slot>
    </div>
    <div class="flex shrink-0 items-center gap-12px">
      <slot name="actions"></slot>
    </div>
  </div>
</template>
