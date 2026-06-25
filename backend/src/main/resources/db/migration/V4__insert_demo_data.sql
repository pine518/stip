insert into tracked_entity (entity_code, entity_name, entity_type, status, owner_name)
values
    ('CAR-001', '巡检车 1', 'VEHICLE', 'ACTIVE', 'ops-team'),
    ('PERSON-001', '巡检员 1', 'PERSON', 'ACTIVE', 'ops-team'),
    ('DEVICE-001', '移动设备 1', 'DEVICE', 'ACTIVE', 'iot-team');

insert into trajectory_point (
    event_id,
    entity_id,
    entity_type,
    source_type,
    longitude,
    latitude,
    speed,
    direction,
    accuracy,
    event_time,
    geom,
    geohash
)
select
    'demo-gps-001',
    id,
    entity_type,
    'GPS',
    121.473701,
    31.230416,
    42.50,
    90.00,
    8.00,
    now(),
    st_setsrid(st_makepoint(121.473701, 31.230416), 4326),
    'wtw3sj'
from tracked_entity
where entity_code = 'CAR-001';

insert into latest_location (
    entity_id,
    trajectory_point_id,
    longitude,
    latitude,
    speed,
    direction,
    event_time,
    geom
)
select
    tp.entity_id,
    tp.id,
    tp.longitude,
    tp.latitude,
    tp.speed,
    tp.direction,
    tp.event_time,
    tp.geom
from trajectory_point tp
where tp.event_id = 'demo-gps-001';

insert into geo_fence (fence_name, fence_type, rule_type, geometry, enabled)
values (
    '上海演示围栏',
    'POLYGON',
    'ENTER',
    st_geomfromtext('POLYGON((121.470000 31.228000,121.480000 31.228000,121.480000 31.236000,121.470000 31.236000,121.470000 31.228000))', 4326),
    true
);

