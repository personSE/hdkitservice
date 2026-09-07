package com.huaweicloud.hdkitservice.service;

import com.huaweicloud.hdkitservice.model.UserHashRecord;
import com.huaweicloud.hdkitservice.repository.UserHashRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserHashService {

    private static final Logger log = LoggerFactory.getLogger(UserHashService.class);

    private final UserHashRecordRepository repository;
    private final boolean persistAkFallback;

    public UserHashService(UserHashRecordRepository repository,
                           @Value("${hdkit.userhash.persist-ak-fallback:true}") boolean persistAkFallback) {
        this.repository = repository;
        this.persistAkFallback = persistAkFallback;
    }

    public void record(String userHash, String domainId, boolean genByAsk) {
        if (userHash == null || userHash.isEmpty()) {
            return;
        }
        if (genByAsk && !persistAkFallback) {
            log.debug("[userhash] persist-ak-fallback disabled, skip ak-generated userHash");
            return;
        }
        try {
            repository.upsert(UUID.randomUUID().toString(), userHash, domainId, genByAsk, LocalDateTime.now());
        } catch (Exception e) {
            log.warn("[userhash] persist failed, ignored: {}", e.getMessage());
        }
    }

    public Map<String, Boolean> resolveHashFlags(Collection<String> userHashes) {
        Map<String, Boolean> flags = new HashMap<>();
        if (userHashes == null || userHashes.isEmpty()) {
            return flags;
        }
        for (UserHashRecord rec : repository.findByUserIdHashIn(userHashes)) {
            flags.put(rec.getUserIdHash(), Boolean.TRUE.equals(rec.getIsGenByAsk()));
        }
        return flags;
    }
}