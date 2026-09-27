# 智标云前端页面与路由清单

> 路由基于 Elegant Router（文件生成），一级菜单 8 个，与 8 张原型一一对应。

## 一级菜单与页面

| # | 一级菜单 | 页面 | 路由 | 路由名 | 对应原型 |
|---|---------|------|------|--------|---------|
| 1 | 工作台 | 投标工作台 | `/home` | `home` | 工作台.png |
| 2 | 标书解析 | 标书解析中心 | `/bid-parse` | `bid-parse` | 标书解析.png |
| 3 | 标书生成 | 标书生成中心 | `/bid-generate` | `bid-generate` | 标书生成.png |
| 4 | 标书审查 | 标书审查中心 | `/bid-review` | `bid-review` | 标书检查.png |
| 5 | 模板中心 | 模板列表 | `/templates` | `templates` | 模板中心.png |
| 6 | 知识库 | 企业素材库 | `/materials` | `materials` | 素材库.png |
| 7 | 审查报告 | 报告列表 | `/reports` | `reports` | 审查报告详情.png（列表为落点） |
| 8 | 系统设置 | 用户/角色/菜单管理 | `/manage/user` 等 | `manage_user` 等 | 复用 Soybean 现有页面 |

## 隐藏详情/辅助入口（不占一级菜单，父级菜单保持高亮）

| 页面 | 路由 | 路由名 | 说明 |
|------|------|--------|------|
| 项目列表 | `/projects` | `projects` | 工作台「查看全部」落点（菜单隐藏） |
| 项目详情 | `/project-detail/:id` | `project-detail` | 刷新可访问，返回保留列表 |
| 解析记录 | `/parse-records` | `parse-records` | 统一表格模式（菜单隐藏） |
| 解析结果详情 | `/parse-result/:id` | `parse-result` | 字体/页面布局/版式结构重点页 |
| 生成记录 | `/generate-records` | `generate-records` | 含常规/修订方式列（菜单隐藏） |
| 审查记录 | `/review-records` | `review-records` | 含报告入口（菜单隐藏） |
| 报告详情 | `/report-detail/:id` | `report-detail` | 总览/问题清单/评分点覆盖/一致性/证据回链 |

## 框架示例菜单（隐藏，不删除源码）

`about`、`alova`、`function`、`multi-menu`、`plugin`、`user-center`、`document`、`exception`
均设置 `hideInMenu`，页面源码保留供复用（编辑器、PDF、图表等示例仍可参考）。

## 数据分层

- 页面 `src/views/**`：只调用 `bidProvider`，不散落请求代码。
- 数据服务 `src/service/providers/bid/`：`BidProvider` 接口 + demo/api 双实现。
  模式由 `VITE_BID_DATA_MODE=demo|api` 控制。
- 演示数据 `src/fixtures/bid/`：项目、解析文件、条款、版式、章节、整改意见、问题、
  报告、模板、素材互相关联（同一项目族 P-2026-001…005 / PF-001…003 /
  RV-2026-001 / RR-2026-005 / IS-001…012 / OP-001…004）。
- 演示写操作持久化在独立命名空间 `zhibiao-demo-state-v1`（localStorage），
  含待办勾选、消息已读、生成草稿；不保存真实证书/敏感正文/会话凭据。
- 类型 `src/typings/bid.d.ts`：Bid.* ViewModel 与 DTO 分离，ID 一律字符串。
