# 住建局综合业务管理平台 API 文档

## 1. 基础信息

| 项目 | 值 |
|---|---|
| 后端服务地址 | `http://localhost:3000` |
| 接口统一前缀 | `/api` |
| 前端开发地址 | `http://localhost:4000` |
| 前端代理规则 | `/api` → `http://localhost:3000` |
| 请求/响应编码 | UTF-8 |
| 响应格式 | `application/json` |
| 认证方式 | 当前阶段无认证 |

> 当前为项目骨架版本，仅提供健康检查接口。四大业务模块的接口尚未实现，后续按本文档约定扩展。

## 2. 统一响应结构

所有接口均返回以下结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": "2026-10-24T10:15:52.847"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | integer | 业务状态码，`0` 表示成功 |
| `message` | string | 提示信息 |
| `data` | any | 业务数据，无数据时为 `null` |
| `timestamp` | string | 服务端响应时间（ISO-8601） |

### 状态码约定

| code | 说明 |
|---|---|
| `0` | 成功 |
| `400` | 请求参数错误（预留） |
| `401` | 未认证或登录失效（预留） |
| `404` | 资源不存在（预留） |
| `409` | 业务状态冲突（预留） |
| `500` | 服务器内部错误（预留） |

## 3. 接口列表

### 3.1 健康检查

用于验证后端服务是否正常运行，前端顶栏据此显示「后端服务正常 / 后端未连接」。

- **请求方法**：`GET`
- **请求路径**：`/api/health`
- **请求参数**：无
- **认证要求**：无

**成功响应示例**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "status": "UP",
    "service": "byzjj-backend",
    "timestamp": "2026-10-24T10:15:52.8470723"
  },
  "timestamp": "2026-10-24T10:15:52.8470723"
}
```

**curl 示例**

```bash
curl http://localhost:3000/api/health
```

**前端调用示例**

```js
import { getHealth } from '@/api/health'

const data = await getHealth()
// data = { status: 'UP', service: 'byzjj-backend', timestamp: '...' }
```

## 4. 跨域说明

- 开发环境推荐通过 Vite 代理访问：前端请求 `/api/health`，由 Vite 转发到 `http://localhost:3000/api/health`。
- 后端已允许 `http://localhost:4000` 直接跨域调用 `/api/**`，便于调试。
- 新增接口时继续使用 `/api` 前缀，并复用统一响应结构。

## 5. 后续业务接口规划（尚未实现）

| 模块 | 规划前缀 | 说明 |
|---|---|---|
| 住宅专项维修资金 | `/api/funds` | 小区台账、交存记录、使用申请等 |
| 国有资产租赁更新 | `/api/lease` | 资产台账、租赁合同、续租、租金收缴等 |
| 直管公房一张图 | `/api/housing` | 公房点位、统计、详情等 |
| 政务公文接收 | `/api/documents` | 收文列表、详情、签收与办理等 |

以上接口当前均未实现，待后续版本补充；实现时需同步更新本文件。
