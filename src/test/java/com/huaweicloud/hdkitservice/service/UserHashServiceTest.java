package com.huaweicloud.hdkitservice.service;

import com.huaweicloud.hdkitservice.repository.UserHashRecordRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UserHashServiceTest {

    @Test
    void recordDomainIdHashWhenFallbackPersistenceDisabled() {
        UserHashRecordRepository repository = mock(UserHashRecordRepository.class);
        UserHashService service = new UserHashService(repository, false);

        service.record("hashFromDomain", "domain123", false);

        verify(repository).upsert(anyString(), anyString(), anyString(), anyBoolean(), any(LocalDateTime.class));
    }

    @Test
    void skipAkFallbackWhenPersistenceDisabled() {
        UserHashRecordRepository repository = mock(UserHashRecordRepository.class);
        UserHashService service = new UserHashService(repository, false);

        service.record("hashFromAk", null, true);

        verify(repository, never()).upsert(anyString(), anyString(), anyString(), anyBoolean(), any(LocalDateTime.class));
    }

    @Test
    void persistAkFallbackWhenEnabledByDefault() {
        UserHashRecordRepository repository = mock(UserHashRecordRepository.class);
        UserHashService service = new UserHashService(repository, true);

        service.record("hashFromAk", null, true);

        verify(repository).upsert(anyString(), anyString(), isNull(), anyBoolean(), any(LocalDateTime.class));
    }
}