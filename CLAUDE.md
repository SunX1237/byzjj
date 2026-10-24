# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概况

- 住建局综合业务演示系统，前后端分离。
- 住宅专项维修资金模块已实现按项目、楼幢、单元、房屋的资金查询；国有资产租赁更新、直管公房一张图、政务公文接收仍为**占位页**。
- 系统提供独立门户页 `/portal`，集中展示四个模块入口；点击卡片进入对应模块页。模块页不显示侧边栏，顶栏提供「返回门户」入口。
- 前端 Vue 3 + Vite 7 + Element Plus，端口 4000；后端 Java 21 + Spring Boot 4.1.2，端口 3000。
- 环境要求：JDK 21、Node 20.19+ 或 22.12+。维修资金查询使用 H2 内存库 + Spring Data JPA；当前无登录鉴权、图表和地图依赖。

## 常用命令

### 后端（backend/）

```bash
cd backend
./gradlew bootRun                 # 启动后端，监听 3000
./gradlew clean build             # 构建并运行测试
./gradlew test                    # 运行全部测试
./gradlew test --tests "gov.byzjj.ByzjjBackendApplicationTests.contextLoads"   # 运行单个测试
./gradlew test --tests "gov.byzjj.funds.FundQueryIntegrationTest"   # 运行维修资金查询集成测试
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
- `front/src/router/index.js` 中 `/portal` 是独立门户路由（不套用 `AppLayout`）；`/` 和未知路径都重定向到 `/portal`；`/funds`、`/lease`、`/housing`、`/documents` 作为 `AppLayout` 子路由。
- `front/src/layouts/AppLayout.vue` 是模块页后台壳：顶部栏（含「返回门户」链接）+ 全宽内容区，不显示侧边栏；顶栏展示系统名称、当前日期与后端健康状态，子路由内容渲染在 `el-main` 中。
- `front/src/views/PortalView.vue` 是全屏门户页：四张模块卡片、系统标题、后端状态；模块入口配置在其 `modules` 数组中，卡片使用 `<router-link>` 跳转。
- `/funds` 使用 `front/src/views/funds/FundQueryView.vue`（级联查询、汇总卡、房屋列表与流水）；`/lease`、`/housing`、`/documents` 继续使用 `front/src/views/PlaceholderView.vue`。
- `front/src/api/funds.js` 封装维修资金树与查询接口；`front/src/utils/format.js` 提供金额、面积、日期格式化。
- `front/src/composables/useBackendHealth.js` 封装三态健康检查，`PortalView.vue` 与 `AppLayout.vue` 共用；`front/src/api/health.js` 调用 `/api/health`。请求失败只会改变状态标签，不会阻塞页面。
- `front/src/api/request.js` 统一解包业务响应，并提取后端错误消息、超时与连接失败提示。
- 主题与全局样式在 `front/src/styles/global.css`（政务蓝）。

### 后端结构

- 包名 `gov.byzjj`，启动类为 `ByzjjBackendApplication`。
- `controller/HealthController.java` 提供 `GET /api/health`；`common/ApiResponse.java` 为统一响应；`config/WebConfig.java` 配置 CORS；`common/GlobalExceptionHandler.java` 统一处理业务异常与未知异常。
- 维修资金代码在 `gov.byzjj.funds`：`entity`（项目/楼幢/单元/房屋/流水）、`repository`、`service/FundQueryService`、`controller/FundController`、`seed/FundDemoDataInitializer`。
- H2 内存库与 JPA 配置在 `application.properties`；应用启动时生成 3 个项目、48 户、624 条流水，重启重置。
- 维修资金接口：`GET /api/funds/tree` 返回层级树；`GET /api/funds/query?projectId&buildingId&unitId&houseId` 返回汇总、房屋列表或流水。余额由流水实时汇总，不落库。
- 依赖使用 Spring Boot 4 的命名：`spring-boot-starter-webmvc` / `spring-boot-starter-webmvc-test`，不是 Boot 3 的 `spring-boot-starter-web`；JPA 使用 `spring-boot-starter-data-jpa` + H2 runtime。
- 数据模型使用逻辑外键（只存 ID），不建 JPA 双向关联；DTO 使用 record；Jackson 3 下不要引入 Jackson 2 的 `jsr310`。

## 新增模块功能时的落点

1. 在 `front/src/views/<module>/` 新建页面，并在 `front/src/router/index.js` 用真实组件替换对应的 `PlaceholderView`。
2. 在 `front/src/api/` 新增模块 API 文件，复用 `request.js`。
3. 后端在 `gov.byzjj.<module>` 下新增 Controller/Service，接口保持 `/api` 前缀并返回 `ApiResponse`。
4. 同步更新 `API.md`（接口契约）、`HelpingDoc.md`（使用说明）和当日 `.claude-logs/YYYY-MM-DD.md`。
5. 调整模块页顶栏或布局时修改 `front/src/layouts/AppLayout.vue`；新增模块入口时同步更新 `front/src/views/PortalView.vue` 的 `modules` 数组。

## 项目约定

- 界面文案、代码注释使用中文；函数注释需说明用途、参数和目的。
- 接口文档固定在根目录 `API.md`，帮助文档固定在 `HelpingDoc.md`。
- 每天的操作记录写入 `.claude-logs/YYYY-MM-DD.md`，包含需求、方案、修改文件和验证结果。
- 不自动执行 `git commit` / `git merge`；删除文件、目录或修改环境变量配置前需确认。
- Gradle Wrapper 通过腾讯云镜像下载 Gradle 9.2.1（`backend/gradle/wrapper/gradle-wrapper.properties`）；官方源在当前网络下会超时，非必要不要改回。
