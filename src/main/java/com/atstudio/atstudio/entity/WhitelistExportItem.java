package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "whitelist_export_items")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WhitelistExportItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private WhitelistExportBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "whitelist_channel_id")
    private WhitelistChannel whitelistChannel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_at_export", nullable = false, length = 30)
    private WhitelistChannelStatus statusAtExport;

    @Column(name = "item_order")
    private Integer itemOrder;

    @Column(name = "channel_id_snapshot")
    private Long channelIdSnapshot;

    @Column(name = "user_id_snapshot")
    private Long userIdSnapshot;

    @Column(name = "user_email_snapshot", nullable = false, length = 100)
    private String userEmailSnapshot;

    @Column(name = "user_nickname_snapshot", length = 20)
    private String userNicknameSnapshot;

    @Column(name = "channel_name_snapshot", nullable = false, length = 100)
    private String channelNameSnapshot;

    @Column(name = "youtube_handle_snapshot", length = 100)
    private String youtubeHandleSnapshot;

    @Column(name = "channel_url_snapshot", nullable = false, length = 255)
    private String channelUrlSnapshot;

    @Column(name = "youtube_channel_id_snapshot", length = 100)
    private String youtubeChannelIdSnapshot;

    @Column(name = "plan_name_snapshot", length = 30)
    private String planNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle_snapshot", length = 20)
    private BillingCycle billingCycleSnapshot;

    @Column(name = "requested_at_snapshot")
    private LocalDateTime requestedAtSnapshot;

    @Column(name = "exported_at_snapshot", nullable = false)
    private LocalDateTime exportedAtSnapshot;
}
