package com.huaweicloud.hdkitservice.repository;

import com.huaweicloud.hdkitservice.model.GitHubStatsDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GitHubStatsDailyRepository extends JpaRepository<GitHubStatsDaily, LocalDate> {

    Optional<GitHubStatsDaily> findByStatDate(LocalDate statDate);

    @Query("SELECT g FROM GitHubStatsDaily g WHERE g.statDate >= :startDate ORDER BY g.statDate")
    List<GitHubStatsDaily> findSince(@Param("startDate") LocalDate startDate);

    @Query("SELECT g FROM GitHubStatsDaily g ORDER BY g.statDate DESC LIMIT 1")
    Optional<GitHubStatsDaily> findLatest();

    @Query("SELECT g FROM GitHubStatsDaily g ORDER BY g.statDate DESC LIMIT :limit")
    List<GitHubStatsDaily> findTopN(@Param("limit") int limit);
}
