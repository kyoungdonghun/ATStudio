package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementIgnoreRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementReconcileRequest;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentSettlementImportAttemptState;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;
import com.atstudio.atstudio.entity.enums.PaymentSettlementSource;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.PaymentSettlementRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPaymentSettlementService unit tests")
class AdminPaymentSettlementServiceTest {

    @Mock PaymentSettlementRepository paymentSettlementRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock PaymentRefundRepository paymentRefundRepository;
    @Mock UserRepository userRepository;
    @Mock PaymentOperationAuditLogService auditLogService;
    @Mock AdminPaymentSettlementAttemptTransactionService attemptTransactionService;

    AdminPaymentSettlementService service;
    AdminPaymentSettlementRowTransactionService rowTransactionService;

    private static final String IDEMPOTENCY_KEY = "11111111-1111-4111-8111-111111111111";

    @BeforeEach
    void setUp() {
        rowTransactionService = new AdminPaymentSettlementRowTransactionService(
                paymentSettlementRepository,
                paymentOrderRepository,
                subscriptionPaymentRepository,
                paymentRefundRepository,
                auditLogService);
        service = new AdminPaymentSettlementService(
                paymentSettlementRepository,
                subscriptionPaymentRepository,
                userRepository,
                auditLogService,
                attemptTransactionService,
                rowTransactionService,
                new PaymentCommandKeyFactory());
        org.mockito.Mockito.lenient()
                .when(attemptTransactionService.create(any(), any(), any()))
                .thenReturn(new AdminPaymentSettlementAttemptTransactionService.CreatedAttempt(
                        1L,
                        "ATS-SETTLE-ATTEMPT-1"));
    }

