<script setup lang="ts">
import { ref } from 'vue';
import { currentDataMode, setDataMode, type BidDataMode } from '@/service/providers/bid/data-mode';

defineOptions({ name: 'DataModeSwitch' });

const mode = ref<BidDataMode>(currentDataMode());

/** 切换前确认：确认后持久化并刷新页面（provider 按新模式重新加载） */
async function onBeforeChange(): Promise<boolean> {
  const next: BidDataMode = mode.value === 'demo' ? 'api' : 'demo';
  const content =
    next === 'demo'
      ? '切换到「Mock 数据」：纯前端假数据，不请求后端，页面将刷新。'
      : '切换到「真实接口」：所有数据请求后端 /api/v1，未实现的接口显示「未接入」，页面将刷新。';

  try {
    await window.$messageBox?.confirm(content, '切换数据模式', {
      type: 'warning',
      confirmButtonText: '切换并刷新',
      cancelButtonText: '取消'
    });
  } catch {
    return false; // 用户取消
  }

  setDataMode(next);
  window.location.reload();
  return true;
}
</script>

<template>
  <ElTooltip placement="bottom" content="演示 = 纯前端 Mock 数据（假前端）；接口 = 真实后端数据">
    <div class="flex-y-center gap-6px pr-4px">
      <span class="text-12px whitespace-nowrap" :class="mode === 'demo' ? 'text-warning' : 'text-secondary'">
        {{ mode === 'demo' ? 'Mock 数据' : '真实接口' }}
      </span>
      <ElSwitch size="small" :model-value="mode === 'api'" :before-change="onBeforeChange" />
    </div>
  </ElTooltip>
</template>
