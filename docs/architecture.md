# 系统架构说明

## 1. 架构目标

STIP 的架构目标是支撑多源位置事件接入、实时位置查询、历史轨迹分析、空间检索、电子围栏告警和统计看板。系统按阶段演进，第一阶段优先完成可演示 MVP，后续逐步引入消息队列、实时计算和离线分析组件。

## 2. 分层架构

```text
前端展示层
  Vue3 / TypeScript / Element Plus / ECharts / Leaflet
        |
        v
接口接入层
  RESTful API / JWT / RBAC / 参数校验 / 限流
        |
        v
业务服务层
  认证 / 实体 / 位置接入 / 轨迹 / 围栏 / 告警 / 统计
        |
        +--> Redis
        |     最新位置缓存 / GEO 查询 / 热点数据
        |
        +--> PostgreSQL + PostGIS
              实体 / 轨迹点 / 围栏 / 告警 / 统计结果
```

后续扩展链路：

```text
位置接入服务 -> Kafka -> Flink -> PostgreSQL/PostGIS
                         -> Redis
                         -> Elasticsearch
                         -> HDFS/HBase
                         -> Spark 离线分析
```

## 3. 服务边界

第一阶段建议采用模块化单体或轻量多模块工程，保留微服务边界但不强制拆分部署。

| 模块 | 职责 | 第一阶段形态 | 后续演进 |
| --- | --- | --- | --- |
| `auth` | 登录、用户、角色、权限、Token | 后端模块 | `auth-service` |
| `ingestion` | 位置事件接入、校验、标准化 | 后端模块 | `ingestion-service` + Kafka |
| `trajectory` | 最新位置、历史轨迹、轨迹抽稀、附近查询 | 后端模块 | `trajectory-service` |
| `fence` | 电子围栏、空间关系判断 | 后端模块 | `fence-service` |
| `alert` | 告警生成、确认、处理、关闭 | 后端模块 | `alert-service` |
| `analytics` | 统计概览、趋势、Top N | 后端模块 | `analytics-service` + Spark |
| `common` | 统一响应、异常、分页、工具类 | 公共模块 | 公共依赖包 |
| `admin-web` | 地图、轨迹、围栏、告警、统计界面 | 前端应用 | 独立部署 |

## 4. 核心数据流

### 4.1 第一阶段同步闭环

```text
设备或模拟器
  |
  v
POST /api/v1/location-events
  |
  v
参数校验 -> 标准化 -> 幂等校验
  |
  +--> PostgreSQL/PostGIS 写入 trajectory_point
  +--> Redis 更新实体最新位置
  +--> 围栏/速度规则判断
          |
          v
        alert_event
```

### 4.2 第二阶段异步链路

```text
位置事件 API
  |
  v
Kafka raw-location-topic
  |
  v
Flink 清洗与计算
  |
  +--> clean-location-topic
  +--> alert-topic
  +--> PostgreSQL/PostGIS
  +--> Redis
  +--> Elasticsearch
```

## 5. 前端架构

前端使用 Vue3 + TypeScript，建议按业务域组织：

```text
frontend/admin-web/src/
  api/
  components/
    map/
    trajectory/
    fence/
    alert/
  stores/
  views/
    dashboard/
    realtime/
    trajectory/
    fence/
    alert/
    system/
  router/
  utils/
```

关键复用组件：

- 地图容器组件。
- 实体点位图层组件。
- 轨迹线图层组件。
- 围栏绘制组件。
- 轨迹播放器组件。
- 告警列表组件。

## 6. 可复用性设计

- 位置事件标准模型独立于实体类型，可复用于人员、车辆和设备。
- 轨迹算法模块只依赖输入点集，避免绑定数据库或 Web 框架。
- 地图图层组件与业务筛选条件解耦。
- 统一响应、分页、错误码和审计字段在公共模块维护。

## 7. 可测试性设计

- 业务模块通过 service 层隔离，便于单元测试。
- PostGIS 查询使用 Testcontainers 做集成测试。
- 地图组件使用固定样例数据做组件测试和人工冒烟验证。
- 轨迹算法使用确定性样例数据验证边界场景。

## 8. 风险提示

- 执行风险：第一阶段如果直接拆成完整微服务，会增加启动、调试和部署成本。
- 性能风险：历史轨迹查询必须限制时间范围，并做分页或抽稀。
- 错误风险：经纬度字段必须和 PostGIS `geom` 保持一致，否则空间查询结果会偏差。
- 扩展风险：Kafka/Flink 引入后要重新定义幂等和补偿策略。
