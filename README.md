# STIP

`STIP` 是 **SpatioTemporal Insight Platform** 的简称，中文名为 **多源时空轨迹智能分析平台**。项目面向智慧城市、园区安防、车辆监管、物流调度、人员安全管理等场景，提供多源位置数据接入、时空存储、轨迹查询、轨迹回放、空间检索、围栏告警和统计分析能力。

## 项目定位

本项目以人员、车辆、设备的轨迹数据分析为核心，按照“可演示 MVP -> 消息削峰与实时计算 -> 离线大数据分析 -> 工程化展示”的路线推进。

第一阶段优先实现可落地闭环：

- Spring Boot 后端。
- PostgreSQL + PostGIS 时空数据存储。
- Redis 实时位置缓存与 GEO 查询。
- Vue 3 + TypeScript 管理后台。
- 位置上报、实时位置、历史轨迹、附近查询、电子围栏、基础告警。

后续阶段逐步引入 Kafka、Flink、Spark、HDFS/HBase、Elasticsearch、监控与压测能力。

## 核心能力

- 多源位置事件接入：GPS、Wi-Fi、蓝牙、摄像头识别、传感器和业务系统事件。
- 标准化轨迹模型：统一实体、位置事件、轨迹点、围栏和告警数据结构。
- 实时监控：地图展示最新位置，支持实体类型、状态和区域筛选。
- 历史轨迹：按实体和时间范围查询，支持轨迹回放和轨迹抽稀。
- 空间查询：附近实体查询、区域内实体查询、电子围栏管理。
- 轨迹挖掘：停留点识别、速度异常检测、漂移点检测。
- 告警管理：围栏进出、异常速度、离线超时等告警处理。
- 统计分析：轨迹点数量、活跃实体、告警趋势、停留点 Top N。

## 技术栈规划

### 后端

- Java 17
- Spring Boot 3
- Spring Security + JWT + RBAC
- MyBatis Plus 或 Spring Data JDBC
- Springdoc OpenAPI
- JUnit 5、Mockito、Testcontainers

### 前端

- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router
- ECharts
- Leaflet
- Axios

### 数据与中间件

- PostgreSQL + PostGIS
- Redis + Redis GEO
- Kafka
- Flink
- Spark
- Elasticsearch
- HDFS/HBase
- Docker Compose

## 建议目录结构

```text
stip/
  backend/
    common/
    gateway-service/
    auth-service/
    ingestion-service/
    trajectory-service/
    fence-service/
    alert-service/
    analytics-service/
  compute/
    flink-jobs/
    spark-jobs/
  frontend/
    admin-web/
  deploy/
    docker-compose.yml
    sql/
  docs/
    architecture.md
    database.md
    api.md
    data-pipeline.md
    algorithm.md
    deployment.md
    testing.md
  tests/
    sample-data/
    performance/
  PLAN.md
  系统设计说明书.md
```

第一阶段可以先采用模块化单体或轻量多模块后端，避免过早引入完整微服务复杂度。

## 本地开发

当前仓库尚未初始化源码工程，暂不提供可执行启动命令。工程初始化后应补充：

```bash
# 启动基础依赖
docker compose up -d

# 启动后端

# 启动前端

# 执行测试

# 构建
```

## 交付标准

每个功能交付前必须检查：

- 是否符合已确认需求和验收标准。
- 是否具备可复用设计，避免重复实现。
- 是否具备测试或明确的人工验证记录。
- 是否同步更新 README、PLAN 或 `docs/` 文档。
- 是否记录执行风险、已知错误和后续处理建议。

## 风险提示

- 执行风险：一次性实现完整微服务、大数据和算法链路会拉长周期，建议先完成 MVP 闭环。
- 资源风险：Kafka、Flink、Spark、Elasticsearch、HDFS/HBase 同时本地运行会占用较高内存。
- 地图风险：第三方地图服务可能受网络和 Key 限制，演示环境需要准备降级方案。
- 数据风险：摄像头和人员识别数据涉及隐私合规，第一阶段建议使用模拟数据。
- 测试风险：轨迹算法必须配套样例数据和测试用例，否则难以证明准确性。

## 参考文档

- `系统设计说明书.md`：完整系统设计、架构、数据模型、接口、算法和阶段规划。
- `PLAN.md`：当前开发执行计划和阶段任务。
