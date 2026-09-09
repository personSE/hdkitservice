package com.huaweicloud.hdkitservice.service;

import com.huaweicloud.hdkitservice.config.HdkitTelemetryConfig;
import com.huaweicloud.hdkitservice.model.TelemetryEventDto;
import org.springframework.stereotype.Component;

@Component
public class TestHarnessFilterRule implements TelemetryFilterRule {

    private final HdkitTelemetryConfig config;

    public TestHarnessFilterRule(HdkitTelemetryConfig config) {
        this.config = config;
    }

    @Override
    public String name() {
        return "testHarness";
    }

    @Override
    public boolean shouldDrop(TelemetryEventDto dto) {
        if (!config.isFilterTestHarnessEnabled()) {
            return false;
        }
        String harness = dto.harness();
        if (harness == null || harness.isBlank()) {
            return false;
        }
        return config.getTestHarnessValues().contains(harness.trim().toLowerCase());
    }
}