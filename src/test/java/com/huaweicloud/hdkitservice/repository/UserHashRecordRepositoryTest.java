package com.huaweicloud.hdkitservice.repository;

import com.huaweicloud.hdkitservice.model.UserHashRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserHashRecordRepositoryTest {

    @Autowired
    private UserHashRecordRepository repository;

    @Test
    void upsertInsertsAndDeduplicatesOnUserIdHash() {
        LocalDateTime now = LocalDateTime.now();
        String id = UUID.randomUUID().toString();

        repository.upsert(id, "hashA", "domainA", false, now);
        repository.upsert(UUID.randomUUID().toString(), "hashA", "domainA", false, now.plusMinutes(1));

        assertEquals(1, repository.count());

        UserHashRecord rec = repository.findAll().get(0);
        assertEquals("hashA", rec.getUserIdHash());
        assertEquals("domainA", rec.getDomainId());
        assertFalse(rec.getIsGenByAsk());
        assertEquals(id, rec.getId());
        assertTrue(rec.getLastUpdated().isAfter(rec.getFirstCreated()));
    }

    @Test
    void upsertStoresFallbackFlagWhenGenByAsk() {
        repository.upsert(UUID.randomUUID().toString(), "hashB", null, true, LocalDateTime.now());

        UserHashRecord rec = repository.findAll().get(0);
        assertNull(rec.getDomainId());
        assertTrue(rec.getIsGenByAsk());
    }
}