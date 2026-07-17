package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository.CommandLockProjection;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.CompletedProviderLookupClaim;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.EvidenceAssessment;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.ProviderLookupClaim;
import com.atstudio.atstudio.service.payment.provider.recurring.ProviderPaymentLookupResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentReconciliationTransactionService evidence gate tests")
class PaymentReconciliationTransactionServiceTest {

    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock PaymentRefundRepository paymentRefundRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock PaymentCommandTransactionService paymentCommandTransactionService;

    PaymentReconciliationTransactionService service;

    @BeforeEach
    void setUp() {
        service = new PaymentReconciliationTransactionService(
                paymentOrderRepository,
                billingAgreementRepository,
                subscriptionPaymentRepository,
                paymentRefundRepository,
                userSubscriptionRepository,
                paymentCommandTransactionService);
    }

    @Test
    @DisplayName("DONE order batch uses an ID keyset and reports only missing finalization rows")
    void doneOrderBatchUsesKeyset() {
        PaymentOrder missingPayment = paymentOrder(5L);
        PaymentOrder finalizedPayment = mock(PaymentOrder.class);
        given(finalizedPayment.getId()).willReturn(9L);
        given(paymentOrderRepository.findLocalReconciliationCandidates(
                eq(PaymentOrderStatus.DONE),
                any(),
                eq(0L),
                any()))
                .willReturn(List.of(missingPayment, finalizedPayment));
        given(subscriptionPaymentRepository.existsByPaymentOrder(missingPayment)).willReturn(false);
        given(subscriptionPaymentRepository.existsByPaymentOrder(finalizedPayment)).willReturn(true);

        PaymentReconciliationTransactionService.LocalReconciliationBatch batch =
                service.reconcileDoneOrderBatch(0L, 2);

        assertThat(batch.checked()).isEqualTo(2);
        assertThat(batch.lastSeenID()).isEqualTo(9L);
        assertThat(batch.exhausted()).isFalse();
        assertThat(batch.issues()).singleElement()
                .extracting(PaymentReconciliationService.LocalReconciliationIssue::paymentOrderId)
                .isEqualTo(5L);
    }

    @Test
    @DisplayName("ACTIVE agreement batch ends on an empty page without moving the cursor")
    void activeAgreementBatchHandlesEmptyPage() {
        given(billingAgreementRepository.findLocalReconciliationCandidates(
                eq(BillingAgreementStatus.ACTIVE),
                eq(17L),
                any()))
                .willReturn(List.of());

        PaymentReconciliationTransactionService.LocalReconciliationBatch batch =
                service.reconcileActiveAgreementBatch(17L, 100, LocalDate.of(2026, 7, 16));

        assertThat(batch.checked()).isZero();
        assertThat(batch.lastSeenID()).isEqualTo(17L);
        assertThat(batch.exhausted()).isTrue();
        assertThat(batch.issues()).isEmpty();
    }