    @Test
    @DisplayName("importSettlements stores matched settlement rows")
    void importSettlementsMatched() {
        Fixture fixture = fixture();
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(fixture.order()));
        given(subscriptionPaymentRepository.findByPaymentOrder(fixture.order()))
                .willReturn(Optional.of(fixture.payment()));
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.ZERO);
        given(paymentSettlementRepository.saveAndFlush(any(PaymentSettlement.class)))
                .willAnswer(invocation -> {
                    PaymentSettlement settlement = invocation.getArgument(0);
                    ReflectionTestUtils.setField(settlement, "id", 1L);
                    return settlement;
                });

        AdminPaymentSettlementImportResponse result = importSettlements(
                csv("""
                        provider,provider_payment_key,order_id,gross_amount,refund_amount,fee_amount,vat_amount,net_settlement_amount,settlement_base_date
                        TOSS,payment_key,ORDER-1,9900,0,300,30,9570,2026-05-26
                        """),
                "import note").getData();

        assertThat(result.importedRows()).isEqualTo(1);
        assertThat(result.failedRows()).isZero();
        assertThat(result.statusCounts()).containsEntry("MATCHED", 1);

        ArgumentCaptor<PaymentSettlement> captor = ArgumentCaptor.forClass(PaymentSettlement.class);
        verify(paymentSettlementRepository).saveAndFlush(captor.capture());
        PaymentSettlement settlement = captor.getValue();
        assertThat(settlement.getStatus()).isEqualTo(PaymentSettlementStatus.MATCHED);
        assertThat(settlement.getPaymentOrder()).isEqualTo(fixture.order());
        assertThat(settlement.getSubscriptionPayment()).isEqualTo(fixture.payment());
        assertThat(settlement.getNetSettlementAmount()).isEqualByComparingTo("9570");
        verify(auditLogService).recordPaymentSettlementEvent(
                any(),
                any(PaymentSettlement.class),
                org.mockito.ArgumentMatchers.eq(
                        com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IMPORTED),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(PaymentSettlementStatus.MATCHED),
                org.mockito.ArgumentMatchers.eq("Settlement row imported."));
    }

    @Test
    @DisplayName("importSettlements marks amount mismatch")
    void importSettlementsMismatch() {
        Fixture fixture = fixture();
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(fixture.order()));
        given(subscriptionPaymentRepository.findByPaymentOrder(fixture.order()))
                .willReturn(Optional.of(fixture.payment()));
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.ZERO);
        given(paymentSettlementRepository.saveAndFlush(any(PaymentSettlement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AdminPaymentSettlementImportResponse result = importSettlements(
                csv("""
                        provider,provider_payment_key,order_id,gross_amount,refund_amount,fee_amount,vat_amount,net_settlement_amount,settlement_base_date
                        TOSS,payment_key,ORDER-1,9900,100,300,30,9470,2026-05-26
                        """),
                null).getData();

        assertThat(result.statusCounts()).containsEntry("MISMATCHED", 1);
        ArgumentCaptor<PaymentSettlement> captor = ArgumentCaptor.forClass(PaymentSettlement.class);
        verify(paymentSettlementRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentSettlementStatus.MISMATCHED);
        assertThat(captor.getValue().getMismatchReason()).contains("refund_amount");
    }

    @Test
    @DisplayName("importSettlements skips duplicate rows by deduplication key")
    void importSettlementsSkipsDuplicate() {
        PaymentSettlement winner = settlementForIgnore("winner");
        given(paymentSettlementRepository.saveAndFlush(any(PaymentSettlement.class)))
                .willThrow(namedDeduplicationViolation());
        given(paymentSettlementRepository.findByDeduplicationKey(any())).willReturn(Optional.of(winner));

        AdminPaymentSettlementImportResponse result = importSettlements(
                csv("""
                        provider,provider_payment_key,order_id,gross_amount,net_settlement_amount,settlement_base_date
                        TOSS,payment_key,ORDER-1,9900,9900,2026-05-26
                        """),
                null).getData();

        assertThat(result.importedRows()).isZero();
        assertThat(result.skippedDuplicateRows()).isEqualTo(1);
        verify(paymentSettlementRepository).saveAndFlush(any());
        verify(paymentSettlementRepository).findByDeduplicationKey(any());
    }

    @Test
    @DisplayName("importSettlements fails the durable attempt for an unrelated integrity violation")
    void importSettlementsFailsAttemptForUnrelatedIntegrityViolation() {
        given(paymentSettlementRepository.saveAndFlush(any(PaymentSettlement.class)))
                .willThrow(unrelatedIntegrityViolation());

        assertThatThrownBy(() -> importSettlements(
                csv("""
                        provider,provider_payment_key,order_id,gross_amount,net_settlement_amount,settlement_base_date
                        TOSS,payment_key,ORDER-1,9900,9900,2026-05-26
                        """),
                null))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.SETTLEMENT_IMPORT_ORCHESTRATION_FAILED));

        verify(attemptTransactionService).fail(1L, "ROW_PERSISTENCE_FAILED");
        verify(attemptTransactionService, never()).complete(any(), any(Integer.class), any(Integer.class),
                any(Integer.class), any(Integer.class));
        verify(paymentSettlementRepository, never()).findByDeduplicationKey(any());
    }

    @Test
    @DisplayName("claimAttempt resolves an exact attempt key-digest constraint as a same-key conflict")
    void claimAttemptResolvesExactAttemptKeyDigestConstraint() {
        String expectedDigest = new PaymentCommandKeyFactory()
                .settlementImportDigest(99L, IDEMPOTENCY_KEY);
        given(attemptTransactionService.create(99L, expectedDigest, "claim note"))
                .willThrow(namedAttemptKeyDigestViolation());
        given(attemptTransactionService.findStateByDigest(expectedDigest))
                .willReturn(Optional.of(new AdminPaymentSettlementAttemptTransactionService.AttemptState(
                        7L,
                        PaymentSettlementImportAttemptState.COMPLETED)));

        assertThatThrownBy(() -> service.importSettlements(
                actor(),
                csv("not parsed after claim conflict"),
                "claim note",
                IDEMPOTENCY_KEY))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.SETTLEMENT_IMPORT_ATTEMPT_COMPLETED));

        verify(attemptTransactionService).findStateByDigest(expectedDigest);
        verifyNoInteractions(
                paymentSettlementRepository,
                paymentOrderRepository,
                subscriptionPaymentRepository,
                paymentRefundRepository,
                auditLogService);
    }

    @Test
    @DisplayName("claimAttempt fails unrelated integrity violations without digest-state lookup")
    void claimAttemptRejectsUnrelatedIntegrityViolationWithoutDigestLookup() {
        given(attemptTransactionService.create(any(), any(), any()))
                .willThrow(unrelatedAttemptIntegrityViolation());
        org.mockito.Mockito.lenient()
                .when(attemptTransactionService.findStateByDigest(any()))
                .thenReturn(Optional.of(new AdminPaymentSettlementAttemptTransactionService.AttemptState(
                        8L,
                        PaymentSettlementImportAttemptState.COMPLETED)));

        assertThatThrownBy(() -> service.importSettlements(
                actor(),
                csv("not parsed after claim failure"),
                "claim note",
                IDEMPOTENCY_KEY))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.SETTLEMENT_IMPORT_ORCHESTRATION_FAILED));

        verify(attemptTransactionService, never()).findStateByDigest(any());
        verify(attemptTransactionService, never()).fail(any(), any());
        verifyNoInteractions(
                paymentSettlementRepository,
                paymentOrderRepository,
                subscriptionPaymentRepository,
                paymentRefundRepository,
                auditLogService);
    }

    @Test
    @DisplayName("attempt key-digest constraint translation recognizes safe MySQL and H2 signatures")
    void translatesAttemptKeyDigestConstraintFromSafeDriverSignatures() {
        assertThat(PaymentSettlementConstraintTranslator.isAttemptKeyDigestUniqueViolation(
                mysqlAttemptKeyDigestViolation(1062))).isTrue();
        assertThat(PaymentSettlementConstraintTranslator.isAttemptKeyDigestUniqueViolation(
                h2AttemptKeyDigestViolation("23505"))).isTrue();
        assertThat(PaymentSettlementConstraintTranslator.isAttemptKeyDigestUniqueViolation(
                mysqlAttemptKeyDigestViolation(1452))).isFalse();
        assertThat(PaymentSettlementConstraintTranslator.isAttemptKeyDigestUniqueViolation(
                h2AttemptKeyDigestViolation("23513"))).isFalse();
    }

    @Test
    @DisplayName("CSV import and recovery keep payment-domain access read-only and write only settlement evidence")
    void importAndRecoveryHaveNoPaymentDomainMutationOrExternalEffectInvocation() {
        given(paymentSettlementRepository.saveAndFlush(any(PaymentSettlement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        service.importSettlements(
                actor(),
                csv("""
                        provider,provider_payment_key,order_id,gross_amount,net_settlement_amount,settlement_base_date
                        TOSS,effect-free-key,ORDER-EFFECT-FREE,9900,9900,2026-05-26
                        """),
                null,
                IDEMPOTENCY_KEY);
        service.recoverImportAttempt(actor(), IDEMPOTENCY_KEY);

        verify(paymentOrderRepository).findByOrderId("ORDER-EFFECT-FREE");
        verify(subscriptionPaymentRepository).findFirstByPgTransactionId("effect-free-key");
        verifyNoMoreInteractions(paymentOrderRepository, subscriptionPaymentRepository);
        verifyNoInteractions(paymentRefundRepository, userRepository);
        verify(paymentSettlementRepository).saveAndFlush(any(PaymentSettlement.class));
        verifyNoMoreInteractions(paymentSettlementRepository);
        verify(auditLogService).recordPaymentSettlementEvent(
                any(),
                any(PaymentSettlement.class),
                eq(PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IMPORTED),
                org.mockito.ArgumentMatchers.isNull(),
                eq(PaymentSettlementStatus.LOCAL_PAYMENT_NOT_FOUND),
                eq("Settlement row imported."));
        verifyNoMoreInteractions(auditLogService);

        ArgumentCaptor<String> digestCaptor = ArgumentCaptor.forClass(String.class);
        verify(attemptTransactionService).create(eq(99L), digestCaptor.capture(),
                org.mockito.ArgumentMatchers.isNull());
        verify(attemptTransactionService).complete(1L, 1, 1, 0, 0);
        verify(attemptTransactionService).recover(digestCaptor.capture());
        assertThat(digestCaptor.getAllValues())
                .containsExactlyElementsOf(List.of(
                        new PaymentCommandKeyFactory().settlementImportDigest(99L, IDEMPOTENCY_KEY),
                        new PaymentCommandKeyFactory().settlementImportDigest(99L, IDEMPOTENCY_KEY)))
                .allMatch(digest -> digest.matches("[0-9a-f]{64}"))
                .noneMatch(digest -> digest.contains(IDEMPOTENCY_KEY));
        verifyNoMoreInteractions(attemptTransactionService);
    }

    @Test
    @DisplayName("settlement import boundary has no provider, billing, receipt, subscription-command, or mail dependency")
    void settlementImportBoundaryHasNoExternalEffectCollaborators() {
        assertThat(directDependencyTypeNames(
                AdminPaymentSettlementService.class,
                AdminPaymentSettlementRowTransactionService.class,
                AdminPaymentSettlementAttemptTransactionService.class))
                .doesNotContain(
                        PaymentCommandTransactionService.class.getName(),
                        AdminPaymentRefundService.class.getName(),
                        PaymentRefundTransactionService.class.getName(),
                        SubscriptionUpgradePaymentExecutor.class.getName(),
                        RecurringRenewalService.class.getName(),
                        UserSubscriptionService.class.getName(),
                        BillingAgreementApplicationService.class.getName(),
                        BillingAgreementPrepareTransactionService.class.getName(),
                        BillingAgreementCleanupProviderExecutor.class.getName(),
                        PaymentReceiptEvidenceService.class.getName(),
                        PaymentReconciliationService.class.getName(),
                        PaymentReconciliationIncidentService.class.getName(),
                        EmailService.class.getName())
                .noneMatch(typeName -> typeName.startsWith(
                        "com.atstudio.atstudio.service.payment.provider."));
    }

    @Test
    @DisplayName("reconcileMissingProviderSettlements does not create a missing-provider row when provider evidence exists")
    void reconcileMissingProviderSettlementsSkipsExistingProviderEvidence() {
        Fixture fixture = fixture();
        given(subscriptionPaymentRepository.findByPaymentStatusAndCreatedAtBetween(
                eq(PaymentStatus.DONE),
                any(),
                any()))
                .willReturn(List.of(fixture.payment()));
        given(subscriptionPaymentRepository.findWithGraphById(30L)).willReturn(Optional.of(fixture.payment()));
        given(paymentSettlementRepository.existsByOrderIdAndSourceNot(
                "ORDER-1",
                PaymentSettlementSource.SYSTEM_RECONCILIATION))
                .willReturn(true);

        AdminPaymentSettlementImportResponse result = service.reconcileMissingProviderSettlements(
                actor(),
                new AdminPaymentSettlementReconcileRequest(
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 31))).getData();

        assertThat(result.importedRows()).isZero();
        assertThat(result.skippedDuplicateRows()).isEqualTo(1);
        verify(paymentSettlementRepository, never()).existsByDeduplicationKey(any());
        verify(paymentSettlementRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("importSettlements rejects missing, empty, unreadable, and malformed CSV files")
    void importSettlementsRejectsInvalidFiles() throws IOException {
        assertThatThrownBy(() -> importSettlements(null, null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> importSettlements(
                new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]),
                null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> importSettlements(
                csv("provider,order_id\n"),
                null))
                .isInstanceOf(BusinessException.class);

        MultipartFile unreadable = mock(MultipartFile.class);
        given(unreadable.isEmpty()).willReturn(false);
        given(unreadable.getInputStream()).willThrow(new IOException("unreadable"));

        assertThatThrownBy(() -> importSettlements(unreadable, null))
                .isInstanceOf(BusinessException.class)
                .hasCauseInstanceOf(IOException.class);

        MultipartFile missingHeader = mock(MultipartFile.class);
        given(missingHeader.isEmpty()).willReturn(false);
        given(missingHeader.getInputStream()).willReturn(new ByteArrayInputStream(new byte[0]));
        assertThatThrownBy(() -> importSettlements(missingHeader, null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("importSettlements reports each invalid provider row without persisting partial evidence")
    void importSettlementsReportsInvalidRows() {
        String longOrderId = "O".repeat(65);
        AdminPaymentSettlementImportResponse result = importSettlements(
                csv("""
                        provider,order_id,gross_amount,refund_amount,fee_amount,vat_amount,net_settlement_amount,settlement_base_date,currency
                        UNKNOWN,ORDER-1,9900,0,0,0,9900,2026-05-26,KRW
                        TOSS,%s,9900,0,0,0,9900,2026-05-26,KRW
                        TOSS,ORDER-3,-1,0,0,0,0,2026-05-26,KRW
                        TOSS,ORDER-4,not-a-number,0,0,0,0,2026-05-26,KRW
                        TOSS,ORDER-5,9900,0,0,0,9900,26-05-2026,KRW
                        TOSS,ORDER-6,9900,0,0,0,9900,2026-05-26,WONN
                        TOSS,,9900,0,0,0,9900,2026-05-26,KRW
                        """.formatted(longOrderId)),
                null).getData();

        assertThat(result.importedRows()).isZero();
        assertThat(result.failedRows()).isEqualTo(7);
        assertThat(result.errors())
                .extracting(error -> error.message())
                .containsExactly(
                        "provider is invalid.",
                        "order_id must be at most 64 characters.",
                        "gross_amount cannot be negative.",
                        "gross_amount must be numeric.",
                        "settlement_base_date must be yyyy-MM-dd.",
                        "currency must be a 3-letter ISO code.",
                        "order_id is required.");
        verify(paymentSettlementRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("importSettlements persists the valid row and returns the mixed row error once")
    void importSettlementsReturnsMixedResult() {
        Fixture fixture = fixture();
        given(paymentOrderRepository.findByOrderId("ORDER-1")).willReturn(Optional.of(fixture.order()));
        given(subscriptionPaymentRepository.findByPaymentOrder(fixture.order()))
                .willReturn(Optional.of(fixture.payment()));
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.ZERO);
        given(paymentSettlementRepository.saveAndFlush(any(PaymentSettlement.class)))
                .willAnswer(invocation -> {
                    PaymentSettlement settlement = invocation.getArgument(0);
                    ReflectionTestUtils.setField(settlement, "id", 81L);
                    return settlement;
                });

        AdminPaymentSettlementImportResponse result = importSettlements(
                csv("""
                        provider,provider_payment_key,order_id,gross_amount,refund_amount,fee_amount,vat_amount,net_settlement_amount,settlement_base_date
                        TOSS,payment_key,ORDER-1,9900,0,300,30,9570,2026-05-26
                        UNKNOWN,payment_key_2,ORDER-2,9900,0,300,30,9570,2026-05-26
                        """),
                "safe synthetic mixed import").getData();

        assertThat(result.importedRows()).isEqualTo(1);
        assertThat(result.failedRows()).isEqualTo(1);
        assertThat(result.errors()).singleElement()
                .satisfies(error -> {
                    assertThat(error.rowNumber()).isEqualTo(3);
                    assertThat(error.message()).isNotBlank();
                });
        verify(paymentSettlementRepository, times(1)).saveAndFlush(any(PaymentSettlement.class));
        verify(auditLogService, times(1)).recordPaymentSettlementEvent(
                any(),
                any(PaymentSettlement.class),
                eq(PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IMPORTED),
                org.mockito.ArgumentMatchers.isNull(),
                eq(PaymentSettlementStatus.MATCHED),
                eq("Settlement row imported."));
    }

    @Test
    @DisplayName("importSettlements parses BOM, quoted commas, escaped quotes, payout dates, and optional evidence")
    void importSettlementsParsesQuotedEvidence() {
        given(paymentSettlementRepository.saveAndFlush(any(PaymentSettlement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "s".repeat(260) + ".csv",
                "text/csv",
                ("\uFEFFprovider,provider_payment_key,provider_settlement_id,order_id,gross_amount,net_settlement_amount,"
                        + "settlement_base_date,settlement_payout_date,provider_status,currency,note\n"
                        + "TOSS,,SETTLEMENT-1,\"ORDER,WITH,COMMA\",9900,9900,2026-05-26,2026-05-27,DONE,krw,"
                        + "\"operator \"\"quoted\"\", note\"\n\n")
                        .getBytes(StandardCharsets.UTF_8));

        AdminPaymentSettlementImportResponse result = importSettlements(file, null).getData();

        assertThat(result.importedRows()).isEqualTo(1);
        ArgumentCaptor<PaymentSettlement> captor = ArgumentCaptor.forClass(PaymentSettlement.class);
        verify(paymentSettlementRepository).saveAndFlush(captor.capture());
        PaymentSettlement settlement = captor.getValue();
        assertThat(settlement.getStatus()).isEqualTo(PaymentSettlementStatus.LOCAL_PAYMENT_NOT_FOUND);
        assertThat(settlement.getOrderId()).isEqualTo("ORDER,WITH,COMMA");
        assertThat(settlement.getSettlementPayoutDate()).isEqualTo(LocalDate.of(2026, 5, 27));
        assertThat(settlement.getCurrency()).isEqualTo("KRW");
        assertThat(settlement.getOperatorNote()).isEqualTo("operator \"quoted\", note");
        assertThat(settlement.getSourceFileName()).hasSize(255);
        assertThat(settlement.getSourcePayload()).isNull();
    }

    @Test
    @DisplayName("importSettlements reconciles by provider payment key when order lookup is unavailable")
    void importSettlementsFallsBackToProviderPaymentKey() {
        Fixture fixture = fixture();
        given(paymentOrderRepository.findByOrderId("ORDER-ALIAS")).willReturn(Optional.empty());
        given(subscriptionPaymentRepository.findFirstByPgTransactionId("payment_key"))
                .willReturn(Optional.of(fixture.payment()));
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.ZERO);
        given(paymentSettlementRepository.saveAndFlush(any(PaymentSettlement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AdminPaymentSettlementImportResponse result = importSettlements(
                csv("""
                        provider,provider_payment_key,order_id,gross_amount,net_settlement_amount,settlement_base_date
                        TOSS,payment_key,ORDER-ALIAS,9900,9900,2026-05-26
                        """),
                "operator note").getData();

        assertThat(result.statusCounts()).containsEntry("MATCHED", 1);
        verify(subscriptionPaymentRepository).findFirstByPgTransactionId("payment_key");
    }

    @Test
    @DisplayName("reconcileMissingProviderSettlements creates evidence and skips unusable or duplicate payments")
    void reconcileMissingProviderSettlementsCreatesAndSkipsEvidence() {
        Fixture fixture = fixture();
        SubscriptionPayment withoutOrder = SubscriptionPayment.builder()
                .id(31L)
                .amount(BigDecimal.valueOf(1000))
                .paymentStatus(PaymentStatus.DONE)
                .build();
        SubscriptionPayment duplicate = SubscriptionPayment.builder()
                .id(32L)
                .paymentOrder(fixture.order())
                .amount(BigDecimal.valueOf(9900))
                .paymentStatus(PaymentStatus.DONE)
                .build();
        given(subscriptionPaymentRepository.findByPaymentStatusAndCreatedAtBetween(
                eq(PaymentStatus.DONE), any(), any()))
                .willReturn(List.of(withoutOrder, fixture.payment(), duplicate));
        given(subscriptionPaymentRepository.findWithGraphById(30L)).willReturn(Optional.of(fixture.payment()));
        given(subscriptionPaymentRepository.findWithGraphById(32L)).willReturn(Optional.of(duplicate));
        given(paymentSettlementRepository.existsByOrderIdAndSourceNot(
                "ORDER-1", PaymentSettlementSource.SYSTEM_RECONCILIATION))
                .willReturn(false, true);
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.valueOf(1000));
        given(paymentSettlementRepository.saveAndFlush(any(PaymentSettlement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AdminPaymentSettlementImportResponse result = service.reconcileMissingProviderSettlements(
                actor(), null).getData();

        assertThat(result.totalRows()).isEqualTo(3);
        assertThat(result.importedRows()).isEqualTo(1);
        assertThat(result.skippedDuplicateRows()).isEqualTo(1);
        assertThat(result.failedRows()).isEqualTo(1);
        assertThat(result.errors()).singleElement()
                .satisfies(error -> assertThat(error.message()).contains("no payment order"));
        assertThat(result.statusCounts()).containsEntry("PROVIDER_SETTLEMENT_NOT_FOUND", 1);
        ArgumentCaptor<PaymentSettlement> captor = ArgumentCaptor.forClass(PaymentSettlement.class);
        verify(paymentSettlementRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getNetSettlementAmount()).isEqualByComparingTo("8900");
    }

    @Test
    @DisplayName("reconcileMissingProviderSettlements rejects an inverted date range")
    void reconcileMissingProviderSettlementsRejectsInvertedRange() {
        assertThatThrownBy(() -> service.reconcileMissingProviderSettlements(
                actor(),
                new AdminPaymentSettlementReconcileRequest(
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 5, 1))))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("reconcileMissingProviderSettlements supplies each omitted date boundary")
    void reconcileMissingProviderSettlementsSuppliesOmittedBoundaries() {
        given(subscriptionPaymentRepository.findByPaymentStatusAndCreatedAtBetween(
                eq(PaymentStatus.DONE), any(), any()))
                .willReturn(List.of());

        service.reconcileMissingProviderSettlements(
                actor(), new AdminPaymentSettlementReconcileRequest(null, LocalDate.now()));
        service.reconcileMissingProviderSettlements(
                actor(), new AdminPaymentSettlementReconcileRequest(LocalDate.now().minusDays(1), null));

        verify(subscriptionPaymentRepository, org.mockito.Mockito.times(2))
                .findByPaymentStatusAndCreatedAtBetween(eq(PaymentStatus.DONE), any(), any());
    }

    @Test
    @DisplayName("reconcileMissingProviderSettlements falls back to the order provider")
    void reconcileMissingProviderSettlementsFallsBackToOrderProvider() {
        Fixture fixture = fixture();
        ReflectionTestUtils.setField(fixture.payment(), "provider", null);
        given(subscriptionPaymentRepository.findByPaymentStatusAndCreatedAtBetween(
                eq(PaymentStatus.DONE), any(), any()))
                .willReturn(List.of(fixture.payment()));
        given(subscriptionPaymentRepository.findWithGraphById(30L)).willReturn(Optional.of(fixture.payment()));
        given(paymentSettlementRepository.existsByOrderIdAndSourceNot(any(), any())).willReturn(false);
        given(paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(any(), anyCollection()))
                .willReturn(BigDecimal.ZERO);
        given(paymentSettlementRepository.saveAndFlush(any(PaymentSettlement.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        service.reconcileMissingProviderSettlements(actor(), null);

        ArgumentCaptor<PaymentSettlement> captor = ArgumentCaptor.forClass(PaymentSettlement.class);
        verify(paymentSettlementRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getProvider()).isEqualTo(PaymentProviderType.TOSS);
    }

    @Test
    @DisplayName("ignoreSettlement stores and audits one normalized required note")
    void ignoreSettlementRecordsActorAndNormalizedNote() {
        User admin = User.builder()
                .id(99L)
                .nickname("admin")
                .email("admin@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build();
        PaymentSettlement settlement = PaymentSettlement.builder()
                .source(PaymentSettlementSource.CSV_MANUAL)
                .provider(PaymentProviderType.TOSS)
                .status(PaymentSettlementStatus.MISMATCHED)
                .deduplicationKey("dedup")
                .importBatchKey("batch")
                .orderId("ORDER-1")
                .grossAmount(BigDecimal.valueOf(9900))
                .refundAmount(BigDecimal.ZERO)
                .feeAmount(BigDecimal.ZERO)
                .vatAmount(BigDecimal.ZERO)
                .netSettlementAmount(BigDecimal.valueOf(9900))
                .currency("KRW")
                .settlementBaseDate(LocalDate.of(2026, 5, 26))
                .build();
        ReflectionTestUtils.setField(settlement, "id", 1L);
        CustomUserDetails actorDetails = actor();
        given(userRepository.findByIdForUpdate(99L)).willReturn(Optional.of(admin));
        given(paymentSettlementRepository.findByIdForUpdate(1L)).willReturn(Optional.of(settlement));

        var response = service.ignoreSettlement(
                1L, actorDetails, new AdminPaymentSettlementIgnoreRequest("  accepted variance  "));

        assertThat(response.getData().status()).isEqualTo(PaymentSettlementStatus.IGNORED);
        assertThat(settlement.getIgnoredBy()).isEqualTo(admin);
        assertThat(settlement.getOperatorNote()).isEqualTo("accepted variance");
        assertThat(settlement.getIgnoredAt()).isNotNull();
        InOrder operationOrder = inOrder(userRepository, paymentSettlementRepository, auditLogService);
        operationOrder.verify(userRepository).findByIdForUpdate(99L);
        operationOrder.verify(paymentSettlementRepository).findByIdForUpdate(1L);
        operationOrder.verify(auditLogService).recordPaymentSettlementEvent(
                same(actorDetails),
                eq(settlement),
                eq(PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IGNORED),
                eq(PaymentSettlementStatus.MISMATCHED),
                eq(PaymentSettlementStatus.IGNORED),
                eq("accepted variance"));
        verify(paymentSettlementRepository, never()).findWithGraphById(any());
    }

    @Test
    @DisplayName("ignoreSettlement rejects null, blank, and trimmed-over-limit notes before reads or writes")
    void ignoreSettlementRejectsInvalidNotesBeforeMutation() {
        PaymentSettlement settlement = settlementForIgnore("invalid-note-dedup");

        assertThatThrownBy(() -> service.ignoreSettlement(2L, actor(), null))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
        assertThatThrownBy(() -> service.ignoreSettlement(
                2L, actor(), new AdminPaymentSettlementIgnoreRequest(null)))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.ignoreSettlement(
                2L, actor(), new AdminPaymentSettlementIgnoreRequest("   ")))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.ignoreSettlement(
                2L, actor(), new AdminPaymentSettlementIgnoreRequest(" " + "a".repeat(501) + " ")))
                .isInstanceOf(BusinessException.class);

        assertThat(settlement.getStatus()).isEqualTo(PaymentSettlementStatus.MISMATCHED);
        assertThat(settlement.getIgnoredBy()).isNull();
        assertThat(settlement.getIgnoredAt()).isNull();
        assertThat(settlement.getOperatorNote()).isNull();
        verifyNoInteractions(paymentSettlementRepository, userRepository, auditLogService);
    }

    @Test
    @DisplayName("ignoreSettlement rejects a null principal before actor or settlement access")
    void ignoreSettlementRejectsNullPrincipal() {
        PaymentSettlement settlement = settlementForIgnore("null-principal-dedup");

        assertThatThrownBy(() -> service.ignoreSettlement(
                3L, null, new AdminPaymentSettlementIgnoreRequest("reviewed")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));

        assertSettlementUnchanged(settlement);
        verifyNoInteractions(userRepository, paymentSettlementRepository, auditLogService);
    }

    @Test
    @DisplayName("ignoreSettlement rejects an ID-less principal before actor or settlement access")
    void ignoreSettlementRejectsPrincipalWithoutId() {
        PaymentSettlement settlement = settlementForIgnore("missing-id-dedup");
        CustomUserDetails missingId = CustomUserDetails.builder()
                .email("system@test.com")
                .role(UserRole.ADMIN)
                .build();

        assertThatThrownBy(() -> service.ignoreSettlement(
                3L, missingId, new AdminPaymentSettlementIgnoreRequest("reviewed")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));

        assertSettlementUnchanged(settlement);
        verifyNoInteractions(userRepository, paymentSettlementRepository, auditLogService);
    }

    @Test
    @DisplayName("ignoreSettlement rejects a non-ADMIN principal before actor or settlement access")
    void ignoreSettlementRejectsNonAdminPrincipal() {
        PaymentSettlement settlement = settlementForIgnore("non-admin-principal-dedup");
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(98L)
                .email("user@test.com")
                .role(UserRole.USER)
                .build();

        assertThatThrownBy(() -> service.ignoreSettlement(
                3L, userDetails, new AdminPaymentSettlementIgnoreRequest("reviewed")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED));

        assertSettlementUnchanged(settlement);
        verifyNoInteractions(userRepository, paymentSettlementRepository, auditLogService);
    }

    @Test
    @DisplayName("ignoreSettlement rejects a missing authoritative actor before settlement access")
    void ignoreSettlementRejectsMissingAuthoritativeActor() {
        PaymentSettlement settlement = settlementForIgnore("missing-actor-dedup");
        given(userRepository.findByIdForUpdate(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.ignoreSettlement(
                3L, actor(), new AdminPaymentSettlementIgnoreRequest("reviewed")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        assertSettlementUnchanged(settlement);
        verify(userRepository).findByIdForUpdate(99L);
        verifyNoInteractions(paymentSettlementRepository, auditLogService);
    }

    @Test
    @DisplayName("ignoreSettlement rejects authoritative role drift before settlement access")
    void ignoreSettlementRejectsAuthoritativeNonAdminActor() {
        PaymentSettlement settlement = settlementForIgnore("role-drift-dedup");
        User authoritativeUser = authoritativeActor(UserRole.USER);
        given(userRepository.findByIdForUpdate(99L)).willReturn(Optional.of(authoritativeUser));

        assertThatThrownBy(() -> service.ignoreSettlement(
                3L, actor(), new AdminPaymentSettlementIgnoreRequest("reviewed")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED));

        assertSettlementUnchanged(settlement);
        verify(userRepository).findByIdForUpdate(99L);
        verifyNoInteractions(paymentSettlementRepository, auditLogService);
    }

    @Test
    @DisplayName("ignoreSettlement rejects a deleted authoritative ADMIN before settlement access")
    void ignoreSettlementRejectsDeletedAuthoritativeActor() {
        PaymentSettlement settlement = settlementForIgnore("deleted-actor-dedup");
        User authoritativeAdmin = authoritativeActor(UserRole.ADMIN);
        authoritativeAdmin.withdraw();
        given(userRepository.findByIdForUpdate(99L)).willReturn(Optional.of(authoritativeAdmin));

        assertThatThrownBy(() -> service.ignoreSettlement(
                3L, actor(), new AdminPaymentSettlementIgnoreRequest("reviewed")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED));

        assertSettlementUnchanged(settlement);
        verify(userRepository).findByIdForUpdate(99L);
        verifyNoInteractions(paymentSettlementRepository, auditLogService);
    }

    @Test
    @DisplayName("ignoreSettlement rejects every repeated decision without changing first evidence or audit count")
    void ignoreSettlementRejectsRepeatedDecisionsWithoutMutation() {
        User admin = User.builder()
                .id(99L)
                .nickname("admin")
                .email("admin@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build();
        PaymentSettlement settlement = settlementForIgnore("repeat-dedup");
        given(userRepository.findByIdForUpdate(99L)).willReturn(Optional.of(admin));
        given(paymentSettlementRepository.findByIdForUpdate(4L)).willReturn(Optional.of(settlement));

        service.ignoreSettlement(
                4L, actor(), new AdminPaymentSettlementIgnoreRequest("  original decision  "));
        LocalDateTime firstIgnoredAt = settlement.getIgnoredAt();

        assertThatThrownBy(() -> service.ignoreSettlement(
                4L, actor(), new AdminPaymentSettlementIgnoreRequest("original decision")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        assertThatThrownBy(() -> service.ignoreSettlement(
                4L, actor(), new AdminPaymentSettlementIgnoreRequest("conflicting decision")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));

        assertThat(settlement.getStatus()).isEqualTo(PaymentSettlementStatus.IGNORED);
        assertThat(settlement.getIgnoredBy()).isEqualTo(admin);
        assertThat(settlement.getIgnoredAt()).isEqualTo(firstIgnoredAt);
        assertThat(settlement.getOperatorNote()).isEqualTo("original decision");
        verify(paymentSettlementRepository, times(3)).findByIdForUpdate(4L);
        verify(userRepository, times(3)).findByIdForUpdate(99L);
        verify(auditLogService, times(1)).recordPaymentSettlementEvent(
                any(),
                eq(settlement),
                eq(PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IGNORED),
                eq(PaymentSettlementStatus.MISMATCHED),
                eq(PaymentSettlementStatus.IGNORED),
                eq("original decision"));
    }

    private User authoritativeActor(UserRole role) {
        return User.builder()
                .id(99L)
                .nickname("authoritative-admin")
                .email("authoritative-admin@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(role)
                .build();
    }

    private void assertSettlementUnchanged(PaymentSettlement settlement) {
        assertThat(settlement.getStatus()).isEqualTo(PaymentSettlementStatus.MISMATCHED);
        assertThat(settlement.getIgnoredBy()).isNull();
        assertThat(settlement.getIgnoredAt()).isNull();
        assertThat(settlement.getOperatorNote()).isNull();
    }

    private MockMultipartFile csv(String content) {
        return new MockMultipartFile(
                "file",
                "settlements.csv",
                "text/csv",
                content.stripIndent().getBytes(StandardCharsets.UTF_8));
    }

    private ResponseDTO<AdminPaymentSettlementImportResponse> importSettlements(
            MultipartFile file,
            String note) {
        return service.importSettlements(actor(), file, note, IDEMPOTENCY_KEY);
    }

    private PaymentSettlement settlementForIgnore(String deduplicationKey) {
        return PaymentSettlement.builder()
                .source(PaymentSettlementSource.CSV_MANUAL)
                .provider(PaymentProviderType.TOSS)
                .status(PaymentSettlementStatus.MISMATCHED)
                .deduplicationKey(deduplicationKey)
                .importBatchKey("batch")
                .orderId("ORDER-1")
                .grossAmount(BigDecimal.valueOf(9900))
                .refundAmount(BigDecimal.ZERO)
                .feeAmount(BigDecimal.ZERO)
                .vatAmount(BigDecimal.ZERO)
                .netSettlementAmount(BigDecimal.valueOf(9900))
                .currency("KRW")
                .settlementBaseDate(LocalDate.of(2026, 5, 26))
                .build();
    }

    private DataIntegrityViolationException namedDeduplicationViolation() {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new ConstraintViolationException(
                        "could not execute statement",
                        new SQLIntegrityConstraintViolationException(
                                "Duplicate entry 'deduplication' for key 'uq_payment_settlements_deduplication_key'",
                                "23000",
                                1062),
                        PaymentSettlementConstraintTranslator.DEDUPLICATION_UNIQUE_CONSTRAINT));
    }

    private DataIntegrityViolationException unrelatedIntegrityViolation() {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new ConstraintViolationException(
                        "could not execute statement",
                        new SQLIntegrityConstraintViolationException(
                                "Duplicate entry 'ORDER-1' for key 'uq_payment_settlements_order_id'",
                                "23000",
                                1062),
                        "uq_payment_settlements_order_id"));
    }

    private DataIntegrityViolationException namedAttemptKeyDigestViolation() {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new ConstraintViolationException(
                        "could not execute statement",
                        new SQLIntegrityConstraintViolationException(
                                "Duplicate entry 'digest' for key "
                                        + "'uq_payment_settlement_import_attempts_key_digest'",
                                "23000",
                                1062),
                        PaymentSettlementConstraintTranslator.ATTEMPT_KEY_DIGEST_UNIQUE_CONSTRAINT));
    }

    private DataIntegrityViolationException unrelatedAttemptIntegrityViolation() {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new ConstraintViolationException(
                        "could not execute statement",
                        new SQLIntegrityConstraintViolationException(
                                "Duplicate entry 'digest' for key "
                                        + "'uq_payment_settlement_import_attempts_other_digest'",
                                "23000",
                                1062),
                        "uq_payment_settlement_import_attempts_other_digest"));
    }

    private DataIntegrityViolationException mysqlAttemptKeyDigestViolation(int errorCode) {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new SQLException(
                        "Duplicate entry 'digest' for key "
                                + "'payment_settlement_import_attempts."
                                + PaymentSettlementConstraintTranslator.ATTEMPT_KEY_DIGEST_UNIQUE_CONSTRAINT
                                + "'",
                        "23000",
                        errorCode));
    }

    private DataIntegrityViolationException h2AttemptKeyDigestViolation(String sqlState) {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new SQLException(
                        "Unique index or primary key violation: \"PUBLIC."
                                + PaymentSettlementConstraintTranslator.ATTEMPT_KEY_DIGEST_UNIQUE_CONSTRAINT
                                + "_INDEX_7 ON PUBLIC.PAYMENT_SETTLEMENT_IMPORT_ATTEMPTS"
                                + "(KEY_DIGEST NULLS FIRST) VALUES ('digest')\"",
                        sqlState,
                        23505));
    }

    private List<String> directDependencyTypeNames(Class<?>... types) {
        return Arrays.stream(types)
                .flatMap(type -> Arrays.stream(type.getDeclaredFields()))
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(field -> field.getType().getName())
                .toList();
    }

    private Fixture fixture() {
        User user = User.builder()
                .id(16L)
                .nickname("buyer")
                .email("buyer@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build();
        Subscription subscription = Subscription.builder()
                .id(3L)
                .name("STANDARD")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(20)
                .maxWhitelistChannels(3)
                .build();
        UserSubscription userSubscription = UserSubscription.builder()
                .id(20L)
                .user(user)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .startedAt(java.time.LocalDate.of(2026, 5, 1))
                .expiresAt(java.time.LocalDate.of(2026, 6, 1))
                .build();
        PaymentOrder order = PaymentOrder.builder()
                .id(10L)
                .orderId("ORDER-1")
                .user(user)
                .purpose(PaymentPurpose.SUBSCRIBE)
                .provider(PaymentProviderType.TOSS)
                .status(PaymentOrderStatus.DONE)
                .subscription(subscription)
                .userSubscription(userSubscription)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(BigDecimal.valueOf(9900))
                .pgTransactionId("payment_key")
                .expiresAt(LocalDateTime.of(2026, 5, 1, 10, 0))
                .build();
        SubscriptionPayment payment = SubscriptionPayment.builder()
                .id(30L)
                .user(user)
                .userSubscription(userSubscription)
                .subscription(subscription)
                .paymentOrder(order)
                .billingCycle(BillingCycle.MONTHLY)
                .provider(PaymentProviderType.TOSS)
                .amount(BigDecimal.valueOf(9900))
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId("payment_key")
                .build();
        return new Fixture(order, payment);
    }

    private CustomUserDetails actor() {
        return CustomUserDetails.builder()
                .id(99L)
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .build();
    }

    private record Fixture(PaymentOrder order, SubscriptionPayment payment) {
    }
}
