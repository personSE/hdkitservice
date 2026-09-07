package com.huaweicloud.hdkitservice.model;

public record VoucherSummaryDTO(
        long totalCount,
        long totalAmount,
        long todayCount,
        long todayAmount,
        double todayCountChainRatio,
        double todayAmountChainRatio,
        long monthCount,
        long monthAmount,
        double monthCountChainRatio,
        double monthAmountChainRatio
) {}
