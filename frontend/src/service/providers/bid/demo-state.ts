import type { Bid } from '@/typings/bid';
import { isDemoActive } from './data-mode';

/**
 * 演示写操作的独立命名空间（localStorage 持久化）。
 * 仅在演示（Mock）模式下读写；不保存真实证书、敏感正文与会话凭据。
 */
const KEY = 'zhibiao-demo-state-v1';

export interface DemoPersistedState {
  todos?: Bid.TodoVM[];
  notices?: Bid.NoticeVM[];
  draft?: Bid.GenerationDraftVM | null;
  /** 解析中心上传入库的文件（含演示预置文件的全量快照） */
  parseFiles?: Bid.ParseFileVM[];
}

export function loadDemoState(): DemoPersistedState {
  if (!isDemoActive()) return {};
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as DemoPersistedState) : {};
  } catch {
    return {};
  }
}

export function saveDemoState(patch: DemoPersistedState) {
  if (!isDemoActive()) return;
  const next = { ...loadDemoState(), ...patch };
  try {
    localStorage.setItem(KEY, JSON.stringify(next));
  } catch {
    // ignore quota errors in demo mode
  }
}

export function resetDemoState() {
  if (!isDemoActive()) return;
  try {
    localStorage.removeItem(KEY);
  } catch {
    // ignore
  }
}
