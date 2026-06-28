package com.stip.location.domain;

import com.stip.location.api.dto.AnalyticsOverviewResponse;
import com.stip.location.api.dto.EntitySummaryResponse;
import com.stip.location.api.dto.LatestLocationResponse;
import com.stip.location.api.dto.LocationEventRequest;
import com.stip.location.api.dto.LocationEventResponse;
import com.stip.location.api.dto.TrajectoryPointResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LocationEventService {

    private final JdbcClient jdbcClient;

    public LocationEventService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Transactional
    public LocationEventResponse accept(LocationEventRequest request) {
        Long entityId = findEntityId(request.entityCode())
                .orElseGet(() -> createEntity(request));

        try {
            Long pointId = insertTrajectoryPoint(entityId, request);
            upsertLatestLocation(entityId, pointId, request);
            createFenceAlerts(entityId, request);
            return new LocationEventResponse(pointId, true);
        } catch (DuplicateKeyException ex) {
            Long existingPointId = jdbcClient.sql("select id from trajectory_point where event_id = :eventId")
                    .param("eventId", request.eventId())
                    .query(Long.class)
                    .single();
            return new LocationEventResponse(existingPointId, false);
        }
    }

    public List<LatestLocationResponse> findLatestLocations() {
        return jdbcClient.sql("""
                        select e.id as entity_id,
                               e.entity_code,
                               e.entity_name,
                               e.entity_type,
                               l.longitude,
                               l.latitude,
                               l.speed,
                               l.direction,
                               l.event_time
                        from latest_location l
                        join tracked_entity e on e.id = l.entity_id
                        order by l.event_time desc
                        """)
                .query(LatestLocationResponse.class)
                .list();
    }

    public List<EntitySummaryResponse> findEntities() {
        return jdbcClient.sql("""
                        select id,
                               entity_code,
                               entity_name,
                               entity_type,
                               status
                        from tracked_entity
                        order by id asc
                        """)
                .query(EntitySummaryResponse.class)
                .list();
    }

    public List<TrajectoryPointResponse> findTrajectory(Long entityId, OffsetDateTime startTime, OffsetDateTime endTime) {
        return jdbcClient.sql("""
                        select id,
                               longitude,
                               latitude,
                               speed,
                               direction,
                               event_time
                        from trajectory_point
                        where entity_id = :entityId
                          and event_time between :startTime and :endTime
                        order by event_time asc
                        """)
                .param("entityId", entityId)
                .param("startTime", Timestamp.from(startTime.toInstant()))
                .param("endTime", Timestamp.from(endTime.toInstant()))
                .query(TrajectoryPointResponse.class)
                .list();
    }

    public AnalyticsOverviewResponse overview() {
        Integer onlineEntities = jdbcClient.sql("select count(*) from tracked_entity where status = 'ACTIVE'")
                .query(Integer.class)
                .single();
        Integer todayTrajectoryPoints = jdbcClient.sql("""
                        select count(*)
                        from trajectory_point
                        where event_time >= date_trunc('day', now())
                        """)
                .query(Integer.class)
                .single();
        Integer enabledFences = jdbcClient.sql("select count(*) from geo_fence where enabled = true")
                .query(Integer.class)
                .single();
        Integer openAlerts = jdbcClient.sql("select count(*) from alert_event where status = 'OPEN'")
                .query(Integer.class)
                .single();
        return new AnalyticsOverviewResponse(onlineEntities, todayTrajectoryPoints, enabledFences, openAlerts);
    }

    private Optional<Long> findEntityId(String entityCode) {
        return jdbcClient.sql("select id from tracked_entity where entity_code = :entityCode")
                .param("entityCode", entityCode)
                .query(Long.class)
                .optional();
    }

    private Long createEntity(LocationEventRequest request) {
        return jdbcClient.sql("""
                        insert into tracked_entity (entity_code, entity_name, entity_type, status, owner_name)
                        values (:entityCode, :entityCode, :entityType, 'ACTIVE', 'simulator')
                        returning id
                        """)
                .param("entityCode", request.entityCode())
                .param("entityType", request.entityType())
                .query(Long.class)
                .single();
    }

    private Long insertTrajectoryPoint(Long entityId, LocationEventRequest request) {
        return jdbcClient.sql("""
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
                        values (
                            :eventId,
                            :entityId,
                            :entityType,
                            :sourceType,
                            :longitude,
                            :latitude,
                            :speed,
                            :direction,
                            :accuracy,
                            :eventTime,
                            st_setsrid(st_makepoint(:longitude, :latitude), 4326),
                            null
                        )
                        returning id
                        """)
                .param("eventId", request.eventId())
                .param("entityId", entityId)
                .param("entityType", request.entityType())
                .param("sourceType", request.sourceType())
                .param("longitude", request.longitude())
                .param("latitude", request.latitude())
                .param("speed", request.speed())
                .param("direction", request.direction())
                .param("accuracy", request.accuracy())
                .param("eventTime", Timestamp.from(request.eventTime().toInstant()))
                .query(Long.class)
                .single();
    }

    private void upsertLatestLocation(Long entityId, Long pointId, LocationEventRequest request) {
        jdbcClient.sql("""
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
                        values (
                            :entityId,
                            :pointId,
                            :longitude,
                            :latitude,
                            :speed,
                            :direction,
                            :eventTime,
                            st_setsrid(st_makepoint(:longitude, :latitude), 4326)
                        )
                        on conflict (entity_id) do update
                        set trajectory_point_id = excluded.trajectory_point_id,
                            longitude = excluded.longitude,
                            latitude = excluded.latitude,
                            speed = excluded.speed,
                            direction = excluded.direction,
                            event_time = excluded.event_time,
                            geom = excluded.geom,
                            updated_at = now()
                        where latest_location.event_time <= excluded.event_time
                        """)
                .param("entityId", entityId)
                .param("pointId", pointId)
                .param("longitude", request.longitude())
                .param("latitude", request.latitude())
                .param("speed", request.speed())
                .param("direction", request.direction())
                .param("eventTime", Timestamp.from(request.eventTime().toInstant()))
                .update();
    }

    private void createFenceAlerts(Long entityId, LocationEventRequest request) {
        List<FenceRule> matchedRules = jdbcClient.sql("""
                        select id,
                               fence_name,
                               rule_type
                        from geo_fence
                        where enabled = true
                          and (
                              (rule_type = 'ENTER' and st_covers(geometry, st_setsrid(st_makepoint(:longitude, :latitude), 4326)))
                              or
                              (rule_type = 'LEAVE' and not st_covers(geometry, st_setsrid(st_makepoint(:longitude, :latitude), 4326)))
                          )
                        """)
                .param("longitude", request.longitude())
                .param("latitude", request.latitude())
                .query(FenceRule.class)
                .list();

        for (FenceRule rule : matchedRules) {
            String alertType = "FENCE_" + rule.ruleType();
            String message = request.entityCode() + " 触发围栏规则：" + rule.fenceName();
            jdbcClient.sql("""
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
                            values (
                                :alertType,
                                'MEDIUM',
                                :entityId,
                                :fenceId,
                                :message,
                                :longitude,
                                :latitude,
                                :eventTime,
                                'OPEN'
                            )
                            """)
                    .param("alertType", alertType)
                    .param("entityId", entityId)
                    .param("fenceId", rule.id())
                    .param("message", message)
                    .param("longitude", request.longitude())
                    .param("latitude", request.latitude())
                    .param("eventTime", Timestamp.from(request.eventTime().toInstant()))
                    .update();
        }
    }

    private record FenceRule(Long id, String fenceName, String ruleType) {
    }
}
