package com.huaweicloud.hdkitservice.repository;

import com.huaweicloud.hdkitservice.model.SandboxSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SandboxSessionRepository extends JpaRepository<SandboxSession, Long> {

    @Query("SELECT COUNT(DISTINCT s.akHash) FROM SandboxSession s WHERE FUNCTION('DATE', s.createdAt) = :date")
    Long countDistinctUsersByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(s) FROM SandboxSession s WHERE FUNCTION('DATE', s.createdAt) = :date")
    Long countSessionsByDate(@Param("date") LocalDate date);

    @Query("SELECT s.status, COUNT(s) FROM SandboxSession s WHERE FUNCTION('DATE', s.createdAt) = :date GROUP BY s.status")
    List<Object[]> countByStatusAndDate(@Param("date") LocalDate date);

    @Query("SELECT s.action, COUNT(s) FROM SandboxSession s WHERE FUNCTION('DATE', s.createdAt) = :date GROUP BY s.action")
    List<Object[]> countByActionAndDate(@Param("date") LocalDate date);

    @Query("SELECT s.durationMs FROM SandboxSession s WHERE FUNCTION('DATE', s.createdAt) = :date AND s.durationMs IS NOT NULL")
    List<Long> findDurationsByDate(@Param("date") LocalDate date);

    @Query("SELECT HOUR(s.createdAt), COUNT(DISTINCT s.akHash) FROM SandboxSession s WHERE FUNCTION('DATE', s.createdAt) = :date GROUP BY HOUR(s.createdAt) ORDER BY HOUR(s.createdAt)")
    List<Object[]> hourlyUsersByDate(@Param("date") LocalDate date);

    @Query("SELECT FUNCTION('DATE', s.createdAt), COUNT(DISTINCT s.akHash) FROM SandboxSession s WHERE FUNCTION('DATE', s.createdAt) >= :startDate GROUP BY FUNCTION('DATE', s.createdAt) ORDER BY FUNCTION('DATE', s.createdAt)")
    List<Object[]> dailyUsersSince(@Param("startDate") LocalDate startDate);

    @Query("SELECT FUNCTION('DATE', s.createdAt), COUNT(s) FROM SandboxSession s WHERE FUNCTION('DATE', s.createdAt) >= :startDate GROUP BY FUNCTION('DATE', s.createdAt) ORDER BY FUNCTION('DATE', s.createdAt)")
    List<Object[]> dailySessionsSince(@Param("startDate") LocalDate startDate);

    @Query("SELECT COUNT(DISTINCT s.akHash) FROM SandboxSession s")
    Long countAllDistinctUsers();

    @Query("SELECT s.durationMs FROM SandboxSession s WHERE FUNCTION('DATE', s.createdAt) = :date AND s.durationMs IS NOT NULL AND s.status = 'success' ORDER BY s.durationMs")
    List<Long> findSuccessDurationsByDate(@Param("date") LocalDate date);
}
