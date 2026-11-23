package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.ListeningProperties;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.repository.TrackDownloadRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PilotAccessTest {
    private final ListeningProperties properties = new ListeningProperties();
    private final UserSubscriptionRepository grants = mock(UserSubscriptionRepository.class);
    private final BillingAgreementRepository agreements = mock(BillingAgreementRepository.class);
    private final PaymentOrderRepository orders = mock(PaymentOrderRepository.class);
    private final SubscriptionPaymentRepository payments = mock(SubscriptionPaymentRepository.class);
    private final ListeningCatalogService catalog = mock(ListeningCatalogService.class);
    private final PilotAccess access = new PilotAccess(properties, catalog,
            grants, agreements, orders, payments);
    private final LocalDate today = LocalDate.now(PilotAccess.SEOUL);
    private final User user = User.builder().id(51L).role(UserRole.USER).isVerified(true).build();

    private void enroll(LocalDate start, LocalDate end, SubscriptionStatus status, int limit) {
        properties.setEnabled(true);
        properties.setPilotEnabled(true);
        properties.setPilotUserIds(Set.of(user.getId()));
        Subscription plan = Subscription.builder().isActive(true).downloadPerDay(limit).build();
        UserSubscription grant = UserSubscription.builder().user(user).subscription(plan)
                .startedAt(start).expiresAt(end).status(status).build();
        when(grants.findByUser_Id(user.getId())).thenReturn(Optional.of(grant));
    }

    @Test void defaultsClosedAndEnrollmentIsExplicit() {
        assertFalse(access.eligible(user.getId()));
        enroll(today, today, SubscriptionStatus.CANCELLED, 1);
        assertTrue(access.eligible(user.getId()));
        assertFalse(access.eligible(52L));
        properties.setEnabled(false);
        assertFalse(access.eligible(user.getId()));
    }

    @Test void rejectsFutureExpiredUnboundedAndRenewingGrants() {
        enroll(today.plusDays(1), today.plusDays(2), SubscriptionStatus.CANCELLED, 2);
        assertFalse(access.eligible(user.getId()));
        enroll(today.minusDays(2), today.minusDays(1), SubscriptionStatus.CANCELLED, 2);
        assertFalse(access.eligible(user.getId()));
        enroll(today, today, SubscriptionStatus.CANCELLED, -1);
        assertFalse(access.eligible(user.getId()));
        enroll(today, today, SubscriptionStatus.ACTIVE, 2);
        assertFalse(access.eligible(user.getId()));
    }

    @Test void dailyQuotaUsesAHalfOpenSeoulDayInStorageTime() {
        TrackDownloadRepository downloads = mock(TrackDownloadRepository.class);
        LocalDateTime start = LocalDateTime.ofInstant(Instant.parse("2026-11-20T15:00:00Z"), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(Instant.parse("2026-11-21T15:00:00Z"), ZoneId.systemDefault());
        when(downloads.countInPeriod(user, start, end)).thenReturn(3L);
        assertEquals(3L, PilotAccess.dayCount(downloads, user, LocalDate.of(2026, 11, 21)));
    }

    @Test void pinsPublicationBeforeEntitlementOrMetadataCanStartAConsistentRead() {
        enroll(today, today, SubscriptionStatus.CANCELLED, 1);
        when(catalog.snapshot()).thenReturn(new ListeningCatalogService.Catalog(3, List.of(7L)));
        assertEquals(List.of(7L), access.publishedIdsFor(user.getId()));
        var ordered = inOrder(catalog, grants);
        ordered.verify(catalog).snapshot();
        ordered.verify(grants).findByUser_Id(user.getId());
    }

    @Test void canceledPaidHistoryCannotBecomeATrial() {
        enroll(today, today.plusDays(1), SubscriptionStatus.CANCELLED, 2);
        when(orders.existsByUser_Id(user.getId())).thenReturn(true);
        assertFalse(access.eligible(user.getId()));
        when(orders.existsByUser_Id(user.getId())).thenReturn(false);
        when(agreements.existsByUser_Id(user.getId())).thenReturn(true);
        assertFalse(access.eligible(user.getId()));
        when(agreements.existsByUser_Id(user.getId())).thenReturn(false);
        when(payments.existsByUser(user)).thenReturn(true);
        assertFalse(access.eligible(user.getId()));
    }

    @Test void libraryReadsPinCatalogBeforeLoadingGrantAndKeepEmptySelectionClosed() {
        enroll(today, today, SubscriptionStatus.CANCELLED, 2);
        when(catalog.snapshot()).thenReturn(new ListeningCatalogService.Catalog(4, List.of()));
        PilotAccess.Library library = access.libraryForRead(user.getId());

        assertFalse(library.includes(7L));
        var ordered = inOrder(catalog, grants);
        ordered.verify(catalog).snapshot();
        ordered.verify(grants).findByUser_Id(user.getId());
    }

    @Test void libraryWritesLockGrantAndCatalogBeforeEntitlementReads() {
        enroll(today, today, SubscriptionStatus.CANCELLED, 2);
        var grant = grants.findByUser_Id(user.getId());
        when(grants.findByUserIDForUpdate(user.getId())).thenReturn(grant);
        when(catalog.snapshot()).thenReturn(new ListeningCatalogService.Catalog(4, List.of(8L)));
        clearInvocations(grants, catalog, orders);
        PilotAccess.Library library = access.libraryForWrite(user);

        assertTrue(library.includes(8L));
        assertFalse(library.includes(9L));
        var ordered = inOrder(grants, catalog, orders);
        ordered.verify(grants).findByUserIDForUpdate(user.getId());
        ordered.verify(catalog).snapshot();
        ordered.verify(orders).existsByUser_Id(user.getId());
    }
}
