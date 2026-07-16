package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.License;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Download concurrency and schema source contracts")
class DownloadConcurrencyContractTest {

    @Test
    @DisplayName("first-download decisions use the narrow pessimistic user lock")
    void firstDownloadDecisionUsesPessimisticUserLock() throws Exception {
        Lock userLock = UserRepository.class
                .getMethod("findByIdForUpdate", Long.class)
                .getAnnotation(Lock.class);
        Transactional transaction = DownloadService.class
                .getMethod("download", Long.class, CustomUserDetails.class)
                .getAnnotation(Transactional.class);
        String downloadService = Files.readString(Path.of(
                "src/main/java/com/atstudio/atstudio/service/DownloadService.java"));

        assertThat(userLock).isNotNull();
        assertThat(userLock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(transaction).isNotNull();
        assertThat(transaction.readOnly()).isFalse();
        assertThat(downloadService).contains("userRepository.findByIdForUpdate(userDetails.getId())");
    }

    @Test
    @DisplayName("one license per user-track pair is retained in JPA and schema sources")
    void licenseUserTrackInvariantIsPresentInJpaAndSchemaSources() throws Exception {
        Table table = License.class.getAnnotation(Table.class);
        String schema = Files.readString(Path.of("src/main/resources/schema.sql"));
        String patch = Files.readString(Path.of(
                "src/main/resources/db/manual/20260716_download_atomicity.sql"));

        assertThat(table).isNotNull();
        assertThat(Arrays.stream(table.uniqueConstraints())
                .anyMatch(constraint -> Arrays.equals(
                        constraint.columnNames(), new String[]{"user_id", "track_id"})))
                .isTrue();
        assertThat(schema).contains("UNIQUE KEY uq_licenses_user_track (user_id, track_id)");
        assertThat(patch).contains("ADD CONSTRAINT uq_licenses_user_track UNIQUE (user_id, track_id)");
    }

    @Test
    @DisplayName("cross-user first downloads use one atomic track count update")
    void crossUserFirstDownloadsUseAtomicTrackCountUpdate() throws Exception {
        Method incrementMethod = TrackRepository.class
                .getMethod("incrementDownloadCountAtomically", Long.class);
        Modifying modifying = incrementMethod.getAnnotation(Modifying.class);
        Query query = incrementMethod.getAnnotation(Query.class);
        String downloadService = Files.readString(Path.of(
                "src/main/java/com/atstudio/atstudio/service/DownloadService.java"));

        assertThat(incrementMethod.getReturnType()).isEqualTo(int.class);
        assertThat(modifying).isNotNull();
        assertThat(modifying.flushAutomatically()).isTrue();
        assertThat(query.value()).contains("t.downloadCount = t.downloadCount + 1")
                .contains("t.id = :trackId");
        assertThat(downloadService)
                .contains("trackRepository.incrementDownloadCountAtomically(track.getId())")
                .contains("updatedRows != 1")
                .doesNotContain("track.incrementDownloadCount()");
    }
}
