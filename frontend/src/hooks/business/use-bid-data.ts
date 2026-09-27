import { onScopeDispose, ref, shallowRef } from 'vue';
import type { Ref } from 'vue';
import type { BidResult } from '@/service/providers/bid';

/**
 * 统一加载 BidProvider 数据：区分 ok / not-integrated / error 三态，
 * 页面据此渲染内容、未接入或错误状态，不静默回退演示数据。
 */
export function useBidData<T>(fetcher: () => Promise<BidResult<T>>, initial: T) {
  const data = ref(initial) as Ref<T>;
  const loading = ref(false);
  const state = ref<'ok' | 'not-integrated' | 'error'>('ok');
  const message = ref('');

  async function load() {
    loading.value = true;
    try {
      const res = await fetcher();
      state.value = res.state;
      if (res.state === 'ok') {
        data.value = res.data;
      } else {
        message.value = res.message;
      }
    } catch (e) {
      state.value = 'error';
      message.value = e instanceof Error ? e.message : '请求失败';
    } finally {
      loading.value = false;
    }
  }

  load();

  return { data, loading, state, message, load };
}

/** ECharts 实例管理：随作用域自动 dispose，避免标签页切换重复挂载 */
export function useEcharts() {
  const el = shallowRef<HTMLElement | null>(null);
  let chart: any = null;

  async function render(options: Record<string, unknown>) {
    if (!el.value) return;
    const echarts = await import('echarts');
    if (!chart) {
      chart = echarts.init(el.value);
    }
    chart.setOption(options, true);
  }

  function resize() {
    chart?.resize();
  }

  onScopeDispose(() => {
    if (chart) {
      chart.dispose();
      chart = null;
    }
  });

  return { el, render, resize };
}
