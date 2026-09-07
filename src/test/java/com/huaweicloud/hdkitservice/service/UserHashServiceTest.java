package com.huaweicloud.hdkitservice.service;

import com.huaweicloud.hdkitservice.model.UserHashRecord;
import com.huaweicloud.hdkitservice.repository.UserHashRecordRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void resolveHashFlagsMapsHashToGenByAsk() {
        UserHashRecordRepository repository = mock(UserHashRecordRepository.class);
        UserHashService service = new UserHashService(repository, true);

        UserHashRecord domainRec = new UserHashRecord("1", "hashA", "domainA", false,
                LocalDateTime.now(), LocalDateTime.now());
        UserHashRecord akRec = new UserHashRecord("2", "hashB", null, true,
                LocalDateTime.now(), LocalDateTime.now());
        when(repository.findByUserIdHashIn(anyCollection())).thenReturn(List.of(domainRec, akRec));

        Map<String, Boolean> flags = service.resolveHashFlags(Set.of("hashA", "hashB", "missing"));

        assertEquals(2, flags.size());
        assertEquals(false, flags.get("hashA"));
        assertTrue(flags.get("hashB"));
    }

    @Test
    void resolveHashFlagsEmptyInputReturnsEmpty() {
        UserHashRecordRepository repository = mock(UserHashRecordRepository.class);
        UserHashService service = new UserHashService(repository, true);

        assertTrue(service.resolveHashFlags(Set.of()).isEmpty());
    }
}