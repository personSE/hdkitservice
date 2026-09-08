package com.huaweicloud.hdkitservice.service;

import com.huaweicloud.hdkitservice.model.ActivityConversionDTO;
import com.huaweicloud.hdkitservice.model.ActivityStatsSnapshot;
import com.huaweicloud.hdkitservice.model.ActivitySummaryDTO;
import com.huaweicloud.hdkitservice.model.ActivityTrendDTO;
import com.huaweicloud.hdkitservice.model.AgentDistributionDaily;
import com.huaweicloud.hdkitservice.model.AgentDistributionDTO;
import com.huaweicloud.hdkitservice.model.CapabilityDailyStats;
import com.huaweicloud.hdkitservice.model.CapabilityDistributionDTO;
import com.huaweicloud.hdkitservice.model.CapabilitySummaryDTO;
import com.huaweicloud.hdkitservice.model.CapabilityTrendDTO;
import com.huaweicloud.hdkitservice.model.DeveloperSummaryDTO;
import com.huaweicloud.hdkitservice.model.DeveloperTrendDTO;
import com.huaweicloud.hdkitservice.model.DownloadChannelDistributionDTO;
import com.huaweicloud.hdkitservice.model.DownloadChannelSummaryDTO;
import com.huaweicloud.hdkitservice.model.DownloadSummaryDTO;
import com.huaweicloud.hdkitservice.model.DownloadTrendDTO;
import com.huaweicloud.hdkitservice.model.MetricDaily;
import com.huaweicloud.hdkitservice.model.NewUserTrendDTO;
import com.huaweicloud.hdkitservice.model.NpmDownloadStats;
import com.huaweicloud.hdkitservice.model.SandboxDurationBucketDaily;
import com.huaweicloud.hdkitservice.model.SandboxDurationDTO;
import com.huaweicloud.hdkitservice.model.SandboxHourlyDTO;
import com.huaweicloud.hdkitservice.model.SandboxHourlyStats;
import com.huaweicloud.hdkitservice.model.SandboxSummaryDTO;
import com.huaweicloud.hdkitservice.model.SandboxTrendDTO;
import com.huaweicloud.hdkitservice.model.SkillDailyStats;
import com.huaweicloud.hdkitservice.model.SkillRankingDTO;
import com.huaweicloud.hdkitservice.model.VoucherClaimLog;
import com.huaweicloud.hdkitservice.model.VoucherDistributionDTO;
import com.huaweicloud.hdkitservice.model.VoucherFaceValueDaily;
import com.huaweicloud.hdkitservice.model.VoucherSummaryDTO;
import com.huaweicloud.hdkitservice.model.VoucherTrendDTO;
import com.huaweicloud.hdkitservice.repository.ActivityStatsSnapshotRepository;
import com.huaweicloud.hdkitservice.repository.AgentDistributionDailyRepository;
import com.huaweicloud.hdkitservice.repository.CapabilityDailyStatsRepository;
import com.huaweicloud.hdkitservice.repository.MetricDailyRepository;
import com.huaweicloud.hdkitservice.repository.NpmDownloadStatsRepository;
import com.huaweicloud.hdkitservice.repository.SandboxDurationBucketDailyRepository;
import com.huaweicloud.hdkitservice.repository.SandboxHourlyStatsRepository;
import com.huaweicloud.hdkitservice.repository.SandboxSessionRepository;
import com.huaweicloud.hdkitservice.repository.SkillDailyStatsRepository;
import com.huaweicloud.hdkitservice.repository.TelemetryEventRepository;
import com.huaweicloud.hdkitservice.repository.VoucherClaimLogRepository;
import com.huaweicloud.hdkitservice.repository.VoucherFaceValueDailyRepository;
import com.huaweicloud.hdkitservice.repository.VoucherRecordRepository;
import com.huaweicloud.hdkitservice.repository.GitHubStatsDailyRepository;
import com.huaweicloud.hdkitservice.repository.UserIdHashRepository;
import com.huaweicloud.hdkitservice.model.GitHubStatsDaily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private static final String KEY_TOTAL_DEVELOERS = "total_developers";
    private static final String KEY_DAU = "dau";
    private static final String KEY_MAU = "mau";
    private static final String KEY_NEW_USERS = "new_users";
    private static final String KEY_AGENT_TOTAL = "agent_total";
    private static final int TREND_DAYS = 30;

    private final MetricDailyRepository metricRepo;
    private final AgentDistributionDailyRepository agentRepo;
    private final NpmDownloadStatsRepository npmRepo;
    private final TelemetryEventRepository telemetryRepo;
    private final CapabilityDailyStatsRepository capabilityDailyRepo;
    private final SkillDailyStatsRepository skillDailyRepo;
    private final ActivityStatsSnapshotRepository activitySnapshotRepo;
    private final VoucherClaimLogRepository voucherClaimLogRepo;
    private final VoucherFaceValueDailyRepository voucherFaceValueRepo;
    private final VoucherRecordRepository voucherRecordRepo;
    private final SandboxDurationBucketDailyRepository sandboxBucketRepo;
    private final SandboxHourlyStatsRepository sandboxHourlyRepo;
    private final SandboxSessionRepository sandboxSessionRepo;
    private final GitHubStatsDailyRepository githubRepo;
    private final UserIdHashRepository userIdHashRepo;

    public DashboardService(MetricDailyRepository metricRepo,
                            AgentDistributionDailyRepository agentRepo,
                            NpmDownloadStatsRepository npmRepo,
                            TelemetryEventRepository telemetryRepo,
                            CapabilityDailyStatsRepository capabilityDailyRepo,
                            SkillDailyStatsRepository skillDailyRepo,
                            ActivityStatsSnapshotRepository activitySnapshotRepo,
                            VoucherClaimLogRepository voucherClaimLogRepo,
                            VoucherFaceValueDailyRepository voucherFaceValueRepo,
                            VoucherRecordRepository voucherRecordRepo,
                            SandboxDurationBucketDailyRepository sandboxBucketRepo,
                            SandboxHourlyStatsRepository sandboxHourlyRepo,
                            SandboxSessionRepository sandboxSessionRepo,
                            GitHubStatsDailyRepository githubRepo,
                            UserIdHashRepository userIdHashRepo) {
        this.metricRepo = metricRepo;
        this.agentRepo = agentRepo;
        this.npmRepo = npmRepo;
        this.telemetryRepo = telemetryRepo;
        this.capabilityDailyRepo = capabilityDailyRepo;
        this.skillDailyRepo = skillDailyRepo;
        this.activitySnapshotRepo = activitySnapshotRepo;
        this.voucherClaimLogRepo = voucherClaimLogRepo;
        this.voucherFaceValueRepo = voucherFaceValueRepo;
        this.voucherRecordRepo = voucherRecordRepo;
        this.sandboxBucketRepo = sandboxBucketRepo;
        this.sandboxHourlyRepo = sandboxHourlyRepo;
        this.sandboxSessionRepo = sandboxSessionRepo;
        this.githubRepo = githubRepo;
        this.userIdHashRepo = userIdHashRepo;
    }

    public DeveloperSummaryDTO getDeveloperSummary() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate prevDay = today.minusDays(2);
        LocalDate monthAgo = today.minusDays(30);

        long totalDevs = getMetricValue(KEY_TOTAL_DEVELOERS, today, userIdHashRepo::countAll);
        long dau = getMetricValue(KEY_DAU, today, () -> telemetryRepo.countDistinctUserHashByDate(today));
        long mau = getMetricValue(KEY_MAU, today, () -> telemetryRepo.countDistinctUserHashSince(monthAgo));
        long agentTotal = getMetricValue(KEY_AGENT_TOTAL, today, telemetryRepo::countDistinctAgentHarness);

        long newUsersToday = getMetricValue(KEY_NEW_USERS, today, () -> userIdHashRepo.countNewByDate(today));
        long newUsersYesterday = getMetricValue(KEY_NEW_USERS, yesterday, () -> userIdHashRepo.countNewByDate(yesterday));
        double chainRatio = 0;
        if (newUsersYesterday > 0) {
            chainRatio = (double) (newUsersToday - newUsersYesterday) / newUsersYesterday * 100;
        } else if (newUsersToday > 0) {
            chainRatio = 100;
        }

        List<DeveloperSummaryDTO.DailyTrendPoint> dauTrend = buildDauTrend(today);

        return new DeveloperSummaryDTO(totalDevs, newUsersToday, chainRatio, dau, mau, agentTotal, dauTrend);
    }

    public AgentDistributionDTO getAgentDistribution() {
        List<AgentDistributionDaily> latest = agentRepo.findLatestDistribution();

        if (latest == null || latest.isEmpty()) {
            return buildAgentDistributionFromTelemetry();
        }

        long total = latest.stream().mapToLong(AgentDistributionDaily::getInstallCount).sum();
        List<AgentDistributionDTO.AgentItem> items = new ArrayList<>();
        for (AgentDistributionDaily d : latest) {
            String name = normalizeAgentName(d.getAgentName());
            double pct = total > 0 ? (double) d.getInstallCount() / total * 100 : 0;
            items.add(new AgentDistributionDTO.AgentItem(name, d.getInstallCount(), Math.round(pct * 10) / 10.0));
        }

        items = mergeAgentItems(items);
        total = items.stream().mapToLong(AgentDistributionDTO.AgentItem::count).sum();

        String dateStr = latest.isEmpty() ? LocalDate.now().toString() : latest.get(0).getMetricDate().toString();
        return new AgentDistributionDTO(dateStr, total, items);
    }

    public DownloadTrendDTO getDownloadTrend() {
        LocalDate startDate = LocalDate.now().minusDays(TREND_DAYS);
        List<NpmDownloadStats> stats = npmRepo.findSince(startDate);

        List<DownloadTrendDTO.TrendPoint> npmDaily = new ArrayList<>();
        long total = 0;
        for (NpmDownloadStats s : stats) {
            npmDaily.add(new DownloadTrendDTO.TrendPoint(s.getStatDate().toString(), s.getDailyDownloads()));
            total += s.getDailyDownloads();
        }

        return new DownloadTrendDTO(npmDaily, total, List.of());
    }

    public NewUserTrendDTO getNewUserTrend() {
        LocalDate today = LocalDate.now();
        List<NewUserTrendDTO.MonthPoint> months = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i);
            int year = monthDate.getYear();
            int month = monthDate.getMonthValue();
            String monthStr = year + "-" + String.format("%02d", month);

            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.plusMonths(1);

            List<MetricDaily> metrics = metricRepo.findByKeyBetween(KEY_NEW_USERS, monthStart, monthEnd.minusDays(1));
            long newUserCount = metrics.stream()
                    .mapToLong(m -> m.getMetricValue() != null ? m.getMetricValue() : 0)
                    .sum();

            Double momRate = null;
            if (i < 5) {
                LocalDate prevMonthStart = monthStart.minusMonths(1);
                List<MetricDaily> prevMetrics = metricRepo.findByKeyBetween(KEY_NEW_USERS, prevMonthStart, monthStart.minusDays(1));
                long prevCount = prevMetrics.stream()
                        .mapToLong(m -> m.getMetricValue() != null ? m.getMetricValue() : 0)
                        .sum();
                if (prevCount > 0) {
                    momRate = round1((double) (newUserCount - prevCount) / prevCount * 100);
                } else if (newUserCount > 0) {
                    momRate = 100.0;
                }
            }

            Double yoyRate = null;
            LocalDate lastYearStart = monthStart.minusYears(1);
            List<MetricDaily> lastYearMetrics = metricRepo.findByKeyBetween(KEY_NEW_USERS, lastYearStart, lastYearStart.plusMonths(1).minusDays(1));
            long lastYearCount = lastYearMetrics.stream()
                    .mapToLong(m -> m.getMetricValue() != null ? m.getMetricValue() : 0)
                    .sum();
            if (lastYearCount > 0) {
                yoyRate = round1((double) (newUserCount - lastYearCount) / lastYearCount * 100);
            }

            months.add(new NewUserTrendDTO.MonthPoint(monthStr, newUserCount, momRate, yoyRate));
        }

        return new NewUserTrendDTO(months);
    }

    public DownloadChannelSummaryDTO getDownloadChannelSummary() {
        Optional<NpmDownloadStats> latestOpt = npmRepo.findLatest();
        long npmCumulative = latestOpt
                .map(NpmDownloadStats::getCumulativeDownloads)
                .orElse(0L);

        Optional<GitHubStatsDaily> githubOpt = githubRepo.findLatest();
        long githubDownloads = githubOpt.map(g -> g.getStars() + g.getForks()).orElse(0L);
        long total = npmCumulative + githubDownloads;

        return new DownloadChannelSummaryDTO(total, githubDownloads, npmCumulative, null, null);
    }

    public DownloadChannelDistributionDTO getDownloadChannelDistribution() {
        Optional<NpmDownloadStats> latestOpt = npmRepo.findLatest();
        long npmDownloads = latestOpt
                .map(NpmDownloadStats::getCumulativeDownloads)
                .orElse(0L);

        Optional<GitHubStatsDaily> githubOpt = githubRepo.findLatest();
        long githubDownloads = githubOpt.map(g -> g.getStars() + g.getForks()).orElse(0L);
        long total = npmDownloads + githubDownloads;

        List<DownloadChannelDistributionDTO.ChannelItem> channels = new ArrayList<>();
        if (total > 0) {
            channels.add(new DownloadChannelDistributionDTO.ChannelItem(
                    "GitHub", githubDownloads, round1((double) githubDownloads / total * 100)));
            channels.add(new DownloadChannelDistributionDTO.ChannelItem(
                    "npm", npmDownloads, round1((double) npmDownloads / total * 100)));
        }

        return new DownloadChannelDistributionDTO(channels);
    }

    public DownloadSummaryDTO getDownloadSummary() {
        Optional<NpmDownloadStats> latestOpt = npmRepo.findLatest();
        if (latestOpt.isEmpty()) {
            return new DownloadSummaryDTO(0, 0, 0, "", "");
        }
        NpmDownloadStats latest = latestOpt.get();
        return new DownloadSummaryDTO(
                latest.getDailyDownloads() != null ? latest.getDailyDownloads() : 0,
                latest.getWeekDownloads() != null ? latest.getWeekDownloads() : 0,
                latest.getCumulativeDownloads() != null ? latest.getCumulativeDownloads() : 0,
                latest.getPackageName(),
                latest.getStatDate().toString()
        );
    }

    public DeveloperTrendDTO getDeveloperTrend() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(TREND_DAYS - 1);

        List<MetricDaily> dauMetrics = metricRepo.findByKeySince(KEY_DAU, startDate);
        List<MetricDaily> mauMetrics = metricRepo.findByKeySince(KEY_MAU, startDate);

        Map<LocalDate, Long> dauMap = new TreeMap<>();
        for (MetricDaily m : dauMetrics) {
            dauMap.put(m.getMetricDate(), m.getMetricValue() != null ? m.getMetricValue() : 0);
        }
        Map<LocalDate, Long> mauMap = new TreeMap<>();
        for (MetricDaily m : mauMetrics) {
            mauMap.put(m.getMetricDate(), m.getMetricValue() != null ? m.getMetricValue() : 0);
        }

        List<DeveloperTrendDTO.TrendPoint> points = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            long dau = dauMap.getOrDefault(d, 0L);
            long mau = mauMap.getOrDefault(d, 0L);
            points.add(new DeveloperTrendDTO.TrendPoint(d.toString(), dau, mau));
        }

        return new DeveloperTrendDTO(points);
    }

    public void aggregateMetrics(LocalDate date) {
        log.info("[dashboard] aggregating metrics for {}", date);

        saveMetric(date, KEY_TOTAL_DEVELOERS, userIdHashRepo.countAll());
        saveMetric(date, KEY_DAU, telemetryRepo.countDistinctUserHashByDate(date));
        saveMetric(date, KEY_MAU, telemetryRepo.countDistinctUserHashSince(date.minusDays(30)));
        saveMetric(date, KEY_AGENT_TOTAL, telemetryRepo.countDistinctAgentHarness());
        saveMetric(date, KEY_NEW_USERS, userIdHashRepo.countNewByDate(date));

        aggregateAgentDistribution(date);
    }

    private void aggregateAgentDistribution(LocalDate date) {
        List<Object[]> rows = telemetryRepo.agentDistribution();
        for (Object[] row : rows) {
            String rawName = (String) row[0];
            Number count = (Number) row[1];
            String normalName = normalizeAgentName(rawName);

            AgentDistributionDaily existing = agentRepo
                    .findByMetricDateOrderByInstallCountDesc(date)
                    .stream()
                    .filter(a -> a.getAgentName().equals(normalName))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setInstallCount(count.intValue());
                existing.setUpdatedAt(java.time.LocalDateTime.now());
                agentRepo.save(existing);
            } else {
                agentRepo.save(new AgentDistributionDaily(date, normalName, count.intValue()));
            }
        }
        log.info("[dashboard] agent distribution aggregated for {}, {} agents", date, rows.size());
    }

    private void saveMetric(LocalDate date, String key, long value) {
        Optional<MetricDaily> existing = metricRepo.findByMetricDateAndMetricKey(date, key);
        if (existing.isPresent()) {
            MetricDaily m = existing.get();
            m.setMetricValue(value);
            m.setUpdatedAt(java.time.LocalDateTime.now());
            metricRepo.save(m);
        } else {
            metricRepo.save(new MetricDaily(date, key, value, null));
        }
    }

    private long getMetricValue(String key, LocalDate date, java.util.function.LongSupplier fallback) {
        Optional<MetricDaily> metric = metricRepo.findByMetricDateAndMetricKey(date, key);
        if (metric.isPresent() && metric.get().getMetricValue() != null) {
            return metric.get().getMetricValue();
        }
        try {
            return fallback.getAsLong();
        } catch (Exception e) {
            log.debug("[dashboard] fallback query failed for {}: {}", key, e.getMessage());
            return 0;
        }
    }

    private List<DeveloperSummaryDTO.DailyTrendPoint> buildDauTrend(LocalDate today) {
        LocalDate startDate = today.minusDays(TREND_DAYS - 1);
        List<MetricDaily> metrics = metricRepo.findByKeySince(KEY_DAU, startDate);

        if (!metrics.isEmpty()) {
            return metrics.stream()
                    .map(m -> new DeveloperSummaryDTO.DailyTrendPoint(m.getMetricDate().toString(), m.getMetricValue()))
                    .toList();
        }

        List<Object[]> rows = telemetryRepo.dailyActiveUsersSince(startDate);
        List<DeveloperSummaryDTO.DailyTrendPoint> points = new ArrayList<>();
        for (Object[] row : rows) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            long cnt = ((Number) row[1]).longValue();
            points.add(new DeveloperSummaryDTO.DailyTrendPoint(sqlDate.toLocalDate().toString(), cnt));
        }
        return points;
    }

    private AgentDistributionDTO buildAgentDistributionFromTelemetry() {
        List<Object[]> rows = telemetryRepo.agentDistribution();
        long total = 0;
        List<AgentDistributionDTO.AgentItem> items = new ArrayList<>();
        for (Object[] row : rows) {
            String name = normalizeAgentName((String) row[0]);
            int count = ((Number) row[1]).intValue();
            total += count;
            items.add(new AgentDistributionDTO.AgentItem(name, count, 0));
        }
        items = mergeAgentItems(items);
        total = items.stream().mapToLong(AgentDistributionDTO.AgentItem::count).sum();
        for (int i = 0; i < items.size(); i++) {
            AgentDistributionDTO.AgentItem item = items.get(i);
            double pct = total > 0 ? (double) item.count() / total * 100 : 0;
            items.set(i, new AgentDistributionDTO.AgentItem(item.name(), item.count(), Math.round(pct * 10) / 10.0));
        }
        return new AgentDistributionDTO(LocalDate.now().toString(), total, items);
    }

    private List<AgentDistributionDTO.AgentItem> mergeAgentItems(List<AgentDistributionDTO.AgentItem> items) {
        Map<String, Integer> merged = new HashMap<>();
        for (AgentDistributionDTO.AgentItem item : items) {
            merged.merge(item.name(), item.count(), Integer::sum);
        }
        List<AgentDistributionDTO.AgentItem> result = new ArrayList<>();
        merged.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .forEach(e -> result.add(new AgentDistributionDTO.AgentItem(e.getKey(), e.getValue(), 0)));
        return result;
    }

    static String normalizeAgentName(String raw) {
        if (raw == null || raw.isBlank()) {
            return "unknown";
        }
        String lower = raw.toLowerCase().trim();
        if (lower.startsWith("opencode")) return "opencode";
        if (lower.startsWith("codex")) return "codex";
        if (lower.startsWith("workbuddy")) return "workbuddy";
        if (lower.startsWith("码道")) return "码道";
        if (lower.startsWith("officeace")) return "officeace";
        if (lower.startsWith("codearts")) return "codearts";
        if (lower.startsWith("hermes")) return "hermes";
        if (lower.startsWith("dsh")) return "dsh";
        if (lower.startsWith("mcp")) return "mcp";
        if (lower.startsWith("vscode")) return "vscode";
        return raw.trim();
    }

    // ==================== Open Capabilities ====================

    public CapabilitySummaryDTO getCapabilitySummary() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        long totalCalls = sumCapabilityCallsFromLatest(today);
        long uniqueUsers = telemetryRepo.countDistinctUsersWithCapability();
        long dailyAvg = calcCapabilityDailyAvg(weekAgo, today);
        long todayCalls = sumCapabilityCallsByDate(today);

        return new CapabilitySummaryDTO(totalCalls, uniqueUsers, dailyAvg, todayCalls);
    }

    public CapabilityTrendDTO getCapabilityTrend() {
        LocalDate startDate = LocalDate.now().minusDays(13);

        List<CapabilityDailyStats> stats =
                capabilityDailyRepo.findByStatDateGreaterThanEqualOrderByStatDateAscCapability(startDate);

        if (stats != null && !stats.isEmpty()) {
            return buildTrendFromPreAggregated(stats, startDate);
        }

        List<Object[]> rows = telemetryRepo.capabilityCallsByDate(startDate);
        return buildTrendFromTelemetry(rows, startDate);
    }

    public CapabilityDistributionDTO getCapabilityDistribution() {
        LocalDate today = LocalDate.now();

        List<CapabilityDailyStats> latest =
                capabilityDailyRepo.findByStatDateOrderByCallCountDesc(today);

        if (latest != null && !latest.isEmpty()) {
            long total = latest.stream().mapToLong(CapabilityDailyStats::getCallCount).sum();
            List<CapabilityDistributionDTO.CapabilityItem> items = new ArrayList<>();
            for (CapabilityDailyStats s : latest) {
                double pct = total > 0 ? (double) s.getCallCount() / total * 100 : 0;
                items.add(new CapabilityDistributionDTO.CapabilityItem(
                        s.getCapability(), s.getCallCount(), Math.round(pct * 10) / 10.0));
            }
            return new CapabilityDistributionDTO(items);
        }

        return buildDistributionFromTelemetry();
    }

    public SkillRankingDTO getSkillRanking() {
        List<SkillDailyStats> allStats =
                skillDailyRepo.findTopSkillsSince(LocalDate.of(2000, 1, 1));

        if (allStats != null && !allStats.isEmpty()) {
            Map<String, Long> merged = new LinkedHashMap<>();
            for (SkillDailyStats s : allStats) {
                merged.merge(s.getSkillName(), s.getCallCount(), Long::sum);
            }
            return buildSkillRanking(merged);
        }

        List<Object[]> rows = telemetryRepo.skillRanking(10);
        Map<String, Long> merged = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String name = (String) row[0];
            long cnt = ((Number) row[1]).longValue();
            merged.put(name, cnt);
        }
        return buildSkillRanking(merged);
    }

    void aggregateCapabilityMetrics(LocalDate date) {
        log.info("[capability] aggregating for {}", date);

        List<Object[]> capRows = telemetryRepo.capabilityCallCountsBySpecificDate(date);
        for (Object[] row : capRows) {
            String cap = (String) row[0];
            long cnt = ((Number) row[1]).longValue();
            long users = telemetryRepo.countDistinctUsersByCapabilityAndDate(cap, date);

            CapabilityDailyStats.PK pk = new CapabilityDailyStats.PK(date, cap);
            CapabilityDailyStats existing = capabilityDailyRepo.findById(pk).orElse(null);
            if (existing != null) {
                existing.setCallCount(cnt);
                existing.setUserCount(users);
                existing.setUpdatedAt(java.time.LocalDateTime.now());
                capabilityDailyRepo.save(existing);
            } else {
                capabilityDailyRepo.save(new CapabilityDailyStats(date, cap, cnt, users));
            }
        }

        aggregateSkillDaily(date);

        log.info("[capability] done for {}", date);
    }

    private void aggregateSkillDaily(LocalDate date) {
        List<Object[]> rows = telemetryRepo.skillCallsByDate(date);
        for (Object[] row : rows) {
            LocalDate d = ((java.sql.Date) row[0]).toLocalDate();
            if (!d.equals(date)) continue;
            String skillName = (String) row[1];
            long cnt = ((Number) row[2]).longValue();

            SkillDailyStats.PK pk = new SkillDailyStats.PK(d, skillName);
            SkillDailyStats existing = skillDailyRepo.findById(pk).orElse(null);
            if (existing != null) {
                existing.setCallCount(cnt);
                existing.setUpdatedAt(java.time.LocalDateTime.now());
                skillDailyRepo.save(existing);
            } else {
                skillDailyRepo.save(new SkillDailyStats(d, skillName, cnt));
            }
        }
        log.info("[capability] skill daily aggregated for {}", date);
    }

    private long sumCapabilityCallsFromLatest(LocalDate today) {
        List<CapabilityDailyStats> latest = capabilityDailyRepo.findByStatDateOrderByCallCountDesc(today);
        if (latest != null && !latest.isEmpty()) {
            return latest.stream().mapToLong(CapabilityDailyStats::getCallCount).sum();
        }
        try {
            return telemetryRepo.capabilityCallCounts();
        } catch (Exception e) {
            log.debug("[capability] fallback totalCalls failed: {}", e.getMessage());
            return 0;
        }
    }

    private long sumCapabilityCallsByDate(LocalDate date) {
        List<CapabilityDailyStats> stats = capabilityDailyRepo.findByStatDateOrderByCallCountDesc(date);
        if (stats != null && !stats.isEmpty()) {
            return stats.stream().mapToLong(CapabilityDailyStats::getCallCount).sum();
        }
        try {
            return telemetryRepo.capabilityCallCountByDate(date);
        } catch (Exception e) {
            log.debug("[capability] fallback todayCalls failed: {}", e.getMessage());
            return 0;
        }
    }

    private long calcCapabilityDailyAvg(LocalDate weekAgo, LocalDate today) {
        List<CapabilityDailyStats> stats =
                capabilityDailyRepo.findByStatDateGreaterThanEqualOrderByStatDateAscCapability(weekAgo);
        if (stats != null && !stats.isEmpty()) {
            Map<LocalDate, Long> dailyTotals = new HashMap<>();
            for (CapabilityDailyStats s : stats) {
                dailyTotals.merge(s.getStatDate(), s.getCallCount(), Long::sum);
            }
            if (dailyTotals.isEmpty()) return 0;
            long total = dailyTotals.values().stream().mapToLong(Long::longValue).sum();
            return total / dailyTotals.size();
        }
        return 0;
    }

    private CapabilityTrendDTO buildTrendFromPreAggregated(List<CapabilityDailyStats> stats, LocalDate startDate) {
        Map<LocalDate, Map<String, Long>> dateMap = new TreeMap<>();
        for (CapabilityDailyStats s : stats) {
            dateMap.computeIfAbsent(s.getStatDate(), k -> new HashMap<>())
                    .put(s.getCapability(), s.getCallCount());
        }

        TreeSet<String> capabilities = new TreeSet<>();
        for (Map<String, Long> m : dateMap.values()) {
            capabilities.addAll(m.keySet());
        }

        List<String> dates = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(LocalDate.now()); d = d.plusDays(1)) {
            dates.add(d.toString());
        }

        List<CapabilityTrendDTO.CapabilityTrendLine> lines = new ArrayList<>();
        for (String cap : capabilities) {
            List<long[]> data = new ArrayList<>();
            int idx = 0;
            for (LocalDate d = startDate; !d.isAfter(LocalDate.now()); d = d.plusDays(1)) {
                Map<String, Long> dayData = dateMap.getOrDefault(d, Map.of());
                long count = dayData.getOrDefault(cap, 0L);
                data.add(new long[]{idx, count});
                idx++;
            }
            lines.add(new CapabilityTrendDTO.CapabilityTrendLine(cap, data));
        }

        return new CapabilityTrendDTO(dates, lines);
    }

    private CapabilityTrendDTO buildTrendFromTelemetry(List<Object[]> rows, LocalDate startDate) {
        Map<LocalDate, Map<String, Long>> dateMap = new TreeMap<>();
        for (Object[] row : rows) {
            LocalDate d = ((java.sql.Date) row[0]).toLocalDate();
            String cap = (String) row[1];
            long cnt = ((Number) row[2]).longValue();
            dateMap.computeIfAbsent(d, k -> new HashMap<>()).put(cap, cnt);
        }

        TreeSet<String> capabilities = new TreeSet<>();
        for (Map<String, Long> m : dateMap.values()) {
            capabilities.addAll(m.keySet());
        }

        List<String> dates = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(LocalDate.now()); d = d.plusDays(1)) {
            dates.add(d.toString());
        }

        List<CapabilityTrendDTO.CapabilityTrendLine> lines = new ArrayList<>();
        for (String cap : capabilities) {
            List<long[]> data = new ArrayList<>();
            int idx = 0;
            for (LocalDate d = startDate; !d.isAfter(LocalDate.now()); d = d.plusDays(1)) {
                Map<String, Long> dayData = dateMap.getOrDefault(d, Map.of());
                long count = dayData.getOrDefault(cap, 0L);
                data.add(new long[]{idx, count});
                idx++;
            }
            lines.add(new CapabilityTrendDTO.CapabilityTrendLine(cap, data));
        }

        return new CapabilityTrendDTO(dates, lines);
    }

    private CapabilityDistributionDTO buildDistributionFromTelemetry() {
        List<Object[]> rows = telemetryRepo.capabilityCallCountsByCap();
        long total = 0;
        List<CapabilityDistributionDTO.CapabilityItem> items = new ArrayList<>();
        for (Object[] row : rows) {
            String cap = (String) row[0];
            long cnt = ((Number) row[1]).longValue();
            total += cnt;
            items.add(new CapabilityDistributionDTO.CapabilityItem(cap, cnt, 0));
        }
        for (int i = 0; i < items.size(); i++) {
            CapabilityDistributionDTO.CapabilityItem item = items.get(i);
            double pct = total > 0 ? (double) item.callCount() / total * 100 : 0;
            items.set(i, new CapabilityDistributionDTO.CapabilityItem(
                    item.capability(), item.callCount(), Math.round(pct * 10) / 10.0));
        }
        return new CapabilityDistributionDTO(items);
    }

    private SkillRankingDTO buildSkillRanking(Map<String, Long> merged) {
        List<Map.Entry<String, Long>> sorted = new ArrayList<>(merged.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        int topN = Math.min(10, sorted.size());
        long topTotal = sorted.stream().limit(topN).mapToLong(Map.Entry::getValue).sum();

        List<SkillRankingDTO.SkillItem> skills = new ArrayList<>();
        for (int i = 0; i < topN; i++) {
            Map.Entry<String, Long> e = sorted.get(i);
            double pct = topTotal > 0 ? (double) e.getValue() / topTotal * 100 : 0;
            skills.add(new SkillRankingDTO.SkillItem(
                    i + 1, e.getKey(), e.getValue(), Math.round(pct * 10) / 10.0));
        }
        return new SkillRankingDTO(skills);
    }

    // ==================== Activity Statistics ====================

    public ActivitySummaryDTO getActivitySummary() {
        ActivityStatsSnapshot latest = activitySnapshotRepo.findFirstByOrderBySnapshotDateDesc().orElse(null);
        if (latest == null) {
            return new ActivitySummaryDTO(0, 0, 0, 0, 0, 0, 0, List.of());
        }

        long total = latest.getParticipantCount();
        long c1 = latest.getBeginnerCount();
        long c2 = latest.getIntermediateCount();
        long c3 = latest.getAdvancedCount();

        double c1Rate = total > 0 ? round1((double) c1 / total * 100) : 0;
        double c2Rate = total > 0 ? round1((double) c2 / total * 100) : 0;
        double c3Rate = total > 0 ? round1((double) c3 / total * 100) : 0;

        List<ActivitySummaryDTO.FunnelStage> funnel = List.of(
                new ActivitySummaryDTO.FunnelStage("参与活动", total, 100.0),
                new ActivitySummaryDTO.FunnelStage("初章完成", c1, c1Rate),
                new ActivitySummaryDTO.FunnelStage("第二章完成", c2, c2Rate),
                new ActivitySummaryDTO.FunnelStage("终章完成", c3, c3Rate)
        );

        return new ActivitySummaryDTO(total, c1, c2, c3, c1Rate, c2Rate, c3Rate, funnel);
    }

    public ActivityTrendDTO getActivityTrend() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(13);

        List<ActivityStatsSnapshot> snapshots =
                activitySnapshotRepo.findBySnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(startDate);

        List<ActivityTrendDTO.TrendPoint> chapter1 = new ArrayList<>();
        List<ActivityTrendDTO.TrendPoint> chapter2 = new ArrayList<>();
        List<ActivityTrendDTO.TrendPoint> chapter3 = new ArrayList<>();

        Map<LocalDate, ActivityStatsSnapshot> snapshotMap = new TreeMap<>();
        for (ActivityStatsSnapshot s : snapshots) {
            snapshotMap.put(s.getSnapshotDate(), s);
        }

        ActivityStatsSnapshot prev = null;
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            ActivityStatsSnapshot curr = snapshotMap.get(d);
            if (curr != null) {
                long c1New = curr.getBeginnerCount() - (prev != null ? prev.getBeginnerCount() : 0);
                long c2New = curr.getIntermediateCount() - (prev != null ? prev.getIntermediateCount() : 0);
                long c3New = curr.getAdvancedCount() - (prev != null ? prev.getAdvancedCount() : 0);
                chapter1.add(new ActivityTrendDTO.TrendPoint(d.toString(), Math.max(0, c1New)));
                chapter2.add(new ActivityTrendDTO.TrendPoint(d.toString(), Math.max(0, c2New)));
                chapter3.add(new ActivityTrendDTO.TrendPoint(d.toString(), Math.max(0, c3New)));
                prev = curr;
            } else {
                chapter1.add(new ActivityTrendDTO.TrendPoint(d.toString(), 0));
                chapter2.add(new ActivityTrendDTO.TrendPoint(d.toString(), 0));
                chapter3.add(new ActivityTrendDTO.TrendPoint(d.toString(), 0));
            }
        }

        return new ActivityTrendDTO(chapter1, chapter2, chapter3);
    }

    public ActivityConversionDTO getActivityConversion() {
        ActivityStatsSnapshot latest = activitySnapshotRepo.findFirstByOrderBySnapshotDateDesc().orElse(null);
        if (latest == null || latest.getParticipantCount() == 0) {
            return new ActivityConversionDTO(List.of());
        }

        long total = latest.getParticipantCount();
        long c1 = latest.getBeginnerCount();
        long c2 = latest.getIntermediateCount();
        long c3 = latest.getAdvancedCount();

        double r1 = total > 0 ? round1((double) c1 / total * 100) : 0;
        double r2 = c1 > 0 ? round1((double) c2 / c1 * 100) : 0;
        double r3 = c2 > 0 ? round1((double) c3 / c2 * 100) : 0;

        List<ActivityConversionDTO.ConvItem> stages = List.of(
                new ActivityConversionDTO.ConvItem("参与 → 初章", r1),
                new ActivityConversionDTO.ConvItem("初章 → 第二章", r2),
                new ActivityConversionDTO.ConvItem("第二章 → 终章", r3)
        );

        return new ActivityConversionDTO(stages);
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    // ==================== Voucher Resources ====================

    private static final String KEY_VOUCHER_DAILY_COUNT = "voucher_daily_count";
    private static final String KEY_VOUCHER_DAILY_AMOUNT = "voucher_daily_amount";
    private static final String KEY_VOUCHER_TOTAL_COUNT = "voucher_total_count";
    private static final String KEY_VOUCHER_TOTAL_AMOUNT = "voucher_total_amount";
    private static final String KEY_VOUCHER_MONTHLY_COUNT = "voucher_monthly_count";
    private static final String KEY_VOUCHER_MONTHLY_AMOUNT = "voucher_monthly_amount";

    public VoucherSummaryDTO getVoucherSummary() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDate lastMonthStart = today.minusMonths(1).withDayOfMonth(1);

        long totalCount = getMetricValue(KEY_VOUCHER_TOTAL_COUNT, today, voucherRecordRepo::countAllSuccessUsers);
        long totalAmount = getMetricValue(KEY_VOUCHER_TOTAL_AMOUNT, today, voucherRecordRepo::sumAllSuccessAmount);

        long todayCount = getMetricValue(KEY_VOUCHER_DAILY_COUNT, today, () -> voucherRecordRepo.countSuccessUsersByDate(today));
        long todayAmount = getMetricValue(KEY_VOUCHER_DAILY_AMOUNT, today, () -> voucherRecordRepo.sumSuccessAmountByDate(today));
        long yesterdayCount = getMetricValue(KEY_VOUCHER_DAILY_COUNT, yesterday, () -> voucherRecordRepo.countSuccessUsersByDate(yesterday));
        long yesterdayAmount = getMetricValue(KEY_VOUCHER_DAILY_AMOUNT, yesterday, () -> voucherRecordRepo.sumSuccessAmountByDate(yesterday));

        double todayCountChain = yesterdayCount > 0 ? round1((double) (todayCount - yesterdayCount) / yesterdayCount * 100) : (todayCount > 0 ? 100.0 : 0);
        double todayAmountChain = yesterdayAmount > 0 ? round1((double) (todayAmount - yesterdayAmount) / yesterdayAmount * 100) : (todayAmount > 0 ? 100.0 : 0);

        int year = today.getYear();
        int month = today.getMonthValue();
        int lastYear = lastMonthStart.getYear();
        int lastMonth = lastMonthStart.getMonthValue();

        long monthCount = getMetricValue(KEY_VOUCHER_MONTHLY_COUNT, today, () -> voucherRecordRepo.countSuccessUsersByMonth(year, month));
        long monthAmount = getMetricValue(KEY_VOUCHER_MONTHLY_AMOUNT, today, () -> voucherRecordRepo.sumSuccessAmountByMonth(year, month));
        long lastMonthCount = voucherRecordRepo.countSuccessUsersByMonth(lastYear, lastMonth);
        long lastMonthAmount = voucherRecordRepo.sumSuccessAmountByMonth(lastYear, lastMonth);

        double monthCountChain = lastMonthCount > 0 ? round1((double) (monthCount - lastMonthCount) / lastMonthCount * 100) : (monthCount > 0 ? 100.0 : 0);
        double monthAmountChain = lastMonthAmount > 0 ? round1((double) (monthAmount - lastMonthAmount) / lastMonthAmount * 100) : (monthAmount > 0 ? 100.0 : 0);

        List<Object[]> statusCounts = voucherRecordRepo.countByStatusAndDate(today);
        long successCount = 0, failCount = 0, alreadyClaimedCount = 0;
        for (Object[] row : statusCounts) {
            if ("success".equals(row[0])) successCount = ((Number) row[1]).longValue();
            else if ("fail".equals(row[0])) failCount = ((Number) row[1]).longValue();
            else if ("already_claimed".equals(row[0])) alreadyClaimedCount = ((Number) row[1]).longValue();
        }
        long totalAttempts = successCount + failCount + alreadyClaimedCount;
        double successRate = totalAttempts > 0 ? round1((double) successCount / totalAttempts * 100) : 0;

        return new VoucherSummaryDTO(
                totalCount, totalAmount,
                todayCount, todayAmount, todayCountChain, todayAmountChain,
                monthCount, monthAmount, monthCountChain, monthAmountChain
        );
    }

    public VoucherTrendDTO getVoucherTrend() {
        LocalDate startDate = LocalDate.now().minusDays(TREND_DAYS - 1);
        List<Object[]> rows = voucherRecordRepo.findDailySuccessStatsSince(startDate);

        Map<LocalDate, long[]> dailyMap = new TreeMap<>();
        for (Object[] row : rows) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            LocalDate d = sqlDate.toLocalDate();
            long cnt = ((Number) row[1]).longValue();
            long amt = ((Number) row[2]).longValue();
            dailyMap.put(d, new long[]{cnt, amt});
        }

        LocalDate today = LocalDate.now();
        List<VoucherTrendDTO.TrendPoint> points = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            long[] vals = dailyMap.getOrDefault(d, new long[]{0, 0});
            points.add(new VoucherTrendDTO.TrendPoint(d.toString(), vals[0], vals[1]));
        }

        return new VoucherTrendDTO(points);
    }

    public VoucherDistributionDTO getVoucherDistribution() {
        LocalDate today = LocalDate.now();

        List<VoucherFaceValueDaily> preAggregated = voucherFaceValueRepo.findByStatDateOrderByClaimCountDesc(today);
        if (preAggregated != null && !preAggregated.isEmpty()) {
            long total = preAggregated.stream().mapToLong(VoucherFaceValueDaily::getClaimCount).sum();
            List<VoucherDistributionDTO.FaceValueItem> items = new ArrayList<>();
            for (VoucherFaceValueDaily v : preAggregated) {
                double pct = total > 0 ? round1((double) v.getClaimCount() / total * 100) : 0;
                items.add(new VoucherDistributionDTO.FaceValueItem(v.getFaceAmount(), v.getClaimCount(), pct));
            }
            return new VoucherDistributionDTO(items);
        }

        List<Object[]> rows = voucherRecordRepo.findFaceValueDistributionByDate(today);
        if (rows.isEmpty()) {
            return new VoucherDistributionDTO(List.of());
        }
        long total = 0;
        for (Object[] row : rows) {
            total += ((Number) row[1]).longValue();
        }
        List<VoucherDistributionDTO.FaceValueItem> items = new ArrayList<>();
        for (Object[] row : rows) {
            int amount = ((Number) row[0]).intValue();
            int count = ((Number) row[1]).intValue();
            double pct = total > 0 ? round1((double) count / total * 100) : 0;
            items.add(new VoucherDistributionDTO.FaceValueItem(amount, count, pct));
        }
        return new VoucherDistributionDTO(items);
    }

    void aggregateVoucherMetrics(LocalDate date) {
        log.info("[voucher] aggregating for {}", date);

        saveMetric(date, KEY_VOUCHER_DAILY_COUNT, voucherRecordRepo.countSuccessUsersByDate(date));
        saveMetric(date, KEY_VOUCHER_DAILY_AMOUNT, voucherRecordRepo.sumSuccessAmountByDate(date));

        long prevTotalCount = 0;
        long prevTotalAmount = 0;
        Optional<MetricDaily> prevCount = metricRepo.findByMetricDateAndMetricKey(date.minusDays(1), KEY_VOUCHER_TOTAL_COUNT);
        Optional<MetricDaily> prevAmount = metricRepo.findByMetricDateAndMetricKey(date.minusDays(1), KEY_VOUCHER_TOTAL_AMOUNT);
        if (prevCount.isPresent() && prevCount.get().getMetricValue() != null) {
            prevTotalCount = prevCount.get().getMetricValue();
        }
        if (prevAmount.isPresent() && prevAmount.get().getMetricValue() != null) {
            prevTotalAmount = prevAmount.get().getMetricValue();
        }

        long todayCount = voucherRecordRepo.countSuccessUsersByDate(date);
        long todayAmount = voucherRecordRepo.sumSuccessAmountByDate(date);
        saveMetric(date, KEY_VOUCHER_TOTAL_COUNT, prevTotalCount + todayCount);
        saveMetric(date, KEY_VOUCHER_TOTAL_AMOUNT, prevTotalAmount + todayAmount);

        int year = date.getYear();
        int month = date.getMonthValue();
        saveMetric(date, KEY_VOUCHER_MONTHLY_COUNT, voucherRecordRepo.countSuccessUsersByMonth(year, month));
        saveMetric(date, KEY_VOUCHER_MONTHLY_AMOUNT, voucherRecordRepo.sumSuccessAmountByMonth(year, month));

        aggregateVoucherFaceValue(date);

        log.info("[voucher] done for {}", date);
    }

    private void aggregateVoucherFaceValue(LocalDate date) {
        List<Object[]> rows = voucherRecordRepo.findFaceValueDistributionByDate(date);
        for (Object[] row : rows) {
            int amountYuan = ((Number) row[0]).intValue();
            int count = ((Number) row[1]).intValue();

            VoucherFaceValueDaily.PK pk = new VoucherFaceValueDaily.PK(date, amountYuan);
            VoucherFaceValueDaily existing = voucherFaceValueRepo.findById(pk).orElse(null);
            if (existing != null) {
                existing.setClaimCount(count);
                existing.setUpdatedAt(java.time.LocalDateTime.now());
                voucherFaceValueRepo.save(existing);
            } else {
                voucherFaceValueRepo.save(new VoucherFaceValueDaily(date, amountYuan, count));
            }
        }
        log.info("[voucher] face value distribution aggregated for {}, {} groups", date, rows.size());
    }

    // ==================== Sandbox Resources ====================

    private static final String KEY_SANDBOX_DAILY_USERS = "sandbox_daily_users";
    private static final String KEY_SANDBOX_DAILY_SESSIONS = "sandbox_daily_sessions";
    private static final String KEY_SANDBOX_DAILY_SUCCESS = "sandbox_daily_success";
    private static final String KEY_SANDBOX_DAILY_FAIL = "sandbox_daily_fail";
    private static final String KEY_SANDBOX_DAILY_CREATE = "sandbox_daily_create";
    private static final String KEY_SANDBOX_DAILY_REUSE = "sandbox_daily_reuse";
    private static final String KEY_SANDBOX_TOTAL_USERS = "sandbox_total_users";
    private static final String KEY_SANDBOX_AVG_DURATION_MS = "sandbox_avg_duration_ms";
    private static final String KEY_SANDBOX_P95_DURATION_MS = "sandbox_p95_duration_ms";

    public SandboxSummaryDTO getSandboxSummary() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long totalUsers = getMetricValue(KEY_SANDBOX_TOTAL_USERS, today,
                sandboxSessionRepo::countAllDistinctUsers);

        long dailyUsers = getMetricValue(KEY_SANDBOX_DAILY_USERS, today,
                () -> sandboxSessionRepo.countDistinctUsersByDate(today));
        long yesterdayUsers = getMetricValue(KEY_SANDBOX_DAILY_USERS, yesterday,
                () -> sandboxSessionRepo.countDistinctUsersByDate(yesterday));
        double chainRatio = 0;
        if (yesterdayUsers > 0) {
            chainRatio = (double) (dailyUsers - yesterdayUsers) / yesterdayUsers * 100;
        } else if (dailyUsers > 0) {
            chainRatio = 100;
        }

        long avgMs = getMetricValue(KEY_SANDBOX_AVG_DURATION_MS, today, () -> calcAvgDuration(today));
        long yesterdayAvgMs = getMetricValue(KEY_SANDBOX_AVG_DURATION_MS, yesterday, () -> calcAvgDuration(yesterday));
        double avgSec = avgMs / 1000.0;
        double avgDeltaSec = (yesterdayAvgMs - avgMs) / 1000.0;

        long p95Ms = getMetricValue(KEY_SANDBOX_P95_DURATION_MS, today, () -> calcP95Duration(today));
        double p95Sec = p95Ms / 1000.0;

        long successCount = getMetricValue(KEY_SANDBOX_DAILY_SUCCESS, today, () -> 0);
        long failCount = getMetricValue(KEY_SANDBOX_DAILY_FAIL, today, () -> 0);
        long totalOps = successCount + failCount;
        double successRate = totalOps > 0 ? (double) successCount / totalOps * 100 : 0;

        return new SandboxSummaryDTO(totalUsers, dailyUsers, chainRatio,
                avgSec, avgDeltaSec, p95Sec, "<20s");
    }

    private long calcAvgDuration(LocalDate date) {
        List<Long> durations = sandboxSessionRepo.findSuccessDurationsByDate(date);
        if (durations == null || durations.isEmpty()) return 0;
        return (long) durations.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    private long calcP95Duration(LocalDate date) {
        List<Long> durations = sandboxSessionRepo.findSuccessDurationsByDate(date);
        if (durations == null || durations.isEmpty()) return 0;
        int idx = (int) Math.ceil(durations.size() * 0.95) - 1;
        if (idx < 0) idx = 0;
        if (idx >= durations.size()) idx = durations.size() - 1;
        return durations.get(idx);
    }

    public SandboxTrendDTO getSandboxTrend() {
        LocalDate startDate = LocalDate.now().minusDays(29);

        List<MetricDaily> userMetrics = metricRepo.findByKeySince(KEY_SANDBOX_DAILY_USERS, startDate);
        List<MetricDaily> eventMetrics = metricRepo.findByKeySince(KEY_SANDBOX_DAILY_SESSIONS, startDate);

        if (userMetrics != null && !userMetrics.isEmpty()) {
            List<SandboxTrendDTO.TrendPoint> daily = userMetrics.stream()
                    .map(m -> new SandboxTrendDTO.TrendPoint(m.getMetricDate().toString(), m.getMetricValue()))
                    .toList();
            List<SandboxTrendDTO.TrendPoint> events = (eventMetrics != null ? eventMetrics : List.<MetricDaily>of()).stream()
                    .map(m -> new SandboxTrendDTO.TrendPoint(m.getMetricDate().toString(), m.getMetricValue()))
                    .toList();
            long total = daily.stream().mapToLong(SandboxTrendDTO.TrendPoint::value).sum();
            return new SandboxTrendDTO(daily, events, total);
        }

        return new SandboxTrendDTO(List.of(), List.of(), 0);
    }

    public SandboxDurationDTO getSandboxDurationDistribution() {
        LocalDate today = LocalDate.now();

        List<SandboxDurationBucketDaily> buckets =
                sandboxBucketRepo.findByStatDateOrderByBucketOrder(today);

        if (buckets != null && !buckets.isEmpty()) {
            return new SandboxDurationDTO(today.toString(),
                    buckets.stream().map(b -> new SandboxDurationDTO.BucketItem(
                            bucketLabelToDisplay(b.getBucketLabel()),
                            b.getBucketOrder(), b.getCount()))
                            .toList());
        }

        return buildDurationFromTelemetry(today);
    }

    public SandboxHourlyDTO getSandboxHourly() {
        LocalDate today = LocalDate.now();

        List<SandboxHourlyStats> stats =
                sandboxHourlyRepo.findByStatDateOrderByHourOfDay(today);

        if (stats != null && !stats.isEmpty()) {
            Map<Integer, Integer> hourMap = new HashMap<>();
            for (SandboxHourlyStats s : stats) {
                hourMap.put(s.getHourOfDay(), s.getUserCount());
            }
            List<SandboxHourlyDTO.HourlyPoint> points = new ArrayList<>();
            for (int h = 0; h < 24; h++) {
                points.add(new SandboxHourlyDTO.HourlyPoint(h, hourMap.getOrDefault(h, 0)));
            }
            return new SandboxHourlyDTO(today.toString(), points);
        }

        return buildHourlyFromTelemetry(today);
    }

    void aggregateSandboxMetrics(LocalDate date) {
        log.info("[sandbox] aggregating for {}", date);

        long dailyUsers = sandboxSessionRepo.countDistinctUsersByDate(date);
        long dailySessions = sandboxSessionRepo.countSessionsByDate(date);
        saveMetric(date, KEY_SANDBOX_DAILY_USERS, dailyUsers);
        saveMetric(date, KEY_SANDBOX_DAILY_SESSIONS, dailySessions);

        List<Object[]> statusCounts = sandboxSessionRepo.countByStatusAndDate(date);
        long success = 0, fail = 0;
        for (Object[] row : statusCounts) {
            if ("success".equals(row[0])) success = ((Number) row[1]).longValue();
            else if ("fail".equals(row[0])) fail = ((Number) row[1]).longValue();
        }
        saveMetric(date, KEY_SANDBOX_DAILY_SUCCESS, success);
        saveMetric(date, KEY_SANDBOX_DAILY_FAIL, fail);

        List<Object[]> actionCounts = sandboxSessionRepo.countByActionAndDate(date);
        long create = 0, reuse = 0;
        for (Object[] row : actionCounts) {
            if ("create".equals(row[0])) create = ((Number) row[1]).longValue();
            else if ("reuse".equals(row[0])) reuse = ((Number) row[1]).longValue();
        }
        saveMetric(date, KEY_SANDBOX_DAILY_CREATE, create);
        saveMetric(date, KEY_SANDBOX_DAILY_REUSE, reuse);

        long prevTotal = getMetricValue(KEY_SANDBOX_TOTAL_USERS, date.minusDays(1),
                () -> sandboxSessionRepo.countAllDistinctUsers() - dailyUsers);
        saveMetric(date, KEY_SANDBOX_TOTAL_USERS, prevTotal + dailyUsers);

        List<Long> durations = sandboxSessionRepo.findSuccessDurationsByDate(date);
        if (durations != null && !durations.isEmpty()) {
            long avg = durations.stream().mapToLong(l -> l).sum() / durations.size();
            long p95 = calcP95Long(durations);
            saveMetric(date, KEY_SANDBOX_AVG_DURATION_MS, avg);
            saveMetric(date, KEY_SANDBOX_P95_DURATION_MS, p95);

            aggregateDurationBuckets(date, durations);
        }

        aggregateSandboxHourlyStats(date);

        log.info("[sandbox] done for {}", date);
    }

    private long calcP95(List<Double> durations) {
        List<Double> copy = new ArrayList<>(durations);
        Collections.sort(copy);
        int idx = (int) Math.ceil(copy.size() * 0.95) - 1;
        idx = Math.max(0, idx);
        return (long) copy.get(idx).doubleValue();
    }

    private long calcP95Long(List<Long> durations) {
        List<Long> copy = new ArrayList<>(durations);
        Collections.sort(copy);
        int idx = (int) Math.ceil(copy.size() * 0.95) - 1;
        idx = Math.max(0, idx);
        return copy.get(idx);
    }

    private void aggregateDurationBuckets(LocalDate date, List<Long> durations) {
        Object[][] buckets = {
            {"lt_5s",   1,      0,   5000},
            {"5_8s",    2,   5000,   8000},
            {"8_10s",   3,   8000,  10000},
            {"10_15s",  4,  10000,  15000},
            {"15_20s",  5,  15000,  20000},
            {"20_30s",  6,  20000,  30000},
            {"gt_30s",  7,  30000, Long.MAX_VALUE},
        };

        for (Object[] b : buckets) {
            String label = (String) b[0];
            int order = (int) b[1];
            long min = ((Number) b[2]).longValue();
            long max = ((Number) b[3]).longValue();
            long count = durations.stream()
                    .filter(d -> d >= min && d < max)
                    .count();

            SandboxDurationBucketDaily.PK pk = new SandboxDurationBucketDaily.PK(date, label);
            SandboxDurationBucketDaily existing = sandboxBucketRepo.findById(pk).orElse(null);
            if (existing != null) {
                existing.setCount((int) count);
                existing.setUpdatedAt(java.time.LocalDateTime.now());
                sandboxBucketRepo.save(existing);
            } else {
                sandboxBucketRepo.save(new SandboxDurationBucketDaily(date, label, order, (int) count));
            }
        }
    }

    private void aggregateSandboxHourlyStats(LocalDate date) {
        List<Object[]> rows = sandboxSessionRepo.hourlyUsersByDate(date);
        Map<Integer, Integer> hourMap = new HashMap<>();
        for (Object[] row : rows) {
            int hour = ((Number) row[0]).intValue();
            int count = ((Number) row[1]).intValue();
            hourMap.put(hour, count);
        }

        for (int h = 0; h < 24; h++) {
            int count = hourMap.getOrDefault(h, 0);
            SandboxHourlyStats.PK pk = new SandboxHourlyStats.PK(date, h);
            SandboxHourlyStats existing = sandboxHourlyRepo.findById(pk).orElse(null);
            if (existing != null) {
                existing.setUserCount(count);
                existing.setUpdatedAt(java.time.LocalDateTime.now());
                sandboxHourlyRepo.save(existing);
            } else {
                sandboxHourlyRepo.save(new SandboxHourlyStats(date, h, count));
            }
        }
    }

    private SandboxDurationDTO buildDurationFromTelemetry(LocalDate date) {
        List<Long> durations = sandboxSessionRepo.findSuccessDurationsByDate(date);
        if (durations == null || durations.isEmpty()) {
            return new SandboxDurationDTO(date.toString(), List.of());
        }

        Object[][] buckets = {
            {"lt_5s",   1,      0,   5000},
            {"5_8s",    2,   5000,   8000},
            {"8_10s",   3,   8000,  10000},
            {"10_15s",  4,  10000,  15000},
            {"15_20s",  5,  15000,  20000},
            {"20_30s",  6,  20000,  30000},
            {"gt_30s",  7,  30000, Long.MAX_VALUE},
        };

        List<SandboxDurationDTO.BucketItem> items = new ArrayList<>();
        for (Object[] b : buckets) {
            String label = (String) b[0];
            int order = (int) b[1];
            long min = ((Number) b[2]).longValue();
            long max = ((Number) b[3]).longValue();
            int count = (int) durations.stream()
                    .filter(d -> d >= min && d < max)
                    .count();
            items.add(new SandboxDurationDTO.BucketItem(bucketLabelToDisplay(label), order, count));
        }
        return new SandboxDurationDTO(date.toString(), items);
    }

    private SandboxHourlyDTO buildHourlyFromTelemetry(LocalDate date) {
        List<Object[]> rows = sandboxSessionRepo.hourlyUsersByDate(date);
        Map<Integer, Integer> hourMap = new HashMap<>();
        for (Object[] row : rows) {
            int hour = ((Number) row[0]).intValue();
            int count = ((Number) row[1]).intValue();
            hourMap.put(hour, count);
        }

        List<SandboxHourlyDTO.HourlyPoint> points = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            points.add(new SandboxHourlyDTO.HourlyPoint(h, hourMap.getOrDefault(h, 0)));
        }
        return new SandboxHourlyDTO(date.toString(), points);
    }

    private static String bucketLabelToDisplay(String label) {
        return switch (label) {
            case "lt_5s" -> "<5s";
            case "5_8s" -> "5-8s";
            case "8_10s" -> "8-10s";
            case "10_15s" -> "10-15s";
            case "15_20s" -> "15-20s";
            case "20_30s" -> "20-30s";
            case "gt_30s" -> ">30s";
            default -> label;
        };
    }
}
