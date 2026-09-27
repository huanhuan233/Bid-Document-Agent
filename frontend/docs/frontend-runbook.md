# 智标云前端 Runbook

## 环境要求

- Node >= 20.19，pnpm >= 8.7（保留现有 workspace 与锁文件版本）。

## 启动（演示模式，默认）

```bash
cd frontend
pnpm i          # 首次
pnpm dev        # http://localhost:10032（端口固定，strictPort）
```

- 登录页任意非空用户名/密码即可进入（仅 demo 模式生效，不请求后端）。
- 所有页面使用 `src/fixtures/bid` 演示数据，界面持续显示「演示数据」。
- 演示写操作（待办勾选、消息已读、生成草稿）保存在 localStorage
  `zhibiao-demo-state-v1`；系统设置页提供「重置演示数据」（或清除该 key）。

## API 联调模式

```bash
# 1. 修改 .env：VITE_BID_DATA_MODE=api
# 2. 修改 .env.test / .env.prod：VITE_SERVICE_BASE_URL=http://<后端地址>
# 3. 启动后端（Spring Boot，默认 8080，开启 CORS/会话 Cookie）
pnpm dev
```

- 登录走 `POST /api/v1/auth/login`（服务端会话，兼容 Bearer）。
- 未实现的业务接口会显示「未接入」空状态，绝不静默回退演示数据。
- 代理：dev 模式默认 `VITE_HTTP_PROXY=Y`，经 `/proxy-default` 转发，避免 `/api/v1` 拼接两遍。

## 构建与检查

```bash
pnpm typecheck   # vue-tsc --noEmit
pnpm build       # vite build --mode prod（同时触发 Elegant Router 重新生成路由文件）
pnpm lint        # 注意：脚本带 --fix，会自动修复
```

**当前验证状态（本次交付实测）**：`pnpm typecheck` ✅ 通过（0 错误）；`pnpm build`（prod 模式）✅ 成功；
`pnpm dev` ✅ 在 http://localhost:10032 正常启动并返回 HTTP 200。

## 常见问题

- **新增页面后路由未生成**：路由文件由 Vite 的 elegant-router 插件在 dev/build 时生成，
  `pnpm gen-route` 仅是新建页面脚手架（交互式）。新增 views 文件后跑一次 `pnpm dev` 或 `pnpm build`。
- **elegant-router 目录结构硬规则（重要）**：插件只把每个目录下的 `index.vue` 与 `[param].vue` 识别为页面；
  同级平铺的 `records.vue` 这类文件会被忽略。且目录一旦包含子路由（子目录页面），
  该目录的 `index.vue` **不会**作为父级视图渲染（父级只得到 `layout.base`）。
  因此详情/记录页一律放在 `src/views` **顶层目录**（如 `parse-result/[id].vue`、`review-records/index.vue`），
  并通过 meta `hideInMenu + activeMenu` 挂到对应菜单，禁止放到功能目录的子目录里。
- **重建路由文件**：插件合并旧 `routes.ts` 时若结构差异过大会崩溃
  （`Cannot read properties of undefined (reading 'replace')`）。
  恢复方法：删除 `src/router/elegant/routes.ts`、`src/router/elegant/imports.ts`、
  `src/typings/elegant-router.d.ts` 后重跑 `pnpm dev/build` 重新生成，再补回 meta 定制。
- **路由 meta 定制**：直接修改 `src/router/elegant/routes.ts` 中生成的 meta（标题/图标/hideInMenu/activeMenu），
  插件再生成时会保留已定制字段；route.* 文案需同步补充 `src/locales/langs/zh-cn.ts` 与 `en-us.ts`
  （含 `-` 的 key 必须加引号，如 `'parse-records':`）。
- **路由跳转**：业务代码统一使用 `useRouterPush()` 返回的 `routerPush({ key, params?, query? })`，
  该封装在 `src/hooks/common/router.ts` 中扩展自 `router.push`。
- **演示模式与真实模式切换**：清理 `zhibiao-demo-state-v1` 与演示 token（`demo-` 前缀），
  重新登录；正式构建默认 api 模式，不得启用演示登录。

## 浏览器验证说明

本次交付环境为无浏览器沙箱，已完成 `pnpm typecheck` 与 `pnpm build` 验证；
1366/1600/1920 宽度的截图核对需在本地浏览器执行（页面布局采用 Grid/Flex + 面板独立滚动，
窄屏下右栏自动折行、表格内部横向滚动，无整页横向溢出设计）。
