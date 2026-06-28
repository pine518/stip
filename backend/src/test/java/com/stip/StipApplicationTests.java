package com.stip;

import org.junit.jupiter.api.Test;
import com.stip.alert.domain.AlertService;
import com.stip.fence.domain.FenceService;
import com.stip.location.domain.LocationEventService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class StipApplicationTests {

    @MockBean
    private LocationEventService locationEventService;

    @MockBean
    private FenceService fenceService;

    @MockBean
    private AlertService alertService;

    @Test
    void contextLoads() {
    }
}
