package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportSummaryResponse;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.WhitelistExportBatch;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("WhitelistExportBatchRepository tests")
class WhitelistExportBatchRepositoryTest {

    @Autowired WhitelistExportBatchRepository whitelistExportBatchRepository;
    @Autowired UserRepository userRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("recent summaries are owner and exact-scope isolated, newest first, and limited to ten")
    void recentSummariesAreOwnedScopedOrderedAndBounded() {
        User owner = userRepository.save(user("owner", "owner@test.com"));
        User otherAdmin = userRepository.save(user("other-admin", "other-admin@test.com"));
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 13, 12, 0);
        List<Long> matchingIDs = new ArrayList<>();

        for (int index = 0; index < 12; index++) {
            matchingIDs.add(saveBatch(
                    owner,
                    WhitelistChannelStatus.PENDING,
                    index % 2 == 0 ? "Shorts" : "shorts",
                    createdAt,
                    "matching-" + index + ".csv").getId());
        }
        saveBatch(
                owner,
                WhitelistChannelStatus.PENDING,
                "shorts",
                createdAt.minusDays(1),
                "older-with-later-id.csv");
        saveBatch(
                otherAdmin,
                WhitelistChannelStatus.PENDING,
                "shorts",
                createdAt.plusDays(1),
                "other-owner.csv");
        saveBatch(
                owner,
                WhitelistChannelStatus.EXPORTED,
                "shorts",
                createdAt.plusDays(1),
                "other-status.csv");
        saveBatch(
                owner,
                WhitelistChannelStatus.PENDING,
                "other",
                createdAt.plusDays(1),
                "other-keyword.csv");

        List<AdminWhitelistExportSummaryResponse> result =
                whitelistExportBatchRepository.findRecentSummariesByOwnerAndExactScope(
                        owner.getId(),
                        WhitelistChannelStatus.PENDING,
                        "shorts",
                        PageRequest.of(0, 10));
        List<Long> expectedIDs = new ArrayList<>(matchingIDs);
        Collections.reverse(expectedIDs);

        assertThat(result).hasSize(10);
        assertThat(result)
                .extracting(AdminWhitelistExportSummaryResponse::batchId)
                .containsExactlyElementsOf(expectedIDs.subList(0, 10));
        assertThat(result)
                .extracting(AdminWhitelistExportSummaryResponse::fileName)
                .allMatch(fileName -> fileName.startsWith("matching-"));
    }

    @Test
    @DisplayName("keyword-only recent lookup requires a recorded all-status scope")
    void keywordOnlyRecentLookupRequiresAllStatusScope() {
        User owner = userRepository.save(user("owner-all", "owner-all@test.com"));
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 13, 12, 0);
        WhitelistExportBatch allStatusBatch = saveBatch(
                owner,
                null,
                "MixedScope",
                createdAt,
                "all-status.csv");
        saveBatch(
                owner,
                WhitelistChannelStatus.PENDING,
                "MixedScope",
                createdAt.plusMinutes(1),
                "pending.csv");

        List<AdminWhitelistExportSummaryResponse> result =
                whitelistExportBatchRepository.findRecentSummariesByOwnerAndExactScope(
                        owner.getId(),
                        null,
                        "mixedscope",
                        PageRequest.of(0, 10));

        assertThat(result)
                .extracting(AdminWhitelistExportSummaryResponse::batchId)
                .containsExactly(allStatusBatch.getId());
        assertThat(result.get(0).status()).isNull();
    }

    private WhitelistExportBatch saveBatch(
            User owner,
            WhitelistChannelStatus status,
            String keyword,
            LocalDateTime createdAt,
            String fileName
    ) {
        WhitelistExportBatch batch = WhitelistExportBatch.builder()
                .fileName(fileName)
                .itemCount(1)
                .exportedBy(owner)
                .statusFilter(status)
                .keywordFilter(keyword)
                .build();
        WhitelistExportBatch saved = whitelistExportBatchRepository.saveAndFlush(batch);
        jdbcTemplate.update(
                "UPDATE whitelist_export_batches SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(createdAt),
                saved.getId());
        return saved;
    }

    private User user(String nickname, String email) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .password("pw")
                .role(UserRole.ADMIN)
                .build();
    }
}
