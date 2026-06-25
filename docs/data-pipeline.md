# 数据链路设计说明

## 1. 目标

数据链路负责将多源位置事件转换为统一轨迹数据，并支撑实时告警、缓存更新、历史查询和离线分析。

## 2. 阶段划分

| 阶段 | 链路 | 目标 |
| --- | --- | --- |
| MVP | API -> DB/Redis/规则判断 | 快速打通演示闭环 |
| 实时计算 | API -> Kafka -> Flink -> DB/Redis/告警 | 支撑高并发接入和实时清洗 |
| 离线分析 | Kafka/HDFS -> Spark -> 统计结果 | 支撑历史趋势和行为模式 |

## 3. MVP 链路

```text
设备/模拟器
  |
  v
位置上报 API
  |
  +--> 参数校验
  +--> 标准化
  +--> 幂等校验
  +--> 写入 trajectory_point
  +--> 更新 latest_location / Redis GEO
  +--> 执行基础告警规则
  +--> 写入 alert_event
```

适用场景：

- 本地演示。
- 小规模样例数据。
- 前后端联调。
- 验证数据模型和空间查询。

## 4. Kafka Topic 规划

| Topic | 数据 | 生产者 | 消费者 |
| --- | --- | --- | --- |
| `raw-location-topic` | 原始标准位置事件 | `ingestion-service` | Flink 清洗任务 |
| `clean-location-topic` | 清洗后的轨迹点 | Flink | 轨迹落库消费者 |
| `alert-topic` | 实时告警事件 | Flink | `alert-service` |
| `dead-location-topic` | 无法处理的位置事件 | Flink/消费者 | 人工排查或补偿任务 |

## 5. 事件模型

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
  "eventTime": "2026-06-25T09:30:00+08:00",
  "traceId": "trace-20260625-000001"
}
```

## 6. Flink 处理步骤

1. 从 `raw-location-topic` 消费事件。
2. 按 `entityCode` 分区。
3. 解析并校验经纬度、时间和实体类型。
4. 基于 `eventId` 去重。
5. 按事件时间处理乱序数据。
6. 计算速度异常、漂移点和围栏事件。
7. 输出清洗轨迹点到 `clean-location-topic`。
8. 输出告警到 `alert-topic`。
9. 无法处理的数据进入 `dead-location-topic`。

## 7. Spark 离线分析

输入：

- HDFS 原始位置事件。
- PostgreSQL/PostGIS 历史轨迹。
- HBase 轨迹宽表，可选。

输出：

- 每日轨迹点数量。
- 活跃实体数量。
- 实体移动距离。
- 停留点 Top N。
- 告警趋势。
- 数据源质量统计。

## 8. 幂等与补偿

- 位置事件以 `eventId` 作为业务幂等键。
- 轨迹落库对 `eventId` 建唯一索引。
- Kafka 消费者记录消费失败原因和重试次数。
- 超过重试次数的数据写入死信 topic。
- 补偿任务从死信 topic 或错误表重新处理。

## 9. 监控指标

| 指标 | 说明 |
| --- | --- |
| API QPS | 位置上报请求量 |
| Kafka lag | 消费堆积 |
| Flink checkpoint 状态 | 实时任务稳定性 |
| 清洗丢弃数 | 非法事件数量 |
| 告警生成数 | 规则触发趋势 |
| DB 写入耗时 | 落库性能 |
| Redis 更新耗时 | 最新位置缓存性能 |

## 10. 风险提示

- 执行风险：第二阶段引入 Kafka/Flink 后，需要重新验证端到端一致性。
- 错误风险：只依赖 Kafka offset 不能替代业务幂等，必须保留 `eventId` 唯一约束。
- 性能风险：Flink 规则计算和数据库写入必须解耦，否则实时任务可能反压。
- 运维风险：死信数据如果无人处理，会造成数据缺口。
