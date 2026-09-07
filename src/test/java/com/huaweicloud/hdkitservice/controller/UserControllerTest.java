package com.huaweicloud.hdkitservice.controller;

import com.huaweicloud.hdkitservice.filter.TelemetryRateLimitFilter;
import com.huaweicloud.hdkitservice.service.IncentiveClient;
import com.huaweicloud.hdkitservice.service.JwtService;
import com.huaweicloud.hdkitservice.service.TelemetryHashService;
import com.huaweicloud.hdkitservice.service.UserHashService;
import com.huaweicloud.hdkitservice.util.Masker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = TelemetryRateLimitFilter.class))
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private IncentiveClient incentiveClient;

    @MockBean
    private TelemetryHashService hashService;

    @MockBean
    private UserHashService userHashService;

    @MockBean
    private Masker masker;

    @MockBean
    private JwtService jwtService;

    @Test
    void generatorUserIDHashWithDomainId() throws Exception {
        when(incentiveClient.resolveDomainIdFromIam("AK", "SK", null)).thenReturn("domain123");
        when(hashService.generateUserHash("domain123")).thenReturn("hash123abc");

        mvc.perform(get("/rest/developer/server/hdkitservice/user/generatorUserIDHash")
                        .header("X-HW-AK", "AK").header("X-HW-SK", "SK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userHash").value("hash123abc"));

        verify(userHashService).record(eq("hash123abc"), eq("domain123"), eq(false));
    }

    @Test
    void generatorUserIDHashIamFailedFallback() throws Exception {
        when(incentiveClient.resolveDomainIdFromIam("AK", "SK", null))
                .thenThrow(new IncentiveClient.IncentiveException("HDKIT_IAM_ERROR", "IAM failed", null));
        when(hashService.generateFallbackUserHash("AK")).thenReturn("fallback123");

        mvc.perform(get("/rest/developer/server/hdkitservice/user/generatorUserIDHash")
                        .header("X-HW-AK", "AK").header("X-HW-SK", "SK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userHash").value("fallback123"));

        verify(userHashService).record(eq("fallback123"), org.mockito.ArgumentMatchers.isNull(), eq(true));
    }

    @Test
    void generatorUserIDHashDomainIdEmptyFallback() throws Exception {
        when(incentiveClient.resolveDomainIdFromIam("AK", "SK", null)).thenReturn("");
        when(hashService.generateFallbackUserHash("AK")).thenReturn("fallback456");

        mvc.perform(get("/rest/developer/server/hdkitservice/user/generatorUserIDHash")
                        .header("X-HW-AK", "AK").header("X-HW-SK", "SK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userHash").value("fallback456"));

        verify(userHashService).record(eq("fallback456"), eq(""), eq(true));
    }

    @Test
    void generatorUserIDHashMissingAkHeaderReturns400() throws Exception {
        mvc.perform(get("/rest/developer/server/hdkitservice/user/generatorUserIDHash")
                        .header("X-HW-SK", "SK"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("HDKIT_INVALID_REQUEST"));
    }
}