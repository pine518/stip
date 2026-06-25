# 测试说明

## 1. 测试目标

测试体系需要覆盖后端业务逻辑、空间查询、轨迹算法、接口契约、前端关键交互和性能风险。第一阶段以可复现的单元测试、集成测试和冒烟验证为主。

## 2. 测试分层

| 层级 | 工具 | 目标 |
| --- | --- | --- |
| 单元测试 | JUnit 5、Mockito、Vitest | 算法、service、组件逻辑 |
| 集成测试 | Testcontainers | PostgreSQL/PostGIS、Redis |
| 接口测试 | MockMvc、RestAssured | REST API 契约 |
| 前端测试 | Vitest、Playwright 可选 | 页面流程和组件交互 |
| 压测 | JMeter、Gatling | 写入和查询性能 |

## 3. 后端单元测试

必须覆盖：

- 位置事件参数校验。
- 事件标准化。
- `eventId` 幂等判断。
- 距离计算。
- 速度异常检测。
- 漂移点检测。
- 停留点识别。
- 轨迹抽稀。

命名建议：

```text
TrajectoryAlgorithmTest
LocationEventValidatorTest
GeoDistanceTest
AlertRuleServiceTest
```

## 4. 集成测试

PostGIS 测试：

- 空间扩展初始化。
- 点位写入。
- 附近实体查询。
- 多边形围栏包含判断。
- 空间索引查询可执行。

Redis 测试：

- 最新位置缓存写入。
- GEO 半径查询。
- 缓存过期策略。

Kafka 测试后续补充：

- 生产消息。
- 消费消息。
- 重试。
- 死信。

## 5. API 测试

核心用例：

| 接口 | 用例 |
| --- | --- |
| `POST /location-events` | 成功、重复事件、非法经纬度、缺失实体 |
| `POST /location-events/batch` | 部分成功、全部失败、空数组 |
| `GET /entities/{id}/latest-location` | 有数据、无数据、无权限 |
| `GET /entities/{id}/trajectory` | 正常范围、范围过大、无数据 |
| `GET /geo/nearby` | 正常查询、半径非法、结果为空 |
| `POST /fences` | 圆形围栏、多边形围栏、非法 geometry |
| `POST /alerts/{id}/handle` | 确认、重复确认、无权限 |

## 6. 前端测试

关键流程：

- 地图加载并展示实体点位。
- 按实体类型筛选。
- 查询并播放历史轨迹。
- 创建电子围栏。
- 查看并处理告警。
- 统计看板展示图表。

组件测试重点：

- 地图组件接收点位数据后渲染图层。
- 轨迹播放器的播放、暂停、倍速。
- 围栏表单校验。
- 告警状态切换。

## 7. 压测计划

第一阶段重点压测：

| 场景 | 指标 |
| --- | --- |
| 单条位置上报 | QPS、P95、错误率 |
| 批量位置上报 | 吞吐量、入库耗时 |
| 最新位置查询 | P95、Redis 命中率 |
| 历史轨迹查询 | P95、返回点数 |
| 附近实体查询 | P95、空间索引效果 |

压测数据建议：

- 100 个实体。
- 每个实体 1,000 个轨迹点。
- 时间跨度 7 天。
- 至少 5 个电子围栏。

## 8. 冒烟验证

每次交付前至少执行：

1. 启动数据库、Redis、后端、前端。
2. 创建或导入实体。
3. 上报一条位置事件。
4. 查询最新位置。
5. 查询历史轨迹。
6. 创建电子围栏。
7. 触发一条告警。
8. 前端地图确认位置和轨迹可见。

## 9. 测试数据

建议目录：

```text
tests/sample-data/
  entities.json
  location-events-small.json
  location-events-invalid.json
  trajectory-shanghai.json
  fences.json
```

## 10. 风险提示

- 执行风险：没有 Testcontainers 时，PostGIS 查询容易只在本机通过。
- 错误风险：轨迹算法如果没有边界用例，速度异常和停留点结果会不稳定。
- 性能风险：只测少量数据无法暴露历史轨迹查询和地图渲染问题。
- 文档风险：测试命令必须写入 README，否则后续无法复现验证结果。
