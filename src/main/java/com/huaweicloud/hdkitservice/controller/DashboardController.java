package com.huaweicloud.hdkitservice.controller;

import com.huaweicloud.hdkitservice.model.ActivityConversionDTO;
import com.huaweicloud.hdkitservice.model.ActivitySummaryDTO;
import com.huaweicloud.hdkitservice.model.ActivityTrendDTO;
import com.huaweicloud.hdkitservice.model.AgentDistributionDTO;
import com.huaweicloud.hdkitservice.model.CapabilityDistributionDTO;
import com.huaweicloud.hdkitservice.model.CapabilitySummaryDTO;
import com.huaweicloud.hdkitservice.model.CapabilityTrendDTO;
import com.huaweicloud.hdkitservice.model.DeveloperSummaryDTO;
import com.huaweicloud.hdkitservice.model.DeveloperTrendDTO;
import com.huaweicloud.hdkitservice.model.DownloadChannelDistributionDTO;
import com.huaweicloud.hdkitservice.model.DownloadChannelSummaryDTO;
import com.huaweicloud.hdkitservice.model.DownloadSummaryDTO;
import com.huaweicloud.hdkitservice.model.DownloadTrendDTO;
import com.huaweicloud.hdkitservice.model.NewUserTrendDTO;
import com.huaweicloud.hdkitservice.model.SandboxDurationDTO;
import com.huaweicloud.hdkitservice.model.SandboxHourlyDTO;
import com.huaweicloud.hdkitservice.model.SandboxSummaryDTO;
import com.huaweicloud.hdkitservice.model.SandboxTrendDTO;
import com.huaweicloud.hdkitservice.model.SkillRankingDTO;
import com.huaweicloud.hdkitservice.model.VoucherDistributionDTO;
import com.huaweicloud.hdkitservice.model.VoucherSummaryDTO;
import com.huaweicloud.hdkitservice.model.VoucherTrendDTO;
import com.huaweicloud.hdkitservice.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/developer/server/hdkitservice/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/developer/summary")
    public DeveloperSummaryDTO developerSummary() {
        return dashboardService.getDeveloperSummary();
    }

    @GetMapping("/developer/trend")
    public DeveloperTrendDTO developerTrend() {
        return dashboardService.getDeveloperTrend();
    }

    @GetMapping("/developer/new-user-trend")
    public NewUserTrendDTO newUserTrend() {
        return dashboardService.getNewUserTrend();
    }

    @GetMapping("/agent/distribution")
    public AgentDistributionDTO agentDistribution() {
        return dashboardService.getAgentDistribution();
    }

    @GetMapping("/download/trend")
    public DownloadTrendDTO downloadTrend() {
        return dashboardService.getDownloadTrend();
    }

    @GetMapping("/download/channel-summary")
    public DownloadChannelSummaryDTO downloadChannelSummary() {
        return dashboardService.getDownloadChannelSummary();
    }

    @GetMapping("/download/channel-distribution")
    public DownloadChannelDistributionDTO downloadChannelDistribution() {
        return dashboardService.getDownloadChannelDistribution();
    }

    @GetMapping("/download/summary")
    public DownloadSummaryDTO downloadSummary() {
        return dashboardService.getDownloadSummary();
    }

    @GetMapping("/aggregate")
    public String triggerAggregation() {
        dashboardService.aggregateMetrics(java.time.LocalDate.now());
        return "{\"status\":\"ok\"}";
    }

    // ==================== Open Capabilities ====================

    @GetMapping("/capability/summary")
    public CapabilitySummaryDTO capabilitySummary() {
        return dashboardService.getCapabilitySummary();
    }

    @GetMapping("/capability/trend")
    public CapabilityTrendDTO capabilityTrend() {
        return dashboardService.getCapabilityTrend();
    }

    @GetMapping("/capability/distribution")
    public CapabilityDistributionDTO capabilityDistribution() {
        return dashboardService.getCapabilityDistribution();
    }

    @GetMapping("/capability/skill/ranking")
    public SkillRankingDTO skillRanking() {
        return dashboardService.getSkillRanking();
    }

    // ==================== Activity Statistics ====================

    @GetMapping("/activity/summary")
    public ActivitySummaryDTO activitySummary() {
        return dashboardService.getActivitySummary();
    }

    @GetMapping("/activity/trend")
    public ActivityTrendDTO activityTrend() {
        return dashboardService.getActivityTrend();
    }

    @GetMapping("/activity/conversion")
    public ActivityConversionDTO activityConversion() {
        return dashboardService.getActivityConversion();
    }

    // ==================== Voucher Resources ====================

    @GetMapping("/voucher/summary")
    public VoucherSummaryDTO voucherSummary() {
        return dashboardService.getVoucherSummary();
    }

    @GetMapping("/voucher/trend")
    public VoucherTrendDTO voucherTrend() {
        return dashboardService.getVoucherTrend();
    }

    @GetMapping("/voucher/distribution")
    public VoucherDistributionDTO voucherDistribution() {
        return dashboardService.getVoucherDistribution();
    }

    // ==================== Sandbox Resources ====================

    @GetMapping("/sandbox/summary")
    public SandboxSummaryDTO sandboxSummary() {
        return dashboardService.getSandboxSummary();
    }

    @GetMapping("/sandbox/trend")
    public SandboxTrendDTO sandboxTrend() {
        return dashboardService.getSandboxTrend();
    }

    @GetMapping("/sandbox/duration")
    public SandboxDurationDTO sandboxDuration() {
        return dashboardService.getSandboxDurationDistribution();
    }

    @GetMapping("/sandbox/hourly")
    public SandboxHourlyDTO sandboxHourly() {
        return dashboardService.getSandboxHourly();
    }
}
