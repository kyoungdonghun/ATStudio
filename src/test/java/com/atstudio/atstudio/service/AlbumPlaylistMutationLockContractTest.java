package com.atstudio.atstudio.service;

import com.atstudio.atstudio.repository.AlbumRepository;
import com.atstudio.atstudio.repository.PlaylistRepository;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class AlbumPlaylistMutationLockContractTest {

    @Test
    void albumAndPlaylistMutationLookupsDeclarePessimisticWriteLocks() throws Exception {
        assertPessimisticWrite(AlbumRepository.class.getMethod("findByIdForUpdate", Long.class));
        assertPessimisticWrite(PlaylistRepository.class.getMethod("findByIdForUpdate", Long.class));
    }

    private void assertPessimisticWrite(Method method) {
        Lock lock = method.getAnnotation(Lock.class);
        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }
}
