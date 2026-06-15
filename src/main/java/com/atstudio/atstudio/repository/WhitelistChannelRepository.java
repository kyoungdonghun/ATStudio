package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.WhitelistChannel;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WhitelistChannelRepository extends JpaRepository<WhitelistChannel, Long> {

    List<WhitelistChannel> findByUserOrderByCreatedAtDesc(User user);

    List<WhitelistChannel> findByUserOrderByPrimaryDescCreatedAtDesc(User user);

    long countByUser(User user);

    long countByUserAndStatusIn(User user, Collection<WhitelistChannelStatus> statuses);

    Optional<WhitelistChannel> findByUserAndPrimaryTrue(User user);

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
    List<WhitelistChannel> findByStatusOrderByRequestedAtAsc(WhitelistChannelStatus status);

    void deleteAllByUser(User user);
}
