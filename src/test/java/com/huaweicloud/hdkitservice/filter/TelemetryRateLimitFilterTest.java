package com.huaweicloud.hdkitservice.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelemetryRateLimitFilterTest {

    private static final String USER_HASH_PATH = "/rest/developer/server/hdkitservice/user/generatorUserIDHash";
    private static final String TELEMETRY_PATH = "/rest/developer/server/hdkitservice/telemetry/events";

    private final TelemetryRateLimitFilter filter =
            new TelemetryRateLimitFilter(50, 10, 2, 20, 5, 2);

    @Test
    void nonLimitedPathPassesThrough() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/rest/developer/server/hdkitservice/voucher/status");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, new MockFilterChain());

        assertEquals(200, res.getStatus());
    }

    @Test
    void userHashEndpointAllowsUnderLimit() throws Exception {
        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", USER_HASH_PATH);
            req.addHeader("X-HW-AK", "AK");
            MockHttpServletResponse res = new MockHttpServletResponse();

            filter.doFilter(req, res, new MockFilterChain());

            assertEquals(200, res.getStatus());
        }
    }

    @Test
    void userHashEndpointLimitsByAk() throws Exception {
        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", USER_HASH_PATH);
            req.addHeader("X-HW-AK", "AK-SAME");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertEquals(200, res.getStatus());
        }

        MockHttpServletRequest req = new MockHttpServletRequest("GET", USER_HASH_PATH);
        req.addHeader("X-HW-AK", "AK-SAME");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, new MockFilterChain());

        assertEquals(429, res.getStatus());
    }

    @Test
    void telemetryEndpointStillLimitsByIp() throws Exception {
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", TELEMETRY_PATH);
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertEquals(200, res.getStatus());
        }

        MockHttpServletRequest req = new MockHttpServletRequest("POST", TELEMETRY_PATH);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());

        assertEquals(429, res.getStatus());
    }
}