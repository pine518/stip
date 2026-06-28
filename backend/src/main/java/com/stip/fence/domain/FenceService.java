package com.stip.fence.domain;

import com.stip.fence.api.dto.CreateFenceRequest;
import com.stip.fence.api.dto.FenceResponse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FenceService {

    private final JdbcClient jdbcClient;

    public FenceService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<FenceResponse> findAll() {
        return jdbcClient.sql("""
                        select id,
                               fence_name,
                               fence_type,
                               rule_type,
                               st_asgeojson(geometry) as geometry_geo_json,
                               enabled,
                               created_at,
                               updated_at
                        from geo_fence
                        order by id desc
                        """)
                .query(FenceResponse.class)
                .list();
    }

    @Transactional
    public FenceResponse create(CreateFenceRequest request) {
        Long id = jdbcClient.sql("""
                        insert into geo_fence (fence_name, fence_type, rule_type, geometry, enabled)
                        values (
                            :fenceName,
                            :fenceType,
                            :ruleType,
                            st_geomfromtext(:geometryWkt, 4326),
                            :enabled
                        )
                        returning id
                        """)
                .param("fenceName", request.fenceName())
                .param("fenceType", request.fenceType())
                .param("ruleType", request.ruleType())
                .param("geometryWkt", request.geometryWkt())
                .param("enabled", request.enabled())
                .query(Long.class)
                .single();

        return jdbcClient.sql("""
                        select id,
                               fence_name,
                               fence_type,
                               rule_type,
                               st_asgeojson(geometry) as geometry_geo_json,
                               enabled,
                               created_at,
                               updated_at
                        from geo_fence
                        where id = :id
                        """)
                .param("id", id)
                .query(FenceResponse.class)
                .single();
    }

    @Transactional
    public void delete(Long id) {
        jdbcClient.sql("update alert_event set fence_id = null where fence_id = :id")
                .param("id", id)
                .update();
        jdbcClient.sql("delete from geo_fence where id = :id")
                .param("id", id)
                .update();
    }

    @Transactional
    public FenceResponse setEnabled(Long id, boolean enabled) {
        jdbcClient.sql("""
                        update geo_fence
                        set enabled = :enabled,
                            updated_at = now()
                        where id = :id
                        """)
                .param("id", id)
                .param("enabled", enabled)
                .update();

        return jdbcClient.sql("""
                        select id,
                               fence_name,
                               fence_type,
                               rule_type,
                               st_asgeojson(geometry) as geometry_geo_json,
                               enabled,
                               created_at,
                               updated_at
                        from geo_fence
                        where id = :id
                        """)
                .param("id", id)
                .query(FenceResponse.class)
                .single();
    }
}
