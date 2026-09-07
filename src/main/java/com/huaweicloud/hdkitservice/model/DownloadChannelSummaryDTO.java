package com.huaweicloud.hdkitservice.model;

public record DownloadChannelSummaryDTO(
        long totalDownloads,
        long githubDownloads,
        long npmDownloads,
        Double githubTrend,
        Double npmTrend
) {}
