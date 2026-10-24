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

> 当前已实现健康检查与住宅专项维修资金查询接口；国有资产租赁、直管公房一张图、政务公文接收接口尚未实现，后续按本文档约定扩展。

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
| `400` | 请求参数错误 |
| `401` | 未认证或登录失效（预留） |
| `404` | 资源不存在 |
| `409` | 业务状态冲突（预留） |
| `500` | 服务器内部错误 |

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

### 3.2 维修资金层级树

返回项目 → 楼幢 → 单元 → 房屋的完整层级，用于前端级联筛选。

- **请求方法**：`GET`
- **请求路径**：`/api/funds/tree`
- **请求参数**：无

**成功响应示例（节选）**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "projects": [
      {
        "id": 1,
        "name": "阳光花园",
        "buildings": [
          {
            "id": 1,
            "name": "1号楼",
            "units": [
              {
                "id": 1,
                "name": "1单元",
                "houses": [
                  { "id": 1, "name": "101室", "ownerName": "张伟" }
                ]
              }
            ]
          }
        ]
      }
    ]
  },
  "timestamp": "2026-10-24T11:45:00"
}
```

**curl 示例**

```bash
curl http://localhost:3000/api/funds/tree
```

### 3.3 维修资金查询

按项目、楼幢、单元、房屋实时查询资金交存、使用、结息和余额。传入的最深层级决定查询范围。

- **请求方法**：`GET`
- **请求路径**：`/api/funds/query`

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `projectId` | long | 是 | 项目 ID |
| `buildingId` | long | 否 | 楼幢 ID；传 `unitId` 时必须同时传 |
| `unitId` | long | 否 | 单元 ID；传 `houseId` 时必须同时传 |
| `houseId` | long | 否 | 房屋 ID |

**查询层级**

| 传入参数 | scope | 返回内容 |
|---|---|---|
| 仅 `projectId` | `PROJECT` | 项目汇总 + 该项目全部房屋汇总 |
| `projectId + buildingId` | `BUILDING` | 楼幢汇总 + 楼幢房屋汇总 |
| `projectId + buildingId + unitId` | `UNIT` | 单元汇总 + 单元房屋汇总 |
| 再加 `houseId` | `HOUSE` | 房屋详情 + 汇总 + 全部流水 |

**房屋级响应示例（节选）**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "scope": "HOUSE",
    "scopeName": "阳光花园 · 1号楼 · 1单元 · 101室",
    "summary": {
      "depositTotal": 6366.00,
      "usageTotal": 864.00,
      "interestTotal": 146.57,
      "balance": 5648.57
    },
    "houses": [],
    "house": {
      "projectName": "阳光花园",
      "buildingName": "1号楼",
      "unitName": "1单元",
      "roomNo": "101室",
      "ownerName": "张伟",
      "area": 112.00
    },
    "transactions": [
      {
        "id": 13,
        "type": "INTEREST",
        "typeLabel": "结息",
        "amount": 21.10,
        "occurredAt": "2026-10-22",
        "remark": "季度结息",
        "balanceAfter": 5648.57
      }
    ]
  },
  "timestamp": "2026-10-24T11:45:00"
}
```

- 聚合层级的 `house` 为 `null`、`transactions` 为 `[]`；房屋层级的 `houses` 为 `[]`。
- `balance = depositTotal + interestTotal - usageTotal`。
- `type` 取值：`DEPOSIT`（交存）、`USAGE`（使用）、`INTEREST`（结息）；`typeLabel` 为中文标签。
- 流水按日期倒序返回，`balanceAfter` 为该笔流水后的余额。

**curl 示例**

```bash
curl "http://localhost:3000/api/funds/query?projectId=1"
curl "http://localhost:3000/api/funds/query?projectId=1&buildingId=1"
curl "http://localhost:3000/api/funds/query?projectId=1&buildingId=1&unitId=1"
curl "http://localhost:3000/api/funds/query?projectId=1&buildingId=1&unitId=1&houseId=1"
```

**错误响应示例**

```json
{ "code": 400, "message": "请选择项目", "data": null, "timestamp": "..." }
{ "code": 404, "message": "项目不存在", "data": null, "timestamp": "..." }
{ "code": 400, "message": "楼幢不属于所选项目", "data": null, "timestamp": "..." }
```

## 4. 跨域说明

- 开发环境推荐通过 Vite 代理访问：前端请求 `/api/health`，由 Vite 转发到 `http://localhost:3000/api/health`。
- 后端已允许 `http://localhost:4000` 直接跨域调用 `/api/**`，便于调试。
- 新增接口时继续使用 `/api` 前缀，并复用统一响应结构。

## 5. 后续业务接口规划（尚未实现）

| 模块 | 规划前缀 | 说明 |
|---|---|---|
| 国有资产租赁更新 | `/api/lease` | 资产台账、租赁合同、续租、租金收缴等 |
| 直管公房一张图 | `/api/housing` | 公房点位、统计、详情等 |
| 政务公文接收 | `/api/documents` | 收文列表、详情、签收与办理等 |

维修资金接口已在上文实现；以上其余接口待后续版本补充，实现时需同步更新本文件。

## 6. 演示数据说明

- 维修资金使用 H2 内存数据库，应用启动时自动生成演示数据，重启后重置。
- 演示规模：3 个项目、6 幢、12 个单元、48 户、624 条资金流水。
- 金额均为两位小数；结息按季度基于当时余额计算。
