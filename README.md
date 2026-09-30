# 智标云（bid-platform）· 标书智能平台

> 智标云 · 让投标更简单。前端 Vue3 + Element Plus（Soybean Admin），后端 Spring Boot 3 模块化单体，
> Dify 工作流作为外挂智能引擎（当前为显式 mock 模式）。

## 目录结构

```
标书智能体/
├── frontend/                 # 前端：Vue3 + Vite + Element Plus + UnoCSS（pnpm workspace）
│   └── docs/frontend-runbook.md   # 前端详细 Runbook（路由约定、常见问题）
├── backend/                  # 后端：Spring Boot 3.5 / Java 21 / MyBatis-Plus / Flyway
│   ├── src/main/java/com/zhibiao/platform/
│   │   ├── PlatformApplication.java     # 启动类
│   │   ├── identity/                    # 用户/角色/权限/登录会话
│   │   ├── project/ file/ asset/ library/ audit/   # 业务模块
│   │   └── shared/                      # 安全配置、统一响应、异常、对象存储端口
│   └── src/main/resources/
│       ├── application.yml              # 主配置（全部支持 ZB_* 环境变量覆盖）
│       └── db/migration/V1~V7__*.sql    # Flyway 建表与种子数据
├── deploy/
│   └── docker-compose.yml    # 本地中间件：MySQL / Redis / RabbitMQ / MinIO
└── 原型/                      # 设计文档与工作流交付物（无需运行）
```

## 环境要求

| 工具 | 版本 | 用途 |
|---|---|---|
| Node.js | >= 20.19 | 前端 |
| pnpm | >= 8.7 | 前端包管理 |
| JDK | 21（Temurin 已验证） | 后端 |
| Maven | 3.9.x | 后端构建 |
| Docker + Compose | 任意近期版本 | 本地中间件 |

## 快速开始

### 第 0 步：启动中间件（只需一次）

```bash
cd deploy
docker compose up -d
docker compose ps        # 等待全部 healthy
```

| 服务 | 本机地址 | 凭证 | 说明 |
|---|---|---|---|
| MySQL 8 | `localhost:13310` | `bid / bid`，库 `bid_platform` | Flyway 自动建表（V1~V7）；本机 3306 常被占用，故用 13310 |
| Redis 7 | `localhost:16379` | 无密码 | 会话存储（本机 6379 常被占用，故用 16379） |
| RabbitMQ 3.13 | `localhost:5672` / 控制台 `localhost:15672` | `bid / bid` | 任务消息 |
| MinIO | S3 `localhost:19000` / 控制台 `localhost:19001` | `bidminio / bidminio123` | 文件存储，桶 `bid-platform` 自动创建 |

> 端口冲突说明：MySQL 用 13310、Redis 用 16379、MinIO 用 19000，与本仓库 `application.yml` 的本地缺省值一致；
> 若你机器上这些端口空闲想用标准端口，请同步改 compose 端口映射与 `ZB_DB_URL` / `ZB_REDIS_PORT` / `ZB_MINIO_ENDPOINT`。

> 镜像拉取说明：Docker Hub 直连不通时可用镜像源拉取后打标签，例如
> `docker pull docker.m.daocloud.io/library/rabbitmq:3.13-management-alpine && docker tag docker.m.daocloud.io/library/rabbitmq:3.13-management-alpine rabbitmq:3.13-management-alpine`

### 第 1 步：启动后端（8080）

```bash
cd backend
# Windows (CMD)
set ZB_BOOTSTRAP_ADMIN_USERNAME=admin
set ZB_BOOTSTRAP_ADMIN_PASSWORD=admin12345
mvn spring-boot:run

# Linux / macOS
ZB_BOOTSTRAP_ADMIN_USERNAME=admin ZB_BOOTSTRAP_ADMIN_PASSWORD=admin12345 mvn spring-boot:run
```

- 首次启动 Flyway 自动执行 7 个迁移脚本；管理员账号由上方环境变量创建（**不配置则跳过并打警告**）。
- 健康检查：`curl http://localhost:8080/actuator/health` → `{"status":"UP"}`
- Dify 当前为 `mock` 模式（`ZB_DIFY_MODE=mock`），无需真实 Dify 即可联调外层链路。

验证登录（任选一种）：

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"userName\":\"admin\",\"password\":\"admin12345\"}"
```

### 第 2 步：启动前端

**演示模式（默认，不依赖后端）**：

```bash
cd frontend
pnpm i
pnpm dev          # http://localhost:10032，登录页任意非空账号密码可进
```

**API 联调模式（连真实后端）**：

1. `frontend/.env` 已配置 `VITE_BID_DATA_MODE=api` 为默认；也可不改 env，
   启动后用右上角「Mock 数据 / 真实接口」开关运行时切换（localStorage 持久化）
2. `pnpm dev`，用上面创建的管理员账号登录（后端没启动时，登录页可点「演示模式进入」走 Mock）
3. 详见 `frontend/docs/frontend-runbook.md`（dev 模式默认走 `/proxy-default` 代理，无需改 CORS；
   `VITE_SERVICE_BASE_URL` 必须带 `/api/v1` 前缀，已按此配置）
4. 修改 `.env*` 后必须重启 `pnpm dev`（代理目标只在 dev server 启动时读取；数据模式切换则无需重启）

## 常用命令

```bash
# 前端
pnpm dev            # 开发（test 模式）
pnpm typecheck      # vue-tsc 类型检查
pnpm build          # 生产构建（prod 模式）
pnpm lint           # ESLint（带 --fix）

