import type { Bid } from '@/typings/bid';

/**
 * 运行时数据模式：demo（纯前端 Mock，假前端）/ api（真实后端）。
 * 优先级：localStorage 手动切换 > .env 的 VITE_BID_DATA_MODE > demo。
 * 可通过头部开关或登录页「演示模式进入」运行时切换，无需修改 env / 重启服务。
 */
export type BidDataMode = 'demo' | 'api';

const LS_KEY = 'zhibiao-data-mode';

/** 读取用户手动切换并持久化的模式；未手动切换过时返回 null */
export function readStoredDataMode(): BidDataMode | null {
  try {
    const raw = localStorage.getItem(LS_KEY);
    return raw === 'demo' || raw === 'api' ? raw : null;
  } catch {
    return null;
  }
}

/** 当前生效的数据模式（每次调用实时解析，支持运行时切换） */
export function currentDataMode(): BidDataMode {
  const stored = readStoredDataMode();
  if (stored) return stored;
  return (import.meta.env.VITE_BID_DATA_MODE || 'demo') === 'api' ? 'api' : 'demo';
}

/** 是否处于演示（Mock）模式：登录与业务数据均不请求后端 */
export function isDemoActive(): boolean {
  return currentDataMode() === 'demo';
}

/** 持久化数据模式切换（写入 localStorage，刷新后仍生效） */
export function setDataMode(mode: BidDataMode): void {
  try {
    localStorage.setItem(LS_KEY, mode);
  } catch {
    // ignore quota errors
  }
}
