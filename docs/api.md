# API 设计说明

## 1. 接口原则

- 使用 RESTful API。
- 统一前缀：`/api/v1`。
- 所有写接口支持 `traceId`。
- 所有分页接口使用 `pageNo`、`pageSize`。
- 所有时间字段使用 ISO-8601 格式。
- 经纬度字段统一使用 `longitude`、`latitude`。

## 2. 统一响应

成功响应：

```json
{
  "code": "0",
  "message": "success",
  "traceId": "trace-20260625-000001",
  "data": {}
}
```

失败响应：

```json
{
  "code": "VALIDATION_ERROR",
  "message": "longitude must be between -180 and 180",
  "traceId": "trace-20260625-000001",
  "data": null
}
```

## 3. 错误码

| 错误码 | HTTP 状态 | 说明 |
| --- | --- | --- |
| `0` | 200 | 成功 |
| `VALIDATION_ERROR` | 400 | 参数错误 |
| `UNAUTHORIZED` | 401 | 未登录或 Token 无效 |
| `FORBIDDEN` | 403 | 无权限 |
| `NOT_FOUND` | 404 | 资源不存在 |
| `DUPLICATE_EVENT` | 409 | 重复位置事件 |
| `BUSINESS_ERROR` | 422 | 业务规则不满足 |
| `INTERNAL_ERROR` | 500 | 系统错误 |

## 4. 核心接口

### 4.1 上报单条位置事件

`POST /api/v1/location-events`

请求：

```json
{
  "eventId": "gps-20260625-000001",
  "entityCode": "CAR-001",
  "entityType": "VEHICLE",
  "sourceType": "GPS",
  "longitude": 121.473701,
  "latitude": 31.230416,
  "speed": 42.5,
  "direction": 90.0,
  "accuracy": 8.0,
  "eventTime": "2026-06-25T09:30:00+08:00"
}
```

响应：

```json
{
  "code": "0",
  "message": "success",
  "traceId": "trace-20260625-000001",
  "data": {
    "trajectoryPointId": 10001,
    "accepted": true
  }
}
```

校验规则：

- `eventId` 必填且唯一。
- `entityCode` 必填。
- `longitude` 范围为 `[-180, 180]`。
- `latitude` 范围为 `[-90, 90]`。
- `eventTime` 不允许为空。

### 4.2 批量上报位置事件

`POST /api/v1/location-events/batch`

请求：

```json
{
  "events": [
    {
      "eventId": "gps-20260625-000001",
      "entityCode": "CAR-001",
      "entityType": "VEHICLE",
      "sourceType": "GPS",
      "longitude": 121.473701,
      "latitude": 31.230416,
      "eventTime": "2026-06-25T09:30:00+08:00"
    }
  ]
}
```

响应：

```json
{
  "code": "0",
  "message": "success",
  "traceId": "trace-20260625-000002",
  "data": {
    "total": 1,
    "accepted": 1,
    "rejected": 0
  }
}
```

### 4.3 查询实体最新位置

`GET /api/v1/entities/{entityId}/latest-location`

响应：

```json
{
  "code": "0",
  "message": "success",
  "traceId": "trace-20260625-000003",
  "data": {
    "entityId": 1,
    "entityCode": "CAR-001",
    "entityName": "巡检车 1",
    "longitude": 121.473701,
    "latitude": 31.230416,
    "speed": 42.5,
    "direction": 90.0,
    "eventTime": "2026-06-25T09:30:00+08:00"
  }
}
```

### 4.4 查询历史轨迹

`GET /api/v1/entities/{entityId}/trajectory`

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `startTime` | 是 | 开始时间 |
| `endTime` | 是 | 结束时间 |
| `simplify` | 否 | 是否抽稀 |
| `tolerance` | 否 | 抽稀容差，单位米 |

响应：

```json
{
  "code": "0",
  "message": "success",
  "traceId": "trace-20260625-000004",
  "data": {
    "entityId": 1,
    "pointCount": 2,
    "points": [
      {
        "longitude": 121.473701,
        "latitude": 31.230416,
        "speed": 42.5,
        "eventTime": "2026-06-25T09:30:00+08:00"
      }
    ]
  }
}
```

### 4.5 附近实体查询

`GET /api/v1/geo/nearby`

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `longitude` | 是 | 中心点经度 |
| `latitude` | 是 | 中心点纬度 |
| `radiusMeter` | 是 | 半径，单位米 |
| `entityType` | 否 | 实体类型 |
| `limit` | 否 | 返回数量 |

### 4.6 电子围栏管理

创建围栏：`POST /api/v1/fences`

```json
{
  "fenceName": "园区 A",
  "fenceType": "POLYGON",
  "ruleType": "ENTER",
  "geometry": {
    "type": "Polygon",
    "coordinates": [
      [
        [121.47, 31.23],
        [121.48, 31.23],
        [121.48, 31.24],
        [121.47, 31.24],
        [121.47, 31.23]
      ]
    ]
  },
  "enabled": true
}
```

更新围栏：`PUT /api/v1/fences/{id}`

删除围栏：`DELETE /api/v1/fences/{id}`

查询围栏：`GET /api/v1/fences?pageNo=1&pageSize=20`

### 4.7 告警处理

查询告警：`GET /api/v1/alerts`

处理告警：`POST /api/v1/alerts/{id}/handle`

```json
{
  "action": "ACK",
  "remark": "已通知现场人员确认"
}
```

关闭告警：`POST /api/v1/alerts/{id}/close`

## 5. 权限设计

| 角色 | 权限 |
| --- | --- |
| 系统管理员 | 用户、角色、权限、系统配置 |
| 业务管理员 | 实体、围栏、告警规则、统计 |
| 监控人员 | 实时地图、告警处理、轨迹回放 |
| 数据分析人员 | 历史轨迹、统计分析、数据导出 |

## 6. 可测试性要求

- 每个接口必须覆盖成功、参数错误、权限不足和资源不存在场景。
- 位置上报接口必须覆盖重复 `eventId`。
- 历史轨迹接口必须覆盖时间范围过大和无数据。
- 附近查询必须覆盖半径非法、经纬度非法和结果为空。

## 7. 风险提示

- 执行风险：API 设计如果不控制时间范围和分页，后续会造成查询压力。
- 错误风险：批量上报不能因为单条错误导致整批不可追踪，必须返回接受和拒绝数量。
- 安全风险：轨迹数据属于敏感数据，接口必须默认鉴权并记录审计日志。