# 后端
mvn spring-boot:run        # 本地启动
mvn clean package          # 打包 target/bid-platform.jar
java -jar target/bid-platform.jar   # 以默认 profile（api,dev）运行

# 中间件
docker compose up -d       # 启动
docker compose down        # 停止（保留数据）
docker compose down -v     # 停止并清除全部数据
```

## 主要配置项（环境变量）

| 变量 | 缺省 | 说明 |
|---|---|---|
| `ZB_SERVER_PORT` | `8080` | api 进程端口 |
| `SPRING_PROFILES_ACTIVE` | `api,dev` | worker 进程用 `api,worker`（8081） |
| `ZB_DB_URL` / `ZB_DB_USER` / `ZB_DB_PASSWORD` | 本地缺省 | MySQL 连接 |
| `ZB_REDIS_HOST` / `ZB_REDIS_PORT` | `localhost` / `16379` | Redis 连接 |
| `ZB_RABBIT_HOST` / `ZB_RABBIT_USER` / `ZB_RABBIT_PASSWORD` | `localhost` / `bid` / `bid` | RabbitMQ |
| `ZB_MINIO_ENDPOINT` / `ZB_MINIO_ACCESS_KEY` / `ZB_MINIO_SECRET_KEY` | `http://localhost:19000` / `bidminio` / `bidminio123` | **本地开发缺省，生产必须覆盖** |
| `ZB_BOOTSTRAP_ADMIN_USERNAME` / `ZB_BOOTSTRAP_ADMIN_PASSWORD` | 空（跳过创建） | 初始管理员，密码 >= 8 位 |
| `ZB_CORS_ORIGINS` | `http://localhost:10032,...` | 允许跨域携带凭据的前端来源 |
| `ZB_DIFY_MODE` | `mock` | `mock` / `real`；生产强制 `real` |
| `ZB_SECRET_MASTER_KEY` | 空 | Dify API Key 落库加密主密钥（Base64 32 字节，接真实 Dify 前必须配置） |

## 认证机制简述

- 登录 `POST /api/v1/auth/login` 建立服务端会话（Spring Session + Redis，HttpOnly `SESSION` Cookie），
  同时兼容 `Authorization: Bearer <sessionId>` 携带同一会话。
- CSRF：Cookie 会话的变更请求需回传 `X-XSRF-TOKEN`（来自 `XSRF-TOKEN` Cookie 或 `GET /api/v1/auth/csrf`）；
  登录接口与 Bearer 头调用不强制 CSRF。
- 未登录返回 `code=8888`，前端据此跳转登录页；权限不足返回 `A0403`。

## 已知边界（诚实声明）

- **worker 任务执行链路未实现**：`application-worker.yml` 与任务表（V5）已就绪，但任务执行器、
  Dify real 适配器、Outbox 投递等代码尚未编写；当前仅 api 进程可完整启动。
- 前端部分页面在 api 模式下接口未接入时显示「未接入」空状态，属预期行为，不回退演示数据。
- 本地 compose 的中间件凭证仅为开发用途，生产部署必须全部走环境注入并收敛 CORS。

## 故障排查

| 现象 | 处理 |
|---|---|
| 后端启动报 Flyway/连接失败 | `docker compose ps` 确认 MySQL healthy；确认 13310 未被占用（或用 `ZB_DB_URL` 指向你的 MySQL） |
| 启动卡在 Redis | 确认 `localhost:16379` 可达（`docker compose up -d redis`） |
| 登录后接口 401 | Cookie 会话失效；检查前后端是否同源/代理配置，或改用 Bearer 方式排查 |
| 前端登录报 403（`A0403`） | 请求缺 `/api/v1` 前缀被 CSRF 拦截：确认 `.env.test` 的 `VITE_SERVICE_BASE_URL=http://localhost:8080/api/v1` 并重启 `pnpm dev`（后端日志对此无输出） |
| 前端登录报 CSRF | `GET /api/v1/auth/csrf` 先取 token，或确认请求走 dev 代理同源 |
| 10032 端口被占 | 前端端口在 `frontend/.env.test` / vite 配置中固定（strictPort），释放端口后重启 |
| MinIO 上传失败 | `ZB_MINIO_ACCESS_KEY/SECRET_KEY` 与 compose 中 `bidminio/bidminio123` 一致；确认桶存在 |
