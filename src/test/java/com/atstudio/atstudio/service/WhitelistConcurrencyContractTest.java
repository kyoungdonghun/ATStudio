package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.WhitelistChannel;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.WhitelistChannelRepository;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Version;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Whitelist concurrency and schema source contracts")
class WhitelistConcurrencyContractTest {

    @Test
    @DisplayName("user and export lock queries use pessimistic write locks")
    void lockQueriesUsePessimisticWrite() throws Exception {
        Lock userLock = UserRepository.class
                .getMethod("findByIdForUpdate", Long.class)
                .getAnnotation(Lock.class);
        Lock channelLock = WhitelistChannelRepository.class
                .getMethod("findByIdForUpdate", Long.class)
                .getAnnotation(Lock.class);
        Lock exportChannelLock = WhitelistChannelRepository.class
                .getMethod(
                        "findAllByIdForUpdate",
                        Collection.class)
                .getAnnotation(Lock.class);

        assertThat(userLock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(channelLock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(exportChannelLock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    @DisplayName("whitelist channel has an optimistic version fence")
    void whitelistChannelHasVersionFence() throws Exception {
        assertThat(WhitelistChannel.class.getDeclaredField("version").isAnnotationPresent(Version.class))
                .isTrue();
    }

    @Test
    @DisplayName("fresh and retained schema sources contain whitelist integrity columns")
    void schemaSourcesContainWhitelistIntegrityColumns() throws Exception {
        String schema = Files.readString(Path.of("src/main/resources/schema.sql"));
        String patch = Files.readString(Path.of(
                "src/main/resources/db/manual/20260716_whitelist_integrity_and_exports.sql"));

        assertThat(schema)
                .contains("version              BIGINT       NOT NULL DEFAULT 0")
                .contains("KEY idx_whitelist_channels_status_requested (status, requested_at, id)")
                .contains("status_filter ENUM")
                .contains("item_order                   INT")
                .contains("channel_id_snapshot         BIGINT");
        assertThat(patch)
                .contains("ADD COLUMN version BIGINT NOT NULL DEFAULT 0")
                .contains("idx_whitelist_channels_export_scope")
                .contains("channel_id_snapshot");
    }
}
