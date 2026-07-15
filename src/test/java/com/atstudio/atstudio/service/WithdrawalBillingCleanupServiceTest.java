package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawalBillingCleanupService unit tests")
class WithdrawalBillingCleanupServiceTest {

    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock BillingAgreementCleanupTransactionService cleanupTransactionService;
    @Mock BillingAgreementCleanupProviderExecutor cleanupProviderExecutor;

    WithdrawalBillingCleanupService service;

    @BeforeEach
    void setUp() {
        service = new WithdrawalBillingCleanupService(
                billingAgreementRepository,
                cleanupTransactionService,
                cleanupProviderExecutor);
    }

    @Test
    @DisplayName("cleanup uses a NEVER boundary and records success after the provider call")
    void cleanup_successUsesClaimProviderResultOrder() throws Exception {
        Method cleanupMethod = WithdrawalBillingCleanupService.class.getMethod("cleanup", Long.class);
        Transactional transactional = cleanupMethod.getAnnotation(Transactional.class);
        assertThat(transactional.propagation()).isEqualTo(Propagation.NEVER);

        LocalDateTime leaseStartedAt = LocalDateTime.now().withNano(0);
        BillingAgreementCleanupTransactionService.WithdrawalCleanupClaim claim =
                BillingAgreementCleanupTransactionService.WithdrawalCleanupClaim.callProvider(
                        11L,
                        PaymentProviderType.TOSS_BILLING,
                        "encrypted-key",
                        leaseStartedAt);
        BillingAgreementCleanupProviderExecutor.CleanupProviderResult providerResult =
                BillingAgreementCleanupProviderExecutor.CleanupProviderResult.succeeded();
        given(cleanupTransactionService.claimWithdrawalCleanup(eq(11L), any(LocalDateTime.class)))
                .willReturn(claim);
        given(cleanupProviderExecutor.deleteBillingKey(
                PaymentProviderType.TOSS_BILLING,
                "encrypted-key")).willReturn(providerResult);

        WithdrawalBillingCleanupService.CleanupOutcome outcome = service.cleanup(11L);

        assertThat(outcome).isEqualTo(WithdrawalBillingCleanupService.CleanupOutcome.SUCCEEDED);
        var ordering = inOrder(cleanupTransactionService, cleanupProviderExecutor);
        ordering.verify(cleanupTransactionService)
                .claimWithdrawalCleanup(eq(11L), any(LocalDateTime.class));
        ordering.verify(cleanupProviderExecutor)
                .deleteBillingKey(PaymentProviderType.TOSS_BILLING, "encrypted-key");
        ordering.verify(cleanupTransactionService)
                .recordWithdrawalCleanupResult(claim, providerResult);
    }

    @Test
    @DisplayName("unknown provider outcome remains pending after the fenced result phase")
    void cleanup_unknownProviderOutcomeRemainsPending() {
        BillingAgreementCleanupTransactionService.WithdrawalCleanupClaim claim =
                BillingAgreementCleanupTransactionService.WithdrawalCleanupClaim.callProvider(
                        12L,
                        PaymentProviderType.TOSS_BILLING,
                        "encrypted-key",
                        LocalDateTime.now().withNano(0));
        BillingAgreementCleanupProviderExecutor.CleanupProviderResult providerResult =
                BillingAgreementCleanupProviderExecutor.CleanupProviderResult.pending(
                        "BILLING_KEY_DELETE_EXCEPTION",
                        "PaymentProviderOutcomeUnknownException");
        given(cleanupTransactionService.claimWithdrawalCleanup(eq(12L), any(LocalDateTime.class)))
                .willReturn(claim);
        given(cleanupProviderExecutor.deleteBillingKey(any(), any())).willReturn(providerResult);

        WithdrawalBillingCleanupService.CleanupOutcome outcome = service.cleanup(12L);

        assertThat(outcome)
                .isEqualTo(WithdrawalBillingCleanupService.CleanupOutcome.PENDING_PROVIDER_CONFIRMATION);
        verify(cleanupTransactionService).recordWithdrawalCleanupResult(claim, providerResult);
    }

    @Test
    @DisplayName("fresh competing cleanup returns the exact in-progress outcome without provider work")
    void cleanup_freshCompetitionReturnsInProgress() {
        given(cleanupTransactionService.claimWithdrawalCleanup(eq(13L), any(LocalDateTime.class)))
                .willReturn(BillingAgreementCleanupTransactionService.WithdrawalCleanupClaim
                        .withoutProvider(
                                BillingAgreementCleanupTransactionService.CleanupAction.IN_PROGRESS,
                                13L));

        WithdrawalBillingCleanupService.CleanupOutcome outcome = service.cleanup(13L);

        assertThat(outcome).isEqualTo(WithdrawalBillingCleanupService.CleanupOutcome.IN_PROGRESS);
        verify(cleanupProviderExecutor, org.mockito.Mockito.never()).deleteBillingKey(any(), any());
    }

    @Test
    @DisplayName("retry candidates use the bounded B-owned projection")
    void findRetryCandidateIDs_usesBoundedProjection() {
        given(billingAgreementRepository.findWithdrawalCleanupCandidateIDs(
                eq(0L),
                any(Pageable.class))).willReturn(List.of(21L, 22L));

        assertThat(service.findRetryCandidateIDs()).containsExactly(21L, 22L);

        verify(billingAgreementRepository).findWithdrawalCleanupCandidateIDs(
                eq(0L),
                any(Pageable.class));
    }

    @Test
    @DisplayName("bounded stale candidates become detect-only pending claims")
    void detectStaleCleanupClaims_marksOnlyStillStaleRows() {
        given(billingAgreementRepository.findStaleBillingKeyCleanupCandidateIDs(
                any(LocalDateTime.class),
                eq(0L),
                any(Pageable.class))).willReturn(List.of(31L, 32L));
        given(cleanupTransactionService.markStaleCleanupPending(eq(31L), any(LocalDateTime.class)))
                .willReturn(true);
        given(cleanupTransactionService.markStaleCleanupPending(eq(32L), any(LocalDateTime.class)))
                .willReturn(false);

        assertThat(service.detectStaleCleanupClaims()).isEqualTo(1);

        verify(cleanupTransactionService).markStaleCleanupPending(eq(31L), any(LocalDateTime.class));
        verify(cleanupTransactionService).markStaleCleanupPending(eq(32L), any(LocalDateTime.class));
    }
}
