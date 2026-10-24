# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概况

- 住建局综合业务演示系统，前后端分离。
- 四个业务模块（住宅专项维修资金、国有资产租赁更新、直管公房一张图、政务公文接收）当前均为**占位页**，只显示固定文本，尚未实现业务功能。
- 前端 Vue 3 + Vite 7 + Element Plus，端口 4000；后端 Java 21 + Spring Boot 4.1.2，端口 3000。
- 环境要求：JDK 21、Node 20.19+ 或 22.12+。当前无数据库、登录鉴权、图表和地图依赖。

## 常用命令

### 后端（backend/）

```bash
cd backend
./gradlew bootRun                 # 启动后端，监听 3000
./gradlew clean build             # 构建并运行测试
./gradlew test                    # 运行全部测试
./gradlew test --tests "gov.byzjj.ByzjjBackendApplicationTests.contextLoads"   # 运行单个测试
java -jar build/libs/byzjj-backend-0.0.1-SNAPSHOT.jar   # 运行构建产物
```

Windows CMD/PowerShell 将 `./gradlew` 替换为 `gradlew.bat`。

健康检查：`curl http://localhost:3000/api/health`。

### 前端（front/）

```bash
cd front
npm install
npm run dev       # 开发服务器，固定 4000 端口
npm run build     # 生产构建到 front/dist/
npm run preview   # 预览生产构建
```

当前未配置 lint 或前端测试脚本，不要假设存在 `npm run lint` / `npm test`。`npm run build` 会提示主 chunk 超过 500 kB，这是全量引入 Element Plus 的预期结果，不是构建失败。

## 架构与数据流

```text
浏览器 → http://localhost:4000 (Vite) → /api 代理 → http://localhost:3000 (Spring Boot) → ApiResponse
```

- 代理配置在 `front/vite.config.js`；后端 CORS 仅允许 `http://localhost:4000`（`backend/src/main/java/gov/byzjj/config/WebConfig.java`）。修改前端端口时需同步修改这两处。
- 后端统一响应结构为 `{ code, message, data, timestamp }`，`code=0` 表示成功，定义在 `backend/src/main/java/gov/byzjj/common/ApiResponse.java`。
- Axios 封装在 `front/src/api/request.js`：baseURL 为 `/api`；响应拦截器在 `code === 0` 时直接返回 `body.data`，API 调用方无需再次解包。非 0 时抛出 `message`。

### 前端结构

- `front/src/main.js` 注册 Element Plus（中文 locale）与路由。
- `front/src/layouts/AppLayout.vue` 是后台壳：左侧菜单、顶栏、后端健康状态；子路由内容渲染在 `el-main` 中。
- `front/src/router/index.js` 定义四条子路由：`/funds`、`/lease`、`/housing`、`/documents`；`/` 和未知路径都重定向到 `/funds`。
- 四条路由当前全部复用 `front/src/views/PlaceholderView.vue`，页面标题和说明来自路由 `meta`。替换模块页面时修改 `router/index.js` 中的 `component` 即可。
- `front/src/api/health.js` 调用 `/api/health`，供顶栏显示「后端服务正常 / 后端未连接」。请求失败只会改变状态标签，不会阻塞页面。
- 主题与全局样式在 `front/src/styles/global.css`（政务蓝）。

### 后端结构

- 包名 `gov.byzjj`，启动类为 `ByzjjBackendApplication`。
- `controller/HealthController.java` 提供 `GET /api/health`；`common/ApiResponse.java` 为统一响应；`config/WebConfig.java` 配置 CORS。
- 端口和 UTF-8 配置在 `backend/src/main/resources/application.properties`。
- 依赖使用 Spring Boot 4 的命名：`spring-boot-starter-webmvc` / `spring-boot-starter-webmvc-test`，不是 Boot 3 的 `spring-boot-starter-web`。
- 当前没有数据源和持久层。

## 新增模块功能时的落点

1. 在 `front/src/views/<module>/` 新建页面，并在 `front/src/router/index.js` 用真实组件替换对应的 `PlaceholderView`。
2. 在 `front/src/api/` 新增模块 API 文件，复用 `request.js`。
3. 后端在 `gov.byzjj.<module>` 下新增 Controller/Service，接口保持 `/api` 前缀并返回 `ApiResponse`。
4. 同步更新 `API.md`（接口契约）、`HelpingDoc.md`（使用说明）和当日 `.claude-logs/YYYY-MM-DD.md`。
5. 新增菜单项或调整布局时修改 `front/src/layouts/AppLayout.vue`。

## 项目约定

- 界面文案、代码注释使用中文；函数注释需说明用途、参数和目的。
- 接口文档固定在根目录 `API.md`，帮助文档固定在 `HelpingDoc.md`。
- 每天的操作记录写入 `.claude-logs/YYYY-MM-DD.md`，包含需求、方案、修改文件和验证结果。
- 不自动执行 `git commit` / `git merge`；删除文件、目录或修改环境变量配置前需确认。
- Gradle Wrapper 通过腾讯云镜像下载 Gradle 9.2.1（`backend/gradle/wrapper/gradle-wrapper.properties`）；官方源在当前网络下会超时，非必要不要改回。
