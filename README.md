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

## 目录结构

```text
stip/
  backend/
    src/main/java/com/stip/
    src/main/resources/db/migration/
    pom.xml
  frontend/
    admin-web/
  deploy/
    docker-compose.yml
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

第一阶段采用 Spring Boot 模块化单体骨架，保留后续拆分微服务的包边界。

## 本地开发

### 环境要求

- JDK 17，当前推荐路径：`D:\jdk-17.0.17`
- Maven 3.9+，当前推荐路径：`D:\maven-3.9.9`
- Node.js 20+
- npm 10+
- Docker Desktop

如果 PowerShell 默认 `java` 仍指向 Java 8，请在当前终端设置：

```powershell
$env:JAVA_HOME="D:\jdk-17.0.17"
$env:Path="$env:JAVA_HOME\bin;D:\maven-3.9.9\bin;$env:Path"
```

### 数据库准备

当前默认使用本机 PostgreSQL：

```text
DB_HOST=localhost
DB_PORT=5432
DB_NAME=stip
DB_USERNAME=stip
DB_PASSWORD=stip
```

如果本机 PostgreSQL 中还没有 `stip` 用户和数据库，请用 PostgreSQL 超级用户执行：

```bash
psql -U postgres -f deploy/local-postgres-init.sql
```

如果 `psql` 不在 PATH，可以在 PostgreSQL 安装目录的 `bin` 目录下执行同等命令，或使用 pgAdmin 手动创建：

- database: `stip`
- user: `stip`
- password: `stip`
- extension: `postgis`

### Docker 依赖，可选

执行前请确认 Docker Desktop 已启动，并且 Linux Engine 正常运行。

```bash
docker compose up -d
```

当前 Compose 启动：

- PostgreSQL 16 + PostGIS 3.4，容器端口 `5432`，宿主机端口 `15432`
- Redis 7.4，容器端口 `6379`，宿主机端口 `16379`

Flyway 迁移脚本位于 `backend/src/main/resources/db/migration/`：

- `V1__init_extensions.sql`：启用 PostGIS。
- `V2__create_core_tables.sql`：创建实体、轨迹点、最新位置、围栏、告警、停留点核心表。
- `V3__create_indexes.sql`：创建普通索引和 GiST 空间索引。
- `V4__insert_demo_data.sql`：插入演示实体、轨迹点、最新位置和围栏。

### 启动后端

```bash
cd backend
mvn.cmd spring-boot:run
```

后端默认地址：

- API: `http://localhost:8080/api/v1`
- Health: `http://localhost:8080/api/v1/system/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

如果要连接 Docker 中的 PostGIS，请显式设置 `DB_PORT=15432`。

### 启动前端

```bash
cd frontend/admin-web
npm.cmd install
npm.cmd run dev
```

前端默认地址：`http://localhost:5173`

### 测试与构建

```bash
# 后端测试
cd backend
mvn.cmd test

# 前端构建
cd frontend/admin-web
npm.cmd run build
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
- `docs/architecture.md`：系统架构、服务边界和数据流。
- `docs/database.md`：核心表结构、PostGIS 索引和分区策略。
- `docs/api.md`：接口规范、请求响应、错误码和权限说明。
- `docs/data-pipeline.md`：MVP、Kafka/Flink、Spark 数据链路。
- `docs/algorithm.md`：轨迹清洗、停留点、异常检测和抽稀算法。
- `docs/deployment.md`：本地、演示和生产部署规划。
- `docs/testing.md`：测试分层、接口用例、压测和冒烟验证。
- `docs/frontend-interactions.md`：前端菜单、页面状态和功能交互逻辑。