    @Test
    @DisplayName("only exact provider, order, status, amount, currency, and transaction evidence passes")
    void exactDoneEvidence_passes() {
        ProviderLookupClaim claim = claim(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        ProviderPaymentLookupResult result = ProviderPaymentLookupResult.found(
                claim.provider(),
                claim.orderID(),
                "tx-exact",
                "DONE",
                claim.amount(),
                claim.currency(),
                "{}");

        EvidenceAssessment assessment = service.assessProviderEvidence(claim, result);

        assertThat(assessment.exactDone()).isTrue();
    }

    @Test
    @DisplayName("currency mismatch is a strict Incident-only result")
    void currencyMismatch_isIncidentOnly() {
        ProviderLookupClaim claim = claim(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        ProviderPaymentLookupResult result = ProviderPaymentLookupResult.found(
                claim.provider(),
                claim.orderID(),
                "tx-exact",
                "DONE",
                claim.amount(),
                "USD",
                "{}");

        EvidenceAssessment assessment = service.assessProviderEvidence(claim, result);

        assertThat(assessment.exactDone()).isFalse();
        assertThat(assessment.issueType())
                .isEqualTo(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED);
        assertThat(assessment.failureCode()).isEqualTo("CURRENCY_MISMATCH");
    }

    @Test
    @DisplayName("missing provider identity is a strict PROVIDER_MISMATCH Incident")
    void providerMismatch_isIncidentOnly() {
        ProviderLookupClaim claim = claim(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        ProviderPaymentLookupResult result = ProviderPaymentLookupResult.found(
                null,
                claim.orderID(),
                "tx-exact",
                "DONE",
                claim.amount(),
                claim.currency(),
                "{}");

        EvidenceAssessment assessment = service.assessProviderEvidence(claim, result);

        assertThat(assessment.exactDone()).isFalse();
        assertThat(assessment.issueType())
                .isEqualTo(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED);
        assertThat(assessment.failureCode()).isEqualTo("PROVIDER_MISMATCH");
        assertThat(assessment.failureMessage()).containsIgnoringCase("provider");
    }

    @Test
    @DisplayName("order status and transaction conflicts all fail the strict gate")
    void orderStatusAndTransactionMismatches_areIncidentOnly() {
        ProviderLookupClaim claim = claim(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        List<ProviderPaymentLookupResult> mismatches = List.of(
                ProviderPaymentLookupResult.found(
                        claim.provider(),
                        "OTHER-ORDER",
                        "tx-exact",
                        "DONE",
                        claim.amount(),
                        claim.currency(),
                        "{}"),
                ProviderPaymentLookupResult.found(
                        claim.provider(),
                        claim.orderID(),
                        "tx-exact",
                        "CANCELED",
                        claim.amount(),
                        claim.currency(),
                        "{}"),
                ProviderPaymentLookupResult.found(
                        claim.provider(),
                        claim.orderID(),
                        " ",
                        "DONE",
                        claim.amount(),
                        claim.currency(),
                        "{}"));

        assertThat(mismatches)
                .map(result -> service.assessProviderEvidence(claim, result))
                .extracting(EvidenceAssessment::failureCode)
                .containsExactly(
                        "ORDER_ID_MISMATCH",
                        "PROVIDER_STATUS_MISMATCH",
                        "PROVIDER_TRANSACTION_MISSING");
    }

    @Test
    @DisplayName("persisted provider success requires the same authoritative transaction ID")
    void providerSucceededTransactionMismatch_isIncidentOnly() {
        ProviderLookupClaim claim = claim(PaymentOrderStatus.PROVIDER_SUCCEEDED, "tx-retained");
        ProviderPaymentLookupResult result = ProviderPaymentLookupResult.found(
                claim.provider(),
                claim.orderID(),
                "tx-conflict",
                "DONE",
                claim.amount(),
                claim.currency(),
                "{}");

        EvidenceAssessment assessment = service.assessProviderEvidence(claim, result);

        assertThat(assessment.exactDone()).isFalse();
        assertThat(assessment.failureCode()).isEqualTo("PROVIDER_TRANSACTION_MISMATCH");
    }

    @Test
    @DisplayName("completed provider lookup accepts only recent final payment orders and retains refund evidence")
    void loadCompletedProviderLookup_filtersAndCarriesRefundEvidence() {
        LocalDateTime createdAfter = LocalDateTime.of(2026, 7, 1, 0, 0);
        PaymentOrder valid = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.DONE, true);
        ReflectionTestUtils.setField(valid, "createdAt", createdAfter.plusDays(1));
        given(paymentOrderRepository.findById(valid.getId())).willReturn(Optional.of(valid));
        given(paymentRefundRepository.existsByPaymentOrder_IdAndStatus(
                valid.getId(), com.atstudio.atstudio.entity.enums.PaymentRefundStatus.SUCCEEDED))
                .willReturn(true);

        Optional<CompletedProviderLookupClaim> claim =
                service.loadCompletedProviderLookup(valid.getId(), createdAfter);

        assertThat(claim).isPresent();
        assertThat(claim.orElseThrow().locallyRefundedOrCancelled()).isTrue();
        assertThat(claim.orElseThrow().claim().mutationEligible()).isTrue();
    }

    @Test
    @DisplayName("completed lookup rejects missing, non-DONE, non-final, undated, and old orders")
    void loadCompletedProviderLookup_rejectsIneligibleOrders() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0);
        PaymentOrder nonDone = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.FAILED, true);
        PaymentOrder nonFinal = order(PaymentPurpose.BILLING_AGREEMENT, PaymentOrderStatus.DONE, true);
        PaymentOrder undated = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.DONE, true);
        PaymentOrder old = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.DONE, true);
        ReflectionTestUtils.setField(nonDone, "createdAt", boundary.plusDays(1));
        ReflectionTestUtils.setField(nonFinal, "createdAt", boundary.plusDays(1));
        ReflectionTestUtils.setField(old, "createdAt", boundary.minusSeconds(1));

        given(paymentOrderRepository.findById(1L)).willReturn(Optional.empty());
        given(paymentOrderRepository.findById(2L)).willReturn(Optional.of(nonDone));
        given(paymentOrderRepository.findById(3L)).willReturn(Optional.of(nonFinal));
        given(paymentOrderRepository.findById(4L)).willReturn(Optional.of(undated));
        given(paymentOrderRepository.findById(5L)).willReturn(Optional.of(old));

        assertThat(service.loadCompletedProviderLookup(1L, boundary)).isEmpty();
        assertThat(service.loadCompletedProviderLookup(2L, boundary)).isEmpty();
        assertThat(service.loadCompletedProviderLookup(3L, boundary)).isEmpty();
        assertThat(service.loadCompletedProviderLookup(4L, boundary)).isEmpty();
        assertThat(service.loadCompletedProviderLookup(5L, boundary)).isEmpty();
    }

    @Test
    @DisplayName("read-only lookup accepts purpose-correct subscribe, upgrade, and renewal evidence")
    void loadProviderLookup_acceptsAllFinalPaymentPurposes() {
        LocalDateTime staleBefore = LocalDateTime.of(2026, 7, 16, 0, 0);

        assertEligibleLookup(order(PaymentPurpose.SUBSCRIBE, PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, false), staleBefore);
        assertEligibleLookup(order(PaymentPurpose.UPGRADE, PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, true), staleBefore);
        assertEligibleLookup(order(PaymentPurpose.RENEWAL, PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, true), staleBefore);
    }

    @Test
    @DisplayName("read-only lookup exposes relationship and purpose-state failures as Incident-only claims")
    void loadProviderLookup_invalidEvidenceIsNotMutationEligible() {
        LocalDateTime staleBefore = LocalDateTime.of(2026, 7, 16, 0, 0);
        PaymentOrder missingProjection = order(
                PaymentPurpose.RENEWAL, PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, true);
        given(paymentOrderRepository.findById(missingProjection.getId())).willReturn(Optional.of(missingProjection));
        given(paymentOrderRepository.findCommandLockProjectionByOrderId(missingProjection.getOrderId()))
                .willReturn(Optional.empty());

        ProviderLookupClaim relationshipClaim = service
                .loadProviderLookup(missingProjection.getId(), staleBefore)
                .orElseThrow();

        assertThat(relationshipClaim.mutationEligible()).isFalse();
        assertThat(relationshipClaim.localEligibilityFailure()).isEqualTo("LOCAL_RELATIONSHIP_MISMATCH");

        PaymentOrder billingOnly = order(
                PaymentPurpose.BILLING_AGREEMENT, PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, false);
        stubReadOnlyRelationship(billingOnly, projection(billingOnly));

        ProviderLookupClaim purposeClaim = service
                .loadProviderLookup(billingOnly.getId(), staleBefore)
                .orElseThrow();

        assertThat(purposeClaim.mutationEligible()).isFalse();
        assertThat(purposeClaim.localEligibilityFailure()).isEqualTo("LOCAL_EVIDENCE_INVALID");
    }

    @Test
    @DisplayName("read-only lookup rejects missing orders, non-candidate states, and missing locked relationships")
    void loadProviderLookup_rejectsMissingOrNonCandidateOrders() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 16, 0, 0);
        PaymentOrder ready = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.READY, true);
        given(paymentOrderRepository.findById(99L)).willReturn(Optional.empty());
        given(paymentOrderRepository.findById(ready.getId())).willReturn(Optional.of(ready));

        assertThat(service.loadProviderLookup(99L, boundary)).isEmpty();
        assertThat(service.loadProviderLookup(ready.getId(), boundary)).isEmpty();
    }

    @Test
    @DisplayName("write claim locks the same evidence and rejects a missing locked order")
    void claimProviderLookup_locksAndRevalidatesEvidence() {
        LocalDateTime staleBefore = LocalDateTime.of(2026, 7, 16, 0, 0);
        PaymentOrder order = order(PaymentPurpose.RENEWAL, PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, true);
        CommandLockProjection projection = projection(order);
        given(paymentOrderRepository.findById(order.getId())).willReturn(Optional.of(order));
        given(paymentOrderRepository.findCommandLockProjectionByOrderId(order.getOrderId()))
                .willReturn(Optional.of(projection));
        given(billingAgreementRepository.findByIDForUpdate(order.getBillingAgreement().getId()))
                .willReturn(Optional.of(order.getBillingAgreement()));
        given(userSubscriptionRepository.findByIdForUpdate(order.getUserSubscription().getId()))
                .willReturn(Optional.of(order.getUserSubscription()));
        given(paymentOrderRepository.findByOrderIdForUpdate(order.getOrderId()))
                .willReturn(Optional.of(order));

        assertThat(service.claimProviderLookup(order.getId(), staleBefore))
                .get()
                .extracting(ProviderLookupClaim::mutationEligible)
                .isEqualTo(true);

        given(paymentOrderRepository.findByOrderIdForUpdate(order.getOrderId()))
                .willReturn(Optional.empty());
        assertThat(service.claimProviderLookup(order.getId(), staleBefore)).isEmpty();
    }

    @Test
    @DisplayName("exact provider success either returns retained success or delegates a pending transition")
    void applyExactProviderSuccess_respectsLocalStatusFence() {
        LocalDateTime staleBefore = LocalDateTime.of(2026, 7, 16, 0, 0);
        ProviderLookupClaim retained = claim(PaymentOrderStatus.PROVIDER_SUCCEEDED, "tx-exact");
        ProviderPaymentLookupResult exact = ProviderPaymentLookupResult.found(
                retained.provider(), retained.orderID(), "tx-exact", "DONE",
                retained.amount(), retained.currency(), "{}");

        PaymentCommandTransactionService.ReconciliationFinalizationTarget target =
                service.applyExactProviderSuccess(retained, exact, staleBefore);

        assertThat(target.purpose()).isEqualTo(PaymentPurpose.RENEWAL);
        verify(paymentCommandTransactionService, never())
                .recordProviderSuccessFromReconciliation(any(), any(), any(), any(), any());

        ProviderLookupClaim pending = claim(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        ProviderPaymentLookupResult pendingExact = ProviderPaymentLookupResult.found(
                pending.provider(), pending.orderID(), "tx-new", "DONE",
                pending.amount(), pending.currency(), "{}");
        PaymentCommandTransactionService.ReconciliationFinalizationTarget delegated =
                new PaymentCommandTransactionService.ReconciliationFinalizationTarget(
                        pending.purpose(), pending.userID(), pending.billingAgreementID(), pending.orderID());
        given(paymentCommandTransactionService.recordProviderSuccessFromReconciliation(
                pending.billingAgreementID(), pending.orderID(), "tx-new", "{}", staleBefore))
                .willReturn(delegated);

        assertThat(service.applyExactProviderSuccess(pending, pendingExact, staleBefore)).isSameAs(delegated);
    }

    @Test
    @DisplayName("provider success application refuses mismatched evidence and non-reconcilable local states")
    void applyExactProviderSuccess_rejectsUnsafeInputs() {
        LocalDateTime staleBefore = LocalDateTime.of(2026, 7, 16, 0, 0);
        ProviderLookupClaim pending = claim(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        ProviderPaymentLookupResult missing = ProviderPaymentLookupResult.notFound(
                pending.provider(), pending.orderID(), "NOT_FOUND", "missing");
        ProviderLookupClaim ready = new ProviderLookupClaim(
                pending.paymentOrderID(), pending.userID(), pending.billingAgreementID(),
                pending.userSubscriptionID(), pending.orderID(), pending.commandKey(), pending.provider(),
                pending.purpose(), PaymentOrderStatus.READY, pending.amount(), pending.currency(), null,
                true, null);
        ProviderPaymentLookupResult exact = ProviderPaymentLookupResult.found(
                ready.provider(), ready.orderID(), "tx-exact", "DONE",
                ready.amount(), ready.currency(), "{}");

        assertThatThrownBy(() -> service.applyExactProviderSuccess(pending, missing, staleBefore))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Exact provider DONE evidence");
        assertThatThrownBy(() -> service.applyExactProviderSuccess(ready, exact, staleBefore))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not eligible");
    }

    @Test
    @DisplayName("active agreement reconciliation reports only agreements without an active subscription")
    void activeAgreementBatch_reportsMissingSubscription() {
        BillingAgreement missing = agreement(BillingAgreementStatus.ACTIVE, LocalDate.of(2026, 8, 1));
        BillingAgreement healthy = agreement(BillingAgreementStatus.ACTIVE, LocalDate.of(2026, 8, 1));
        ReflectionTestUtils.setField(missing, "id", 71L);
        ReflectionTestUtils.setField(healthy, "id", 72L);
        LocalDate today = LocalDate.of(2026, 7, 16);
        given(billingAgreementRepository.findLocalReconciliationCandidates(
                eq(BillingAgreementStatus.ACTIVE), eq(0L), any()))
                .willReturn(List.of(missing, healthy));
        given(userSubscriptionRepository.findActiveByUser(missing.getUser(), today))
                .willReturn(Optional.empty());
        given(userSubscriptionRepository.findActiveByUser(healthy.getUser(), today))
                .willReturn(Optional.of(mock(UserSubscription.class)));

        PaymentReconciliationTransactionService.LocalReconciliationBatch batch =
                service.reconcileActiveAgreementBatch(0L, 2, today);

        assertThat(batch.lastSeenID()).isEqualTo(72L);
        assertThat(batch.exhausted()).isFalse();
        assertThat(batch.issues()).singleElement()
                .extracting(PaymentReconciliationService.LocalReconciliationIssue::billingAgreementId)
                .isEqualTo(71L);
    }

    private void assertEligibleLookup(PaymentOrder order, LocalDateTime staleBefore) {
        stubReadOnlyRelationship(order, projection(order));

        ProviderLookupClaim claim = service.loadProviderLookup(order.getId(), staleBefore).orElseThrow();

        assertThat(claim.mutationEligible()).isTrue();
        assertThat(claim.localEligibilityFailure()).isNull();
    }

    private void stubReadOnlyRelationship(PaymentOrder order, CommandLockProjection projection) {
        given(paymentOrderRepository.findById(order.getId())).willReturn(Optional.of(order));
        given(paymentOrderRepository.findCommandLockProjectionByOrderId(order.getOrderId()))
                .willReturn(Optional.of(projection));
        given(billingAgreementRepository.findById(order.getBillingAgreement().getId()))
                .willReturn(Optional.of(order.getBillingAgreement()));
        if (order.getUserSubscription() != null) {
            given(userSubscriptionRepository.findById(order.getUserSubscription().getId()))
                    .willReturn(Optional.of(order.getUserSubscription()));
        }
    }

    private CommandLockProjection projection(PaymentOrder order) {
        CommandLockProjection projection = mock(CommandLockProjection.class);
        given(projection.getBillingAgreementID()).willReturn(order.getBillingAgreement().getId());
        given(projection.getUserSubscriptionID())
                .willReturn(order.getUserSubscription() == null ? null : order.getUserSubscription().getId());
        given(projection.getUserID()).willReturn(order.getUser().getId());
        given(projection.getPurpose()).willReturn(order.getPurpose());
        return projection;
    }

    private PaymentOrder order(PaymentPurpose purpose, PaymentOrderStatus status, boolean withSubscription) {
        User user = User.builder().email("reconcile@example.com").nickname("reconcile").build();
        ReflectionTestUtils.setField(user, "id", 10L);
        LocalDate nextBillingAt = LocalDate.of(2026, 8, 1);
        BillingAgreement agreement = agreement(
                purpose == PaymentPurpose.SUBSCRIBE || purpose == PaymentPurpose.BILLING_AGREEMENT
                        ? BillingAgreementStatus.READY
                        : BillingAgreementStatus.ACTIVE,
                nextBillingAt);
        ReflectionTestUtils.setField(agreement, "user", user);
        UserSubscription userSubscription = withSubscription
                ? UserSubscription.builder()
                        .user(user)
                        .subscription(mock(Subscription.class))
                        .billingCycle(BillingCycle.MONTHLY)
                        .startedAt(LocalDate.of(2026, 7, 1))
                        .expiresAt(nextBillingAt)
                        .build()
                : null;
        if (userSubscription != null) {
            ReflectionTestUtils.setField(userSubscription, "id", 30L);
        }
        PaymentOrder order = PaymentOrder.builder()
                .orderId("ORDER-" + purpose + "-" + status)
                .commandKey("COMMAND-" + purpose)
                .user(user)
                .purpose(purpose)
                .provider(PaymentProviderType.TOSS)
                .status(status)
                .subscription(mock(Subscription.class))
                .userSubscription(userSubscription)
                .billingAgreement(agreement)
                .billingCycle(BillingCycle.MONTHLY)
                .upgradeTargetBillingCycle(purpose == PaymentPurpose.UPGRADE ? BillingCycle.YEARLY : null)
                .billingPeriodStart(purpose == PaymentPurpose.RENEWAL ? nextBillingAt : null)
                .amount(BigDecimal.valueOf(9_900))
                .currency("KRW")
                .expiresAt(LocalDateTime.of(2026, 8, 1, 0, 0))
                .build();
        ReflectionTestUtils.setField(order, "id", (long) (40 + purpose.ordinal() * 10 + status.ordinal()));
        return order;
    }

    private BillingAgreement agreement(BillingAgreementStatus status, LocalDate nextBillingAt) {
        User user = User.builder().email("agreement@example.com").nickname("agreement").build();
        ReflectionTestUtils.setField(user, "id", 10L);
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .status(status)
                .providerCustomerKey("customer-key")
                .billingKeyCiphertext("v2:key:nonce:ciphertext")
                .billingKeyFingerprint("fingerprint")
                .nextBillingAt(nextBillingAt)
                .build();
        ReflectionTestUtils.setField(agreement, "id", 20L);
        return agreement;
    }

    private ProviderLookupClaim claim(
            PaymentOrderStatus status,
            String providerTransactionID) {
        return new ProviderLookupClaim(
                1L,
                2L,
                3L,
                4L,
                "ORDER-1",
                "RENEWAL:3:4:2026-08-17",
                PaymentProviderType.TOSS,
                PaymentPurpose.RENEWAL,
                status,
                BigDecimal.valueOf(9900),
                "KRW",
                providerTransactionID,
                true,
                null);
    }

    private PaymentOrder paymentOrder(Long id) {
        User user = mock(User.class);
        given(user.getId()).willReturn(7L);
        PaymentOrder order = mock(PaymentOrder.class);
        given(order.getId()).willReturn(id);
        given(order.getUser()).willReturn(user);
        given(order.getBillingAgreement()).willReturn(null);
        given(order.getOrderId()).willReturn("ORDER-" + id);
        given(order.getProvider()).willReturn(PaymentProviderType.TOSS);
        given(order.getPurpose()).willReturn(PaymentPurpose.RENEWAL);
        given(order.getStatus()).willReturn(PaymentOrderStatus.DONE);
        given(order.getAmount()).willReturn(BigDecimal.valueOf(9900));
        return order;
    }
}
