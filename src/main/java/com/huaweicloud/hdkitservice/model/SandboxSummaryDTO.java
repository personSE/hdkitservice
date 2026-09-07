package com.huaweicloud.hdkitservice.model;

public record SandboxSummaryDTO(
    long totalUsers,
    long dailyUsers,
    double dailyUsersChainRatio,
    double avgDurationSec,
    double avgDurationDeltaSec,
    double p95DurationSec,
    String slaTarget
) {}
