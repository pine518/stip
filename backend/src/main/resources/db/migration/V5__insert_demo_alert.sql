insert into alert_event (
    alert_type,
    alert_level,
    entity_id,
    fence_id,
    message,
    longitude,
    latitude,
    event_time,
    status
)
select
    'FENCE_ENTER',
    'MEDIUM',
    e.id,
    f.id,
    '巡检车 1 进入上海演示围栏',
    121.473701,
    31.230416,
    now(),
    'OPEN'
from tracked_entity e
cross join geo_fence f
where e.entity_code = 'CAR-001'
  and f.fence_name = '上海演示围栏'
  and not exists (
      select 1
      from alert_event a
      where a.alert_type = 'FENCE_ENTER'
        and a.entity_id = e.id
        and a.fence_id = f.id
  );

