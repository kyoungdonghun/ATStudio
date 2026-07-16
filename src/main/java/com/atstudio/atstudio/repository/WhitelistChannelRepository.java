package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.WhitelistChannel;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WhitelistChannelRepository extends JpaRepository<WhitelistChannel, Long> {

    List<WhitelistChannel> findByUserOrderByPrimaryDescCreatedAtDesc(User user, Pageable pageable);

    long countByUser(User user);

    long countByUserAndStatusIn(User user, Collection<WhitelistChannelStatus> statuses);

    Optional<WhitelistChannel> findByUserAndPrimaryTrue(User user);

    boolean existsByUserAndPrimaryTrue(User user);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE WhitelistChannel wc SET wc.primary = false WHERE wc.user.id = :userID AND wc.primary = true")
    int clearPrimaryByUserID(@Param("userID") Long userID);

    @Query("""
            SELECT wc
            FROM WhitelistChannel wc
            WHERE wc.user = :user
              AND wc.id <> :excludedChannelID
              AND wc.status NOT IN ('REMOVAL_REQUESTED', 'CANCELLED')
            ORDER BY wc.createdAt DESC, wc.id DESC
            """)
    List<WhitelistChannel> findPrimaryReplacement(
            @Param("user") User user,
            @Param("excludedChannelID") Long excludedChannelID,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT wc FROM WhitelistChannel wc WHERE wc.id = :channelID")
    Optional<WhitelistChannel> findByIdForUpdate(@Param("channelID") Long channelID);

    @EntityGraph(attributePaths = {"user", "processedBy"})
    @Query("""
            SELECT wc
            FROM WhitelistChannel wc
            WHERE (:status IS NULL OR wc.status = :status)
              AND (:keyword IS NULL
                   OR LOWER(wc.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(wc.user.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(wc.channelName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(wc.channelUrl) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(wc.youtubeHandle) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(wc.youtubeChannelId) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<WhitelistChannel> searchForAdmin(
            @Param("status") WhitelistChannelStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
            SELECT wc
            FROM WhitelistChannel wc
            WHERE (:status IS NULL OR wc.status = :status)
              AND (:keyword IS NULL
                   OR LOWER(wc.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(wc.user.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(wc.channelName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(wc.channelUrl) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(wc.youtubeHandle) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(wc.youtubeChannelId) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY wc.requestedAt ASC, wc.id ASC
            """)
    List<WhitelistChannel> findExportCandidates(
            @Param("status") WhitelistChannelStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"user"})
    @Query("""
            SELECT wc
            FROM WhitelistChannel wc
            WHERE wc.id IN :channelIDs
            ORDER BY wc.requestedAt ASC, wc.id ASC
            """)
    List<WhitelistChannel> findAllByIdForUpdate(@Param("channelIDs") Collection<Long> channelIDs);

    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE WhitelistChannel wc
            SET wc.status = 'REMOVAL_REQUESTED',
                wc.removalRequestedAt = :requestedAt,
                wc.primary = false
            WHERE wc.user = :user
              AND wc.status IN ('EXPORTED', 'REGISTERED')
            """)
    int requestExternalRemovalForWithdrawal(
            @Param("user") User user,
            @Param("requestedAt") java.time.LocalDateTime requestedAt);

    void deleteAllByUserAndStatusIn(User user, Collection<WhitelistChannelStatus> statuses);
}
