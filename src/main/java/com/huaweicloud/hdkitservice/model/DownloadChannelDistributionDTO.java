package com.huaweicloud.hdkitservice.model;

import java.util.List;

public record DownloadChannelDistributionDTO(
        List<ChannelItem> channels
) {
    public record ChannelItem(
            String channel,
            long count,
            double percentage
    ) {}
}
