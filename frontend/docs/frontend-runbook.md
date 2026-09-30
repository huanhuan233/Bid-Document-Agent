# 智标云前端 Runbook

## 环境要求

- Node >= 20.19，pnpm >= 8.7（保留现有 workspace 与锁文件版本）。

## 启动（演示模式，默认）

```bash
cd frontend
pnpm i          # 首次
pnpm dev        # http://localhost:10032（端口固定，strictPort）
```

## 数据模式运行时切换（Mock / 真实接口）

- **头部开关**（登录后右上角，「Mock 数据 / 真实接口」）：点击切换并刷新页面，立即生效，无需改 env / 重启服务。
- **登录页**：真实接口模式下底部有「演示模式进入（Mock 数据，不连后端）」按钮——后端没启动也能进系统开发页面。
- 优先级：浏览器 localStorage 手动选择（`zhibiao-data-mode`）> `.env` 的 `VITE_BID_DATA_MODE` > demo。
- 实现：`src/service/providers/bid/data-mode.ts`（模式解析与持久化）+ `bidProvider` 运行时 Proxy（按调用时刻转发到 demo/api 实现）。

## 演示模式（Mock，假前端）

- 登录页任意非空用户名/密码即可进入（不请求后端；或直接点「演示模式进入」按钮）。
- 所有页面使用 `src/fixtures/bid` 演示数据，界面持续显示「演示数据」。
- 演示写操作（待办勾选、消息已读、生成草稿、标书解析页上传文件入库）保存在 localStorage
  `zhibiao-demo-state-v1`；原系统设置页的「重置演示数据」入口已随该演示页移除，
  需要重置时在浏览器控制台执行 `localStorage.removeItem('zhibiao-demo-state-v1')` 后刷新。

## API 联调模式

```bash
# 1. 默认模式由 .env 的 VITE_BID_DATA_MODE 控制（当前为 api）；
#    也可不改 env，启动后在头部开关处切换
# 2. .env.test / .env.prod：VITE_SERVICE_BASE_URL=http://<后端地址>/api/v1
#    （必须带 /api/v1 前缀：前端请求 url 是 '/auth/login' 这类相对路径）
# 3. 启动后端（Spring Boot，默认 8080，开启 CORS/会话 Cookie）
pnpm dev
```

- 登录走 `POST /api/v1/auth/login`（服务端会话，兼容 Bearer）。
- 未实现的业务接口会显示「未接入」空状态，绝不静默回退演示数据。
- 代理：dev 模式默认 `VITE_HTTP_PROXY=Y`，经 `/proxy-default` 转发到
  `VITE_SERVICE_BASE_URL`（即 `http://localhost:8080/api/v1`），代理会自动拼接目标路径。
- **修改 `.env*` 后必须重启 `pnpm dev`**（代理目标与环境变量只在启动时读取）。

## 构建与检查

```bash
pnpm typecheck   # vue-tsc --noEmit
pnpm build       # vite build --mode prod（同时触发 Elegant Router 重新生成路由文件）
pnpm lint        # 注意：脚本带 --fix，会自动修复
```

**当前验证状态（本次交付实测）**：`pnpm typecheck` ✅ 通过（0 错误）；`pnpm build`（prod 模式）✅ 成功；
`pnpm dev` ✅ 在 http://localhost:10032 正常启动并返回 HTTP 200。

## 常见问题

- **登录报 `Request failed with status code 403`（后端返回 `A0403 无权访问该资源`）**：
  请求路径少了 `/api/v1` 前缀，命中了后端 CSRF 拦截（登录路径白名单是 `/api/v1/auth/login`）。
  检查 `.env.test` 的 `VITE_SERVICE_BASE_URL=http://localhost:8080/api/v1`，改完**重启 `pnpm dev`**。
  该 403 在后端日志中无任何输出（CsrfFilter 在进入 Controller 前就拒绝），属正常现象。
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
