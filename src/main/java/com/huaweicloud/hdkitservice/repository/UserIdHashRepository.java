package com.huaweicloud.hdkitservice.repository;

import com.huaweicloud.hdkitservice.model.UserIdHash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserIdHashRepository extends JpaRepository<UserIdHash, String> {

    @Query("SELECT COUNT(u) FROM UserIdHash u WHERE u.userIdHash IS NOT NULL AND u.userIdHash <> ''")
    long countAll();

    @Query("SELECT COUNT(u) FROM UserIdHash u WHERE u.userIdHash IS NOT NULL AND u.userIdHash <> '' AND FUNCTION('DATE', u.firstCreated) = :date")
    long countNewByDate(@Param("date") LocalDate date);

    @Query("SELECT FUNCTION('DATE', u.firstCreated), COUNT(u) FROM UserIdHash u " +
            "WHERE u.userIdHash IS NOT NULL AND u.userIdHash <> '' " +
            "AND FUNCTION('DATE', u.firstCreated) >= :startDate " +
            "GROUP BY FUNCTION('DATE', u.firstCreated) ORDER BY FUNCTION('DATE', u.firstCreated)")
    List<Object[]> dailyNewSince(@Param("startDate") LocalDate startDate);
}
