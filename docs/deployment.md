# 部署说明

## 1. 部署目标

第一阶段部署目标是本地可演示、依赖可复现、服务可启动。后续再扩展到完整 Docker Compose 和生产 Kubernetes 方案。

## 2. 本地依赖

建议本地准备：

- JDK 17
- Maven 3.9+
- Node.js 20+
- npm 或 pnpm
- Docker Desktop
- Git

## 3. Docker Compose 依赖规划

第一阶段：

```text
postgres-postgis
redis
```

第二阶段：

```text
kafka
zookeeper 或 kraft kafka
elasticsearch
nacos
```

第三阶段：

```text
flink-jobmanager
flink-taskmanager
spark
hdfs
hbase
```

## 4. 建议环境变量

后端：

```text
SPRING_PROFILES_ACTIVE=dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=stip
DB_USERNAME=stip
DB_PASSWORD=stip
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=replace-me
```

前端：

```text
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_MAP_PROVIDER=leaflet
```

## 5. 本地启动流程

工程初始化后使用以下流程：

```bash
docker compose up -d

# 后端
cd backend
mvn spring-boot:run

# 前端
cd frontend/admin-web
npm install
npm run dev
```

## 6. 数据库初始化

PostGIS 必须启用扩展：

```sql
create extension if not exists postgis;
```

建议使用 Flyway 管理迁移：

```text
deploy/sql/
  V1__init_extension.sql
  V2__create_core_tables.sql
  V3__create_indexes.sql
  V4__insert_demo_data.sql
```

## 7. 演示环境部署

演示环境建议先保持简单：

```text
docker compose
  - postgres-postgis
  - redis
  - backend
  - admin-web
```

通过模拟数据脚本生成轨迹事件，验证：

- 位置上报。
- 最新位置。
- 历史轨迹。
- 附近实体。
- 围栏告警。
- 前端地图展示。

## 8. 生产部署设想

生产环境建议：

- 应用服务容器化。
- 数据库和中间件使用独立集群或云托管服务。
- 网关统一暴露 API。
- Prometheus + Grafana 采集指标。
- ELK 或 OpenSearch 采集日志。
- 数据库定期备份。
- Kafka topic 设置合理分区和保留策略。

## 9. 验证清单

部署完成后检查：

- 后端健康检查接口正常。
- 前端可访问。
- 数据库连接正常。
- PostGIS 扩展已启用。
- Redis 可读写。
- 位置上报接口可写入轨迹点。
- 地图可展示最新位置。

## 10. 风险提示

- 执行风险：本地 Docker 资源不足时，不要同时启动所有大数据组件。
- 错误风险：PostGIS 扩展未启用会导致空间字段和查询失败。
- 安全风险：演示环境不能使用默认 JWT 密钥和数据库密码。
- 运维风险：Kafka、Flink、Spark 引入后要补充日志、指标和重启策略。
