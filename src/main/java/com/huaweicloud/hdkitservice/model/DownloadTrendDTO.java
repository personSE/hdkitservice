package com.huaweicloud.hdkitservice.model;

import java.util.List;

public record DownloadTrendDTO(
        List<TrendPoint> npmDaily,
        long totalNpmDownloads,
        List<TrendPoint> githubDaily
) {
    public record TrendPoint(String date, long downloads) {}
}
