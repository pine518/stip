# 数据库设计说明

## 1. 设计目标

数据库设计优先支撑第一阶段 MVP：实体管理、位置事件写入、最新位置查询、历史轨迹查询、空间检索、电子围栏和告警管理。数据库采用 PostgreSQL + PostGIS。

## 2. 命名规范

- 表名使用小写下划线。
- 主键统一使用 `id`。
- 时间字段统一使用 `created_at`、`updated_at`。
- 业务唯一标识使用 `*_code` 或 `event_id`。
- 空间字段使用 `geometry` 类型，字段名优先为 `geom` 或 `geometry`。

## 3. 核心表

### 3.1 tracked_entity

记录人员、车辆、设备等被追踪对象。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | bigint | PK | 主键 |
| `entity_code` | varchar(64) | unique, not null | 业务编码 |
| `entity_name` | varchar(128) | not null | 名称 |
| `entity_type` | varchar(32) | not null | `PERSON`、`VEHICLE`、`DEVICE` |
| `status` | varchar(32) | not null | `ACTIVE`、`INACTIVE`、`OFFLINE` |
| `owner_name` | varchar(128) | nullable | 负责人 |
| `created_at` | timestamptz | not null | 创建时间 |
| `updated_at` | timestamptz | not null | 更新时间 |

建议索引：

```sql
create unique index uk_tracked_entity_code on tracked_entity(entity_code);
create index idx_tracked_entity_type_status on tracked_entity(entity_type, status);
```

### 3.2 trajectory_point

记录标准化后的轨迹点。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | bigint | PK | 主键 |
| `event_id` | varchar(96) | unique, not null | 事件幂等 ID |
| `entity_id` | bigint | FK, not null | 实体 ID |
| `entity_type` | varchar(32) | not null | 实体类型冗余 |
| `source_type` | varchar(32) | not null | 数据来源 |
| `longitude` | numeric(10, 7) | not null | 经度 |
| `latitude` | numeric(10, 7) | not null | 纬度 |
| `speed` | numeric(10, 2) | nullable | 速度，单位 km/h |
| `direction` | numeric(6, 2) | nullable | 方向角 |
| `accuracy` | numeric(10, 2) | nullable | 精度，单位 m |
| `event_time` | timestamptz | not null | 事件时间 |
| `geom` | geometry(Point, 4326) | not null | 空间点 |
| `geohash` | varchar(32) | nullable | GeoHash |
| `created_at` | timestamptz | not null | 入库时间 |

建议索引：

```sql
create unique index uk_trajectory_point_event_id on trajectory_point(event_id);
create index idx_trajectory_point_entity_time on trajectory_point(entity_id, event_time desc);
create index idx_trajectory_point_time on trajectory_point(event_time desc);
create index idx_trajectory_point_geom on trajectory_point using gist(geom);
```

### 3.3 latest_location

保存实体最新位置，降低实时监控查询成本。也可以只放 Redis，第一阶段建议数据库保底。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `entity_id` | bigint | PK | 实体 ID |
| `trajectory_point_id` | bigint | not null | 最新轨迹点 ID |
| `longitude` | numeric(10, 7) | not null | 经度 |
| `latitude` | numeric(10, 7) | not null | 纬度 |
| `speed` | numeric(10, 2) | nullable | 速度 |
| `direction` | numeric(6, 2) | nullable | 方向角 |
| `event_time` | timestamptz | not null | 最新事件时间 |
| `geom` | geometry(Point, 4326) | not null | 空间点 |
| `updated_at` | timestamptz | not null | 更新时间 |

建议索引：

```sql
create index idx_latest_location_geom on latest_location using gist(geom);
create index idx_latest_location_event_time on latest_location(event_time desc);
```

### 3.4 geo_fence

记录圆形或多边形电子围栏。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | bigint | PK | 主键 |
| `fence_name` | varchar(128) | not null | 围栏名称 |
| `fence_type` | varchar(32) | not null | `CIRCLE`、`POLYGON` |
| `rule_type` | varchar(32) | not null | `ENTER`、`LEAVE`、`STAY_TIMEOUT` |
| `geometry` | geometry(Geometry, 4326) | not null | 围栏空间范围 |
| `enabled` | boolean | not null | 是否启用 |
| `created_at` | timestamptz | not null | 创建时间 |
| `updated_at` | timestamptz | not null | 更新时间 |

