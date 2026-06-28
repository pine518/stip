package com.stip.alert.domain;

import com.stip.alert.api.dto.AlertResponse;
import com.stip.alert.api.dto.HandleAlertRequest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertService {

    private final JdbcClient jdbcClient;

    public AlertService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<AlertResponse> findAll(String status) {
        String statusFilter = status == null || status.isBlank() ? null : status;
        if (statusFilter == null) {
            return jdbcClient.sql("""
                            select a.id,
                                   a.alert_type,
                                   a.alert_level,
                                   a.entity_id,
                                   e.entity_code,
                                   e.entity_name,
                                   a.fence_id,
                                   a.message,
                                   a.longitude,
                                   a.latitude,
                                   a.event_time,
                                   a.status,
                                   a.handled_by,
                                   a.handled_at,
                                   a.created_at
                            from alert_event a
                            join tracked_entity e on e.id = a.entity_id
                            order by a.event_time desc
                            """)
                    .query(AlertResponse.class)
                    .list();
        }

        return jdbcClient.sql("""
                        select a.id,
                               a.alert_type,
                               a.alert_level,
                               a.entity_id,
                               e.entity_code,
                               e.entity_name,
                               a.fence_id,
                               a.message,
                               a.longitude,
                               a.latitude,
                               a.event_time,
                               a.status,
                               a.handled_by,
                               a.handled_at,
                               a.created_at
                        from alert_event a
                        join tracked_entity e on e.id = a.entity_id
                        where a.status = :status
                        order by a.event_time desc
                        """)
                .param("status", statusFilter)
                .query(AlertResponse.class)
                .list();
    }

    @Transactional
    public AlertResponse handle(Long id, HandleAlertRequest request) {
        jdbcClient.sql("""
                        update alert_event
                        set status = 'ACKED',
                            handled_by = :handledBy,
                            handled_at = now()
                        where id = :id
                          and status = 'OPEN'
                        """)
                .param("id", id)
                .param("handledBy", request.handledBy() == null ? 0L : request.handledBy())
                .update();
        return findById(id);
    }

    @Transactional
    public AlertResponse close(Long id, HandleAlertRequest request) {
        jdbcClient.sql("""
                        update alert_event
                        set status = 'CLOSED',
                            handled_by = :handledBy,
                            handled_at = now()
                        where id = :id
                          and status in ('OPEN', 'ACKED')
                        """)
                .param("id", id)
                .param("handledBy", request.handledBy() == null ? 0L : request.handledBy())
                .update();
        return findById(id);
    }

    private AlertResponse findById(Long id) {
        return jdbcClient.sql("""
                        select a.id,
                               a.alert_type,
                               a.alert_level,
                               a.entity_id,
                               e.entity_code,
                               e.entity_name,
                               a.fence_id,
                               a.message,
                               a.longitude,
                               a.latitude,
                               a.event_time,
                               a.status,
                               a.handled_by,
                               a.handled_at,
                               a.created_at
                        from alert_event a
                        join tracked_entity e on e.id = a.entity_id
                        where a.id = :id
                        """)
                .param("id", id)
                .query(AlertResponse.class)
                .single();
    }
}
