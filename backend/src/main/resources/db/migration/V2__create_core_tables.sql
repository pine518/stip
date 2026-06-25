create table tracked_entity (
    id bigserial primary key,
    entity_code varchar(64) not null,
    entity_name varchar(128) not null,
    entity_type varchar(32) not null,
    status varchar(32) not null default 'ACTIVE',
    owner_name varchar(128),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint uk_tracked_entity_code unique (entity_code),
    constraint ck_tracked_entity_type check (entity_type in ('PERSON', 'VEHICLE', 'DEVICE')),
    constraint ck_tracked_entity_status check (status in ('ACTIVE', 'INACTIVE', 'OFFLINE'))
);

create table trajectory_point (
    id bigserial primary key,
    event_id varchar(96) not null,
    entity_id bigint not null references tracked_entity(id),
    entity_type varchar(32) not null,
    source_type varchar(32) not null,
    longitude numeric(10, 7) not null,
    latitude numeric(10, 7) not null,
    speed numeric(10, 2),
    direction numeric(6, 2),
    accuracy numeric(10, 2),
    event_time timestamptz not null,
    geom geometry(Point, 4326) not null,
    geohash varchar(32),
    created_at timestamptz not null default now(),
    constraint uk_trajectory_point_event_id unique (event_id),
    constraint ck_trajectory_point_longitude check (longitude between -180 and 180),
    constraint ck_trajectory_point_latitude check (latitude between -90 and 90)
);

create table latest_location (
    entity_id bigint primary key references tracked_entity(id),
    trajectory_point_id bigint not null references trajectory_point(id),
    longitude numeric(10, 7) not null,
    latitude numeric(10, 7) not null,
    speed numeric(10, 2),
    direction numeric(6, 2),
    event_time timestamptz not null,
    geom geometry(Point, 4326) not null,
    updated_at timestamptz not null default now(),
    constraint ck_latest_location_longitude check (longitude between -180 and 180),
    constraint ck_latest_location_latitude check (latitude between -90 and 90)
);

create table geo_fence (
    id bigserial primary key,
    fence_name varchar(128) not null,
    fence_type varchar(32) not null,
    rule_type varchar(32) not null,
    geometry geometry(Geometry, 4326) not null,
    enabled boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint ck_geo_fence_type check (fence_type in ('CIRCLE', 'POLYGON')),
    constraint ck_geo_fence_rule_type check (rule_type in ('ENTER', 'LEAVE', 'STAY_TIMEOUT'))
);

create table alert_event (
    id bigserial primary key,
    alert_type varchar(32) not null,
    alert_level varchar(16) not null,
    entity_id bigint not null references tracked_entity(id),
    fence_id bigint references geo_fence(id),
    message varchar(512) not null,
    longitude numeric(10, 7),
    latitude numeric(10, 7),
    event_time timestamptz not null,
    status varchar(32) not null default 'OPEN',
    handled_by bigint,
    handled_at timestamptz,
    created_at timestamptz not null default now(),
    constraint ck_alert_event_level check (alert_level in ('LOW', 'MEDIUM', 'HIGH')),
    constraint ck_alert_event_status check (status in ('OPEN', 'ACKED', 'CLOSED'))
);

create table stay_point (
    id bigserial primary key,
    entity_id bigint not null references tracked_entity(id),
    longitude numeric(10, 7) not null,
    latitude numeric(10, 7) not null,
    geom geometry(Point, 4326) not null,
    start_time timestamptz not null,
    end_time timestamptz not null,
    duration_seconds integer not null,
    point_count integer not null,
    confidence numeric(5, 2),
    constraint ck_stay_point_time check (end_time >= start_time),
    constraint ck_stay_point_duration check (duration_seconds >= 0),
    constraint ck_stay_point_count check (point_count > 0)
);