建议索引：

```sql
create index idx_geo_fence_enabled on geo_fence(enabled);
create index idx_geo_fence_geometry on geo_fence using gist(geometry);
```

### 3.5 alert_event

记录围栏、速度、漂移和离线等告警。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | bigint | PK | 主键 |
| `alert_type` | varchar(32) | not null | 告警类型 |
| `alert_level` | varchar(16) | not null | `LOW`、`MEDIUM`、`HIGH` |
| `entity_id` | bigint | not null | 实体 ID |
| `fence_id` | bigint | nullable | 围栏 ID |
| `message` | varchar(512) | not null | 告警内容 |
| `longitude` | numeric(10, 7) | nullable | 经度 |
| `latitude` | numeric(10, 7) | nullable | 纬度 |
| `event_time` | timestamptz | not null | 告警时间 |
| `status` | varchar(32) | not null | `OPEN`、`ACKED`、`CLOSED` |
| `handled_by` | bigint | nullable | 处理人 |
| `handled_at` | timestamptz | nullable | 处理时间 |
| `created_at` | timestamptz | not null | 创建时间 |

建议索引：

```sql
create index idx_alert_event_status_time on alert_event(status, event_time desc);
create index idx_alert_event_entity_time on alert_event(entity_id, event_time desc);
create index idx_alert_event_type_time on alert_event(alert_type, event_time desc);
```

### 3.6 stay_point

保存算法识别出的停留点。

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | bigint | PK | 主键 |
| `entity_id` | bigint | not null | 实体 ID |
| `longitude` | numeric(10, 7) | not null | 经度 |
| `latitude` | numeric(10, 7) | not null | 纬度 |
| `geom` | geometry(Point, 4326) | not null | 空间点 |
| `start_time` | timestamptz | not null | 开始时间 |
| `end_time` | timestamptz | not null | 结束时间 |
| `duration_seconds` | integer | not null | 停留时长 |
| `point_count` | integer | not null | 点位数量 |
| `confidence` | numeric(5, 2) | nullable | 置信度 |

建议索引：

```sql
create index idx_stay_point_entity_time on stay_point(entity_id, start_time desc);
create index idx_stay_point_geom on stay_point using gist(geom);
```

## 4. 分区策略

`trajectory_point` 是高频写入表，应尽早采用按时间范围分区。

建议策略：

- MVP 阶段：单表 + 必要索引。
- 数据量达到百万级：按月分区。
- 高频演示或压测：按日分区。

示例：

```sql
create table trajectory_point (
  id bigint generated always as identity,
  event_id varchar(96) not null,
  entity_id bigint not null,
  event_time timestamptz not null,
  geom geometry(Point, 4326) not null,
  created_at timestamptz not null,
  primary key (id, event_time)
) partition by range (event_time);
```

## 5. 空间查询建议

附近实体查询：

```sql
select *
from latest_location
where st_dwithin(
  geom::geography,
  st_setsrid(st_makepoint(:lng, :lat), 4326)::geography,
  :radius_meter
)
order by st_distance(
  geom::geography,
  st_setsrid(st_makepoint(:lng, :lat), 4326)::geography
)
limit :limit;
```

区域内实体查询：

```sql
select *
from latest_location
where st_contains(:polygon_geometry, geom);
```

轨迹回放查询：

```sql
select *
from trajectory_point
where entity_id = :entity_id
  and event_time between :start_time and :end_time
order by event_time asc;
```

## 6. 可复用性与可测试性

- 表结构应通过迁移工具管理，建议使用 Flyway。
- 空间查询 SQL 应封装在 repository 层，便于集成测试。
- 轨迹点写入必须围绕 `event_id` 做幂等。
- 数据库测试必须覆盖 PostGIS 扩展初始化和空间索引查询。

## 7. 风险提示

- 执行风险：不做分区时，历史轨迹查询会随数据量增长明显变慢。
- 错误风险：经纬度顺序必须使用 `longitude, latitude`，不能反写。
- 性能风险：对 `geom::geography` 的距离查询要结合范围和 limit 控制成本。
- 维护风险：如果不使用迁移工具，后续多环境数据库结构会不一致。
