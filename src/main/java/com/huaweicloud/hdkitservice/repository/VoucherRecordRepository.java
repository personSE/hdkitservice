package com.huaweicloud.hdkitservice.repository;

import com.huaweicloud.hdkitservice.model.VoucherRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VoucherRecordRepository extends JpaRepository<VoucherRecord, Long> {

    @Query("SELECT COUNT(DISTINCT v.akHash) FROM VoucherRecord v WHERE FUNCTION('DATE', v.createdAt) = :date AND v.status = 'success'")
    Long countSuccessUsersByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(v.faceAmount), 0) FROM VoucherRecord v WHERE FUNCTION('DATE', v.createdAt) = :date AND v.status = 'success'")
    Long sumSuccessAmountByDate(@Param("date") LocalDate date);

    @Query("SELECT v.status, COUNT(v) FROM VoucherRecord v WHERE FUNCTION('DATE', v.createdAt) = :date GROUP BY v.status")
    List<Object[]> countByStatusAndDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT v.akHash) FROM VoucherRecord v WHERE v.status = 'success'")
    Long countAllSuccessUsers();

    @Query("SELECT COALESCE(SUM(v.faceAmount), 0) FROM VoucherRecord v WHERE v.status = 'success'")
    Long sumAllSuccessAmount();

    @Query("SELECT COUNT(DISTINCT v.akHash) FROM VoucherRecord v WHERE YEAR(v.createdAt) = :year AND MONTH(v.createdAt) = :month AND v.status = 'success'")
    Long countSuccessUsersByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(v.faceAmount), 0) FROM VoucherRecord v WHERE YEAR(v.createdAt) = :year AND MONTH(v.createdAt) = :month AND v.status = 'success'")
    Long sumSuccessAmountByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT FUNCTION('DATE', v.createdAt), COUNT(DISTINCT v.akHash), COALESCE(SUM(v.faceAmount), 0) FROM VoucherRecord v WHERE FUNCTION('DATE', v.createdAt) >= :startDate AND v.status = 'success' GROUP BY FUNCTION('DATE', v.createdAt) ORDER BY FUNCTION('DATE', v.createdAt)")
    List<Object[]> findDailySuccessStatsSince(@Param("startDate") LocalDate startDate);

    @Query("SELECT v.faceAmount, COUNT(v) FROM VoucherRecord v WHERE FUNCTION('DATE', v.createdAt) = :date AND v.status = 'success' GROUP BY v.faceAmount ORDER BY COUNT(v) DESC")
    List<Object[]> findFaceValueDistributionByDate(@Param("date") LocalDate date);
}
