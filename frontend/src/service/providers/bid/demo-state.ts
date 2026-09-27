import type { Bid } from '@/typings/bid';

/**
 * 演示写操作的独立命名空间（localStorage 持久化）。
 * 仅在 VITE_BID_DATA_MODE=demo 下读写；不保存真实证书、敏感正文与会话凭据。
 */
const KEY = 'zhibiao-demo-state-v1';

export interface DemoPersistedState {
  todos?: Bid.TodoVM[];
  notices?: Bid.NoticeVM[];
  draft?: Bid.GenerationDraftVM | null;
}

function isDemoMode() {
  return (import.meta.env.VITE_BID_DATA_MODE || 'demo') === 'demo';
}

export function loadDemoState(): DemoPersistedState {
  if (!isDemoMode()) return {};
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as DemoPersistedState) : {};
  } catch {
    return {};
  }
}

export function saveDemoState(patch: DemoPersistedState) {
  if (!isDemoMode()) return;
  const next = { ...loadDemoState(), ...patch };
  try {
    localStorage.setItem(KEY, JSON.stringify(next));
  } catch {
    // ignore quota errors in demo mode
  }
}

export function resetDemoState() {
  if (!isDemoMode()) return;
  try {
    localStorage.removeItem(KEY);
  } catch {
    // ignore
  }
}
