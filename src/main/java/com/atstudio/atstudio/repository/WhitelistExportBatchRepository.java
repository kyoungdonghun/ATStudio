package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportSummaryResponse;
import com.atstudio.atstudio.entity.WhitelistExportBatch;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WhitelistExportBatchRepository extends JpaRepository<WhitelistExportBatch, Long> {

    @Query("""
            SELECT new com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportSummaryResponse(
                batch.id,
                batch.fileName,
                batch.itemCount,
                batch.statusFilter,
                batch.keywordFilter,
                batch.createdAt)
            FROM WhitelistExportBatch batch
            WHERE batch.exportedBy.id = :ownerID
              AND ((:status IS NULL AND batch.statusFilter IS NULL) OR batch.statusFilter = :status)
              AND ((:keyword IS NULL AND batch.keywordFilter IS NULL)
                   OR (:keyword IS NOT NULL AND LOWER(batch.keywordFilter) = :keyword))
            ORDER BY batch.createdAt DESC, batch.id DESC
            """)
    List<AdminWhitelistExportSummaryResponse> findRecentSummariesByOwnerAndExactScope(
            @Param("ownerID") Long ownerID,
            @Param("status") WhitelistChannelStatus status,
            @Param("keyword") String normalizedKeyword,
            Pageable pageable);
}
