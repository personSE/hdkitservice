package com.huaweicloud.hdkitservice.repository;

import com.huaweicloud.hdkitservice.model.TelemetryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TelemetryEventRepository extends JpaRepository<TelemetryEvent, String> {

    @Query(value = "SELECT COUNT(DISTINCT e.user_hash) FROM telemetry_event e " +
            "WHERE e.user_hash IS NOT NULL AND e.user_hash <> '' " +
            "AND e.user_hash <> 'test' AND e.user_hash <> 'sha256hash1234'",
            nativeQuery = true)
    long countDistinctUserHash();

    @Query(value = "SELECT COUNT(DISTINCT e.user_hash) FROM telemetry_event e " +
            "WHERE e.user_hash IS NOT NULL AND e.user_hash <> '' " +
            "AND e.user_hash <> 'test' AND e.user_hash <> 'sha256hash1234' " +
            "AND DATE(e.server_time) = :date",
            nativeQuery = true)
    long countDistinctUserHashByDate(@Param("date") LocalDate date);

    @Query(value = "SELECT COUNT(DISTINCT e.user_hash) FROM telemetry_event e " +
            "WHERE e.user_hash IS NOT NULL AND e.user_hash <> '' " +
            "AND e.user_hash <> 'test' AND e.user_hash <> 'sha256hash1234' " +
            "AND DATE(e.server_time) >= :startDate",
            nativeQuery = true)
    long countDistinctUserHashSince(@Param("startDate") LocalDate startDate);

    @Query(value = "SELECT COUNT(DISTINCT e.user_hash) FROM telemetry_event e " +
            "WHERE e.user_hash IS NOT NULL AND e.user_hash <> '' " +
            "AND e.user_hash <> 'test' AND e.user_hash <> 'sha256hash1234' " +
            "AND DATE(e.server_time) BETWEEN :prevStart AND :prevEnd",
            nativeQuery = true)
    long countDistinctUserHashBetween(@Param("prevStart") LocalDate prevStart,
                                      @Param("prevEnd") LocalDate prevEnd);

    @Query(value = "SELECT COUNT(DISTINCT CONCAT(e.install_id, '_', COALESCE(e.agent_harness, 'unknown'))) " +
            "FROM telemetry_event e WHERE e.install_id IS NOT NULL AND e.install_id <> '' AND e.install_id <> 'test'",
            nativeQuery = true)
    long countDistinctAgentHarness();

    @Query(value = "SELECT DATE(e.server_time) AS d, COUNT(DISTINCT e.user_hash) AS cnt " +
            "FROM telemetry_event e " +
            "WHERE e.user_hash IS NOT NULL AND e.user_hash <> '' " +
            "AND e.user_hash <> 'test' AND e.user_hash <> 'sha256hash1234' " +
            "AND DATE(e.server_time) >= :startDate " +
            "GROUP BY DATE(e.server_time) " +
            "ORDER BY d",
            nativeQuery = true)
    List<Object[]> dailyActiveUsersSince(@Param("startDate") LocalDate startDate);

    @Query(value = "SELECT COALESCE(e.agent_harness, 'unknown') AS agent, " +
            "COUNT(DISTINCT e.install_id) AS cnt " +
            "FROM telemetry_event e " +
            "WHERE e.install_id IS NOT NULL AND e.install_id <> '' " +
            "AND e.install_id <> 'test' " +
            "GROUP BY COALESCE(e.agent_harness, 'unknown') " +
            "ORDER BY cnt DESC",
            nativeQuery = true)
    List<Object[]> agentDistribution();

    // ==================== Open Capabilities (v2.1: skill by event_key, mcp/cli by capability) ====================

    // v2.1: 能力调用总次数 — skill 用 event_key，mcp/cli 用 capability 字段
    @Query(value = "SELECT COUNT(*) FROM telemetry_event e " +
            "WHERE e.event_key = 'skill:retrieve' " +
            "   OR e.capability = 'mcp' " +
            "   OR e.capability = 'cli'",
            nativeQuery = true)
    long capabilityCallCounts();

    // v2.1: 能力调用去重用户数 — 同上条件 + 排除 test 数据
    @Query(value = "SELECT COUNT(DISTINCT e.user_hash) FROM telemetry_event e " +
            "WHERE (e.event_key = 'skill:retrieve' " +
            "   OR e.capability = 'mcp' " +
            "   OR e.capability = 'cli') " +
            "AND e.user_hash IS NOT NULL AND e.user_hash <> '' " +
            "AND e.user_hash <> 'test'",
            nativeQuery = true)
    long countDistinctUsersWithCapability();

    // v2.1: 按能力类型分布 — skill 用 event_key，mcp/cli 用 capability 字段
    @Query(value = "SELECT " +
            "CASE " +
            "  WHEN e.event_key = 'skill:retrieve' THEN 'skill' " +
            "  WHEN e.capability = 'mcp' THEN 'mcp' " +
            "  WHEN e.capability = 'cli' THEN 'cli' " +
            "END AS cap_type, COUNT(*) AS cnt " +
            "FROM telemetry_event e " +
            "WHERE e.event_key = 'skill:retrieve' " +
            "   OR e.capability = 'mcp' " +
            "   OR e.capability = 'cli' " +
            "GROUP BY cap_type ORDER BY cnt DESC",
            nativeQuery = true)
    List<Object[]> capabilityCallCountsByCap();

    // v2.1: 按日期×能力类型趋势
    @Query(value = "SELECT DATE(e.server_time) AS d, " +
            "CASE " +
            "  WHEN e.event_key = 'skill:retrieve' THEN 'skill' " +
            "  WHEN e.capability = 'mcp' THEN 'mcp' " +
            "  WHEN e.capability = 'cli' THEN 'cli' " +
            "END AS cap_type, COUNT(*) AS cnt " +
            "FROM telemetry_event e " +
            "WHERE (e.event_key = 'skill:retrieve' " +
            "   OR e.capability = 'mcp' " +
            "   OR e.capability = 'cli') " +
            "AND DATE(e.server_time) >= :startDate " +
            "GROUP BY DATE(e.server_time), cap_type ORDER BY d, cap_type",
            nativeQuery = true)
    List<Object[]> capabilityCallsByDate(@Param("startDate") LocalDate startDate);

    // v2.1: 按日期聚合（预聚合任务用）
    @Query(value = "SELECT " +
            "CASE " +
            "  WHEN e.event_key = 'skill:retrieve' THEN 'skill' " +
            "  WHEN e.capability = 'mcp' THEN 'mcp' " +
            "  WHEN e.capability = 'cli' THEN 'cli' " +
            "END AS cap_type, COUNT(*) AS cnt " +
            "FROM telemetry_event e " +
            "WHERE (e.event_key = 'skill:retrieve' " +
            "   OR e.capability = 'mcp' " +
            "   OR e.capability = 'cli') " +
            "AND DATE(e.server_time) = :date " +
            "GROUP BY cap_type",
            nativeQuery = true)
    List<Object[]> capabilityCallCountsBySpecificDate(@Param("date") LocalDate date);

    // v2.1: 按能力类型+日期统计去重用户
    @Query(value = "SELECT COUNT(DISTINCT e.user_hash) FROM telemetry_event e " +
            "WHERE CASE " +
            "  WHEN :capability = 'skill' THEN e.event_key = 'skill:retrieve' " +
            "  WHEN :capability = 'mcp' THEN e.capability = 'mcp' " +
            "  WHEN :capability = 'cli' THEN e.capability = 'cli' " +
            "END " +
            "AND e.user_hash IS NOT NULL AND e.user_hash <> '' " +
            "AND e.user_hash <> 'test' " +
            "AND DATE(e.server_time) = :date",
            nativeQuery = true)
    long countDistinctUsersByCapabilityAndDate(@Param("capability") String capability,
                                                @Param("date") LocalDate date);

    // v2.1: 某日能力调用总次数
    @Query(value = "SELECT COUNT(*) FROM telemetry_event e " +
            "WHERE (e.event_key = 'skill:retrieve' " +
            "   OR e.capability = 'mcp' " +
            "   OR e.capability = 'cli') " +
            "AND DATE(e.server_time) = :date",
            nativeQuery = true)
    long capabilityCallCountByDate(@Param("date") LocalDate date);

    // v2.0: Skill 排行 — event_key='skill:retrieve'，名称取自 event_value
    @Query(value = "SELECT e.event_value AS skill_name, COUNT(*) AS cnt " +
            "FROM telemetry_event e " +
            "WHERE e.event_key = 'skill:retrieve' " +
            "AND e.event_value IS NOT NULL AND e.event_value <> '' " +
            "GROUP BY e.event_value ORDER BY cnt DESC LIMIT :limit",
            nativeQuery = true)
    List<Object[]> skillRanking(@Param("limit") int limit);

    // v2.0: Skill 按日期趋势 — event_key='skill:retrieve'
    @Query(value = "SELECT DATE(e.server_time) AS d, e.event_value AS skill_name, COUNT(*) AS cnt " +
            "FROM telemetry_event e " +
            "WHERE e.event_key = 'skill:retrieve' " +
            "AND e.event_value IS NOT NULL AND e.event_value <> '' " +
            "AND DATE(e.server_time) >= :startDate " +
            "GROUP BY DATE(e.server_time), e.event_value ORDER BY d, cnt DESC",
            nativeQuery = true)
    List<Object[]> skillCallsByDate(@Param("startDate") LocalDate startDate);

    // ==================== Sandbox Resources (v2.0: tool:*sandbox* events) ====================

    @Query(value = "SELECT COUNT(DISTINCT e.user_hash) FROM telemetry_event e " +
            "WHERE e.event_key LIKE 'tool:%sandbox%' " +
            "AND DATE(e.server_time) = :date " +
            "AND e.user_hash <> 'test' " +
            "AND (e.install_id IS NULL OR e.install_id <> 'test')",
            nativeQuery = true)
    long countSandboxUsersByDate(@Param("date") LocalDate date);

    @Query(value = "SELECT COUNT(*) FROM telemetry_event e " +
            "WHERE e.event_key LIKE 'tool:%sandbox%' " +
            "AND DATE(e.server_time) = :date " +
            "AND e.user_hash <> 'test' " +
            "AND (e.install_id IS NULL OR e.install_id <> 'test')",
            nativeQuery = true)
    long countSandboxEventsByDate(@Param("date") LocalDate date);

    @Query(value = "SELECT CAST(e.event_value AS DECIMAL) AS duration " +
            "FROM telemetry_event e " +
            "WHERE e.event_key LIKE 'tool:%sandbox%' " +
            "AND DATE(e.server_time) = :date " +
            "AND e.event_value REGEXP '^[0-9]+(\\\\.[0-9]+)?$' " +
            "AND e.user_hash <> 'test' " +
            "AND (e.install_id IS NULL OR e.install_id <> 'test')",
            nativeQuery = true)
    List<Double> sandboxDurationsByDate(@Param("date") LocalDate date);

    @Query(value = "SELECT HOUR(e.server_time) AS h, COUNT(DISTINCT e.user_hash) AS cnt " +
            "FROM telemetry_event e " +
            "WHERE e.event_key LIKE 'tool:%sandbox%' " +
            "AND DATE(e.server_time) = :date " +
            "AND e.user_hash <> 'test' " +
            "AND (e.install_id IS NULL OR e.install_id <> 'test') " +
            "GROUP BY HOUR(e.server_time) ORDER BY h",
            nativeQuery = true)
    List<Object[]> sandboxHourlyUsersByDate(@Param("date") LocalDate date);

    @Query(value = "SELECT COUNT(DISTINCT e.user_hash) FROM telemetry_event e " +
            "WHERE e.event_key LIKE 'tool:%sandbox%' " +
            "AND e.user_hash <> 'test' " +
            "AND (e.install_id IS NULL OR e.install_id <> 'test')",
            nativeQuery = true)
    long countAllSandboxUsers();

    @Query(value = "SELECT COUNT(DISTINCT e.user_hash) FROM telemetry_event e " +
            "WHERE e.user_hash IS NOT NULL AND e.user_hash <> '' " +
            "AND e.user_hash <> 'test' AND e.user_hash <> 'sha256hash1234' " +
            "AND DATE(e.server_time) = :date " +
            "AND e.user_hash NOT IN (" +
            "  SELECT DISTINCT e2.user_hash FROM telemetry_event e2 " +
            "  WHERE DATE(e2.server_time) < :date " +
            "  AND e2.user_hash IS NOT NULL AND e2.user_hash <> '' " +
            "  AND e2.user_hash <> 'test' AND e2.user_hash <> 'sha256hash1234'" +
            ")",
            nativeQuery = true)
    long countNewUsersByDate(@Param("date") LocalDate date);
}
