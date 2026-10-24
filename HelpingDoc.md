# 住建局综合业务管理平台 帮助文档

## 1. 项目简介

本项目是一个前后端分离的 Web Demo 骨架，提供统一门户页和四个业务模块：

1. 住宅专项维修资金
2. 国有资产租赁更新
3. 直管公房一张图
4. 政务公文接收

**当前阶段不实现任何业务功能**。系统入口为门户页 `/portal`，页面展示四张模块卡片；点击卡片进入对应模块后，显示白色占位页面和固定文本「本模块功能暂未实现。」。门户页与模块页都会显示后端服务连接状态。

## 2. 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3.5、Vite 7、Element Plus 2、Vue Router 4、Axios |
| 后端 | Java 21、Spring Boot 4.1.2、Gradle |
| 数据 | 当前无数据库 |
| 通信 | 前端 `/api` 请求由 Vite 代理到后端 3000 端口 |

## 3. 环境要求

- **JDK 21**（已验证 21.0.9）
- **Node.js 20.19+ 或 22.12+**（已验证 Node 26.10.0）
- **npm**（已验证 11.19.1）
- 项目已内置 Gradle Wrapper，首次构建需要联网下载 Gradle 与依赖
- 无需安装数据库

## 4. 目录结构

```text
byzjj/
├── front/                          # Vue 前端
│   ├── src/
│   │   ├── api/                    # Axios 封装与接口
│   │   ├── composables/            # 共享组合式逻辑（后端健康状态）
│   │   ├── layouts/AppLayout.vue   # 模块页顶部栏（返回门户）+ 内容区
│   │   ├── router/index.js         # 门户与四个模块路由
│   │   ├── views/PortalView.vue    # 统一门户页
│   │   ├── views/PlaceholderView.vue # 模块占位页
│   │   └── styles/global.css
│   ├── vite.config.js              # 4000 端口与 /api 代理
│   └── package.json
├── backend/                        # Spring Boot 后端
│   └── src/main/java/gov/byzjj/
│       ├── ByzjjBackendApplication.java
│       ├── common/ApiResponse.java
│       ├── config/WebConfig.java   # CORS
│       └── controller/HealthController.java
├── API.md                          # 接口文档
├── HelpingDoc.md                   # 本文档
├── .claude-logs/                   # 操作日志
└── .gitignore
```

## 5. 启动方式

### 5.1 启动后端（端口 3000）

```bash
cd backend
./gradlew bootRun
```

Windows CMD / PowerShell 可使用：

```bat
cd backend
gradlew.bat bootRun
```

也可以先构建再运行：

```bash
cd backend
./gradlew clean build
java -jar build/libs/byzjj-backend-0.0.1-SNAPSHOT.jar
```

启动成功后访问健康接口：

```bash
curl http://localhost:3000/api/health
```

### 5.2 启动前端（端口 4000）

```bash
cd front
npm install
npm run dev
```

浏览器访问：<http://localhost:4000>，会自动跳转到门户页 <http://localhost:4000/portal>。

也可以直接访问四个模块：`/funds`、`/lease`、`/housing`、`/documents`。

> 前端首次 `npm install` 需要联网。本项目使用 npm 镜像源即可正常安装。

## 6. 页面说明

- **门户页 `/portal`**：不显示侧边栏，顶部为系统名称与后端状态；页面中央展示四张业务模块卡片，卡片包含模块名称、功能说明和「功能建设中」标签。点击卡片进入对应模块。
- **模块页**：不显示侧边栏，顶部栏左侧提供「返回门户」按钮，下方为全宽白色占位页和固定文本。
- 门户页与模块页右上角显示当前日期与后端连接状态：
  - 「后端服务正常」：`GET /api/health` 返回 `status=UP`
  - 「后端未连接」：后端未启动或请求失败，页面仍可正常浏览
- 门户页为响应式布局：桌面端 2×2 卡片，窄屏（900px 以下）单列显示。
- 当前没有登录页、数据库、表格、图表、地图等业务内容。

## 7. 前后端联通

- 前端 Axios 的 `baseURL` 为 `/api`。
- Vite 开发服务器将 `/api` 代理到 `http://localhost:3000`，配置见 `front/vite.config.js`。
- 后端已配置允许 `http://localhost:4000` 跨域。
- 统一响应结构见 `API.md`。

## 8. 构建

```bash
# 后端
cd backend
./gradlew clean build

# 前端
cd front
npm run build
```

前端构建产物位于 `front/dist/`，后端可执行 Jar 位于 `backend/build/libs/`。

## 9. 常见问题

### 9.1 端口被占用

- 后端默认 3000，前端默认 4000。
- Windows 可执行 `netstat -ano | findstr :3000` 或 `netstat -ano | findstr :4000` 查看占用进程。
- 修改前端端口时，同时更新 `front/vite.config.js` 中的 `server.port`。

### 9.2 页面显示「后端未连接」

1. 确认后端已启动且控制台无报错。
2. 浏览器访问 `http://localhost:3000/api/health` 是否返回 `status=UP`。
3. 确认前端访问的是 `http://localhost:4000`，而不是直接打开 `index.html`。
4. 检查 3000 端口是否被其他程序占用。

### 9.3 Gradle Wrapper 下载慢或失败

- 首次执行 `./gradlew` 会下载 Gradle 9.2.1（约 135 MB）。
- 项目已将 `backend/gradle/wrapper/gradle-wrapper.properties` 的 `distributionUrl` 配置为腾讯云镜像 `https://mirrors.cloud.tencent.com/gradle/gradle-9.2.1-bin.zip`，便于国内网络下载。
- 如需使用官方源，可改回 `https://services.gradle.org/distributions/gradle-9.2.1-bin.zip`（该地址会跳转到 GitHub，可能较慢）。
- 如本机已安装 Gradle 9.2.1，也可直接执行 `gradle bootRun`。

### 9.4 npm 依赖安装慢

- 可切换国内镜像，例如：`npm config set registry https://registry.npmmirror.com`。
- 若安装后提示 esbuild 的安装脚本被拦截，可执行 `npm install-scripts approve esbuild` 后重新安装；本项目当前构建已验证可用。

### 9.5 中文乱码

- 项目源码与配置文件均使用 UTF-8。
- 后端已设置 `server.servlet.encoding.*=UTF-8`，Gradle 编译已指定 `options.encoding = 'UTF-8'`。

### 9.6 直接访问模块路径 404（生产部署）

- 开发环境 Vite 已处理 history fallback，直接访问 `/portal`、`/funds` 等路径正常。
- 生产部署到 Nginx 等静态服务器时，需要将未知路径回退到 `index.html`，否则刷新模块页会出现 404。

## 10. 后续扩展建议

1. 在 `front/src/views/` 下为四个模块创建真实页面。
2. 在 `front/src/router/index.js` 中将 `PlaceholderView` 替换为对应模块页面组件。
3. 在 `front/src/views/PortalView.vue` 的 `modules` 数组中更新模块说明与入口；模块页顶栏布局在 `front/src/layouts/AppLayout.vue`。
4. 在 `front/src/api/` 下按模块新增接口封装，沿用统一响应解包逻辑。
5. 后端按 `/api/funds`、`/api/lease`、`/api/housing`、`/api/documents` 前缀新增 Controller、Service 与数据层。
6. 新增接口时同步更新 `API.md`，功能变化同步更新本文档与 `.claude-logs/`。
