package com.huaweicloud.hdkitservice.model;

import java.util.List;

public record NewUserTrendDTO(
        List<MonthPoint> months
) {
    public record MonthPoint(
            String month,
            long newUserCount,
            Double momRate,
            Double yoyRate
    ) {}
}
