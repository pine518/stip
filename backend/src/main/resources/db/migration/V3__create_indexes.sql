create index idx_tracked_entity_type_status on tracked_entity(entity_type, status);

create index idx_trajectory_point_entity_time on trajectory_point(entity_id, event_time desc);
create index idx_trajectory_point_time on trajectory_point(event_time desc);
create index idx_trajectory_point_geom on trajectory_point using gist(geom);

create index idx_latest_location_geom on latest_location using gist(geom);
create index idx_latest_location_event_time on latest_location(event_time desc);

create index idx_geo_fence_enabled on geo_fence(enabled);
create index idx_geo_fence_geometry on geo_fence using gist(geometry);

create index idx_alert_event_status_time on alert_event(status, event_time desc);
create index idx_alert_event_entity_time on alert_event(entity_id, event_time desc);
create index idx_alert_event_type_time on alert_event(alert_type, event_time desc);

create index idx_stay_point_entity_time on stay_point(entity_id, start_time desc);
create index idx_stay_point_geom on stay_point using gist(geom);

