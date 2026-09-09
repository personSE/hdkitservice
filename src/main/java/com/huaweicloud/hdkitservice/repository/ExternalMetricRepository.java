package com.huaweicloud.hdkitservice.repository;

import com.huaweicloud.hdkitservice.model.ExternalMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExternalMetricRepository extends JpaRepository<ExternalMetric, Long> {

    List<ExternalMetric> findByMetricKeyAndMetricDate(String metricKey, LocalDate metricDate);

    List<ExternalMetric> findByMetricKeyAndSourceAndMetricDate(String metricKey, String source, LocalDate metricDate);

    @Query("SELECT e FROM ExternalMetric e WHERE e.metricKey = :key AND e.metricDate BETWEEN :start AND :end ORDER BY e.metricDate")
    List<ExternalMetric> findByKeyAndDateRange(@Param("key") String metricKey,
                                                @Param("start") LocalDate start,
                                                @Param("end") LocalDate end);

    @Query("SELECT e FROM ExternalMetric e WHERE e.metricKey = :key AND e.source = :source AND e.metricDate BETWEEN :start AND :end ORDER BY e.metricDate")
    List<ExternalMetric> findByKeySourceAndDateRange(@Param("key") String metricKey,
                                                      @Param("source") String source,
                                                      @Param("start") LocalDate start,
                                                      @Param("end") LocalDate end);

    @Query("SELECT SUM(e.metricValue) FROM ExternalMetric e WHERE e.metricKey = :key AND e.metricDate = :date")
    Long sumValueByKeyAndDate(@Param("key") String metricKey, @Param("date") LocalDate date);

    @Query("SELECT SUM(e.metricValue) FROM ExternalMetric e WHERE e.metricKey = :key AND e.source = :source AND e.metricDate = :date")
    Long sumValueByKeySourceAndDate(@Param("key") String metricKey, @Param("source") String source, @Param("date") LocalDate date);

    @Query("SELECT e.metricDate, SUM(e.metricValue) FROM ExternalMetric e WHERE e.metricKey = :key AND e.metricDate BETWEEN :start AND :end GROUP BY e.metricDate ORDER BY e.metricDate")
    List<Object[]> dailySumByKeyAndDateRange(@Param("key") String metricKey, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
