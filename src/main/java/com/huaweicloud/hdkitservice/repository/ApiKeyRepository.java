package com.huaweicloud.hdkitservice.repository;

import com.huaweicloud.hdkitservice.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByApiKeyAndEnabledTrue(String apiKey);
}
