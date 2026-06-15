package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "whitelist_channels")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WhitelistChannel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String channelUrl;

    @Column(nullable = false, length = 100)
    private String channelName;

    @Column(length = 100)
    private String youtubeHandle;

    @Column(length = 100)
    private String youtubeChannelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private WhitelistChannelStatus status = WhitelistChannelStatus.DRAFT;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean primary = false;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "exported_at")
    private LocalDateTime exportedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "removal_requested_at")
    private LocalDateTime removalRequestedAt;

    @Column(name = "admin_note", length = 500)
    private String adminNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    public void update(String channelUrl, String channelName, String youtubeHandle, String youtubeChannelId) {
        this.channelUrl = channelUrl;
        this.channelName = channelName;
        this.youtubeHandle = youtubeHandle;
        this.youtubeChannelId = youtubeChannelId;

        if (this.status == WhitelistChannelStatus.REGISTERED
                || this.status == WhitelistChannelStatus.EXPORTED
                || this.status == WhitelistChannelStatus.REVISION_REQUESTED) {
            requestRegistration();
        }
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public void requestRegistration() {
        this.status = WhitelistChannelStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
        this.exportedAt = null;
        this.processedAt = null;
        this.removalRequestedAt = null;
    }

    public void cancelRequest() {
        this.status = WhitelistChannelStatus.CANCELLED;
    }

    public void markExported() {
        this.status = WhitelistChannelStatus.EXPORTED;
        this.exportedAt = LocalDateTime.now();
    }

    public void requestRemoval() {
        this.status = WhitelistChannelStatus.REMOVAL_REQUESTED;
        this.removalRequestedAt = LocalDateTime.now();
    }

    public void updateAdminStatus(WhitelistChannelStatus status, User processedBy, String adminNote) {
        this.status = status;
        this.processedBy = processedBy;
        this.adminNote = adminNote;
        this.processedAt = LocalDateTime.now();

        if (status == WhitelistChannelStatus.REGISTERED) {
            this.removalRequestedAt = null;
        }
    }

    public boolean canBeDeletedImmediately() {
        return status == WhitelistChannelStatus.DRAFT
                || status == WhitelistChannelStatus.PENDING
                || status == WhitelistChannelStatus.REVISION_REQUESTED
                || status == WhitelistChannelStatus.REJECTED
                || status == WhitelistChannelStatus.CANCELLED;
    }

    public static boolean countsAgainstPlanLimit(WhitelistChannelStatus status) {
        return status == WhitelistChannelStatus.PENDING
                || status == WhitelistChannelStatus.EXPORTED
                || status == WhitelistChannelStatus.REGISTERED
                || status == WhitelistChannelStatus.REVISION_REQUESTED
                || status == WhitelistChannelStatus.REMOVAL_REQUESTED;
    }
}
