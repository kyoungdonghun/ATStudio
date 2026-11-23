package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.ListeningProperties;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.repository.TrackDownloadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PilotAccess {
    static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private final ListeningProperties properties;
    private final ListeningCatalogService catalog;
    private final UserSubscriptionRepository subscriptions;
    private final BillingAgreementRepository agreements;
    private final PaymentOrderRepository orders;
    private final SubscriptionPaymentRepository payments;

    public boolean enabled() {
        return properties.pilotActive();
    }

    @Transactional(readOnly = true)
    public boolean eligible(Long id) {
        if (!enabled() || !properties.getPilotUserIds().contains(id)) return false;
        return subscriptions.findByUser_Id(id).filter(this::valid).isPresent();
    }

    UserSubscription requireLockedGrant(User user) {
        if (!properties.getPilotUserIds().contains(user.getId())) throw denied();
        return subscriptions.findByUserIDForUpdate(user.getId()).filter(this::valid)
                .orElseThrow(PilotAccess::denied);
    }

    private boolean valid(UserSubscription grant) {
        User u = grant.getUser();
        LocalDate today = LocalDate.now(SEOUL);
        return u.getRole() == UserRole.USER && u.isVerified() && !u.isDeleted()
                && grant.getStatus() == SubscriptionStatus.CANCELLED && !grant.hasPending()
                && !grant.getStartedAt().isAfter(today) && !grant.getExpiresAt().isBefore(today)
                && grant.getSubscription().isActive() && grant.getSubscription().getDownloadPerDay() > 0
                && !orders.existsByUser_Id(u.getId()) && !agreements.existsByUser_Id(u.getId())
                && !payments.existsByUser(u);
    }

    List<Long> publishedIdsFor(Long userId) {
        // Take the catalog lock before any ordinary owner/entitlement read opens the
        // metadata snapshot. Hold it until all personal record projections are built.
        List<Long> published = publishedIds();
        if (!eligible(userId)) throw denied();
        return published;
    }

    List<Long> publishedIds() {
        return catalog.snapshot().trackIds();
    }

    static long dayCount(TrackDownloadRepository downloads, User user, LocalDate date) {
        return downloads.countInPeriod(user, dayStart(date), dayStart(date.plusDays(1)));
    }

    private static LocalDateTime dayStart(LocalDate seoulDate) {
        // Auditing persists LocalDateTime in the process default time zone.
        return seoulDate.atStartOfDay(SEOUL).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static BusinessException denied() {
        return new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
    }
}
