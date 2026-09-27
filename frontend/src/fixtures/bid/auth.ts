/** 演示模式当前用户（仅在 VITE_BID_DATA_MODE=demo 时使用，不请求后端） */
export const demoAuthUser = {
  userId: 'demo-user-001',
  userName: '张三',
  roles: ['R_SUPER'] as string[],
  buttons: [] as string[]
};
