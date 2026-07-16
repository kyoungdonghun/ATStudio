package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.WhitelistExportItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WhitelistExportItemRepository extends JpaRepository<WhitelistExportItem, Long> {

    @Query("""
            SELECT item
            FROM WhitelistExportItem item
            WHERE item.batch.id = :batchID
            ORDER BY CASE WHEN item.itemOrder IS NULL THEN 1 ELSE 0 END,
                     item.itemOrder ASC,
                     item.id ASC
            """)
    List<WhitelistExportItem> findImmutableBatchItems(
            @Param("batchID") Long batchID,
            Pageable pageable);
}
