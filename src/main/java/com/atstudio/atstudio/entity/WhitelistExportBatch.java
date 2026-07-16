package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "whitelist_export_batches")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WhitelistExportBatch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "item_count", nullable = false)
    private int itemCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exported_by")
    private User exportedBy;

    @Column(length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_filter", length = 30)
    private WhitelistChannelStatus statusFilter;

    @Column(name = "keyword_filter", length = 100)
    private String keywordFilter;
}
