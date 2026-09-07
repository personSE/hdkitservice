package com.huaweicloud.hdkitservice.repository;

import com.huaweicloud.hdkitservice.model.UserHashRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface UserHashRecordRepository extends JpaRepository<UserHashRecord, String> {

    List<UserHashRecord> findByUserIdHashIn(Collection<String> userHashes);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_id_hash (id, user_id_hash, domain_id, is_gen_by_ask, first_created, last_updated) " +
            "VALUES (:id, :userHash, :domainId, :genByAsk, :now, :now) " +
            "ON DUPLICATE KEY UPDATE last_updated = :now",
            nativeQuery = true)
    int upsert(@Param("id") String id,
               @Param("userHash") String userHash,
               @Param("domainId") String domainId,
               @Param("genByAsk") Boolean genByAsk,
               @Param("now") LocalDateTime now);
}