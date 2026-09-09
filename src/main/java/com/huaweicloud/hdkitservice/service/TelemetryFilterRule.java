package com.huaweicloud.hdkitservice.service;

import com.huaweicloud.hdkitservice.model.TelemetryEventDto;

public interface TelemetryFilterRule {

    String name();

    boolean shouldDrop(TelemetryEventDto dto);
}