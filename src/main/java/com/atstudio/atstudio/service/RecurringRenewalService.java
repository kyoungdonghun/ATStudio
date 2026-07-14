package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.service.PaymentCommandTransactionService.ProviderFailureDisposition;
import com.atstudio.atstudio.service.PaymentCommandTransactionService.RenewalAction;
import com.atstudio.atstudio.service.PaymentCommandTransactionService.RenewalClaim;
import com.atstudio.atstudio.service.PaymentCommandTransactionService.RenewalFailureResult;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecurringRenewalService {

    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS_BILLING;
    private static final int DUE_SCAN_PAGE_SIZE = 100;

    private final BillingAgreementRepository billingAgreementRepository;
    private final PaymentCommandTransactionService paymentCommandTransactions;
    private final BillingKeyCrypto billingKeyCrypto;
    private final EmailService emailService;
    private final Map<PaymentProviderType, RecurringPaymentProvider> recurringProviders;

    public RecurringRenewalService(
            BillingAgreementRepository billingAgreementRepository,
            PaymentCommandTransactionService paymentCommandTransactions,
            BillingKeyCrypto billingKeyCrypto,
            EmailService emailService,
            List<RecurringPaymentProvider> recurringProviders) {
        this.billingAgreementRepository = billingAgreementRepository;
        this.paymentCommandTransactions = paymentCommandTransactions;
        this.billingKeyCrypto = billingKeyCrypto;
        this.emailService = emailService;
        this.recurringProviders = recurringProviders.stream()
                .collect(Collectors.toUnmodifiableMap(RecurringPaymentProvider::getProviderType, Function.identity()));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public RenewalRunResult processDueRenewals() {
        return processDueRenewals(LocalDate.now());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public RenewalRunResult processDueRenewals(LocalDate today) {
        long lastSeenID = 0L;
        int attempted = 0;
        int succeeded = 0;
        int failed = 0;
        int skipped = 0;

        while (true) {
            List<Long> dueAgreementIDs = billingAgreementRepository.findDueRenewalCandidateIDs(
                    BillingAgreementStatus.ACTIVE,
                    today,
                    lastSeenID,
                    PageRequest.of(0, DUE_SCAN_PAGE_SIZE));
            if (dueAgreementIDs.isEmpty()) {
                break;
            }

            for (Long billingAgreementID : dueAgreementIDs) {
                lastSeenID = billingAgreementID;
                RenewalOutcome outcome = processAgreement(billingAgreementID, today);
                attempted += outcome.attempted();
                succeeded += outcome.succeeded();
                failed += outcome.failed();
                skipped += outcome.skipped();
            }
            if (dueAgreementIDs.size() < DUE_SCAN_PAGE_SIZE) {
                break;
            }
        }

        if (attempted > 0 || failed > 0 || skipped > 0) {
            log.info("Recurring renewal processed: attempted={}, succeeded={}, failed={}, skipped={}",
                    attempted, succeeded, failed, skipped);
        }
        return new RenewalRunResult(attempted, succeeded, failed, skipped);
    }

    private RenewalOutcome processAgreement(Long billingAgreementID, LocalDate today) {
        RenewalClaim claim = null;
        try {
            claim = paymentCommandTransactions.claimRenewal(
                    billingAgreementID,
                    today,
                    LocalDateTime.now());
            return processClaim(claim, today);
        } catch (BusinessException exception) {
            log.warn(
                    "Recurring renewal agreement failed. agreementId={}, errorCode={}",
                    billingAgreementID,
                    exception.getErrorCode());
            return failedOutcomeFor(claim);
        } catch (RuntimeException exception) {
            log.warn(
                    "Recurring renewal agreement failed. agreementId={}, exception={}",
                    billingAgreementID,
                    exception.getClass().getSimpleName());
            return failedOutcomeFor(claim);
        }
    }

    private RenewalOutcome failedOutcomeFor(RenewalClaim claim) {
        return claim != null && claim.action() == RenewalAction.CALL_PROVIDER
                ? RenewalOutcome.failure()
                : RenewalOutcome.failedWithoutAttempt();
    }

    private RenewalOutcome processClaim(RenewalClaim claim, LocalDate today) {
        if (claim.action() == RenewalAction.SKIPPED) {
            return RenewalOutcome.skip();
        }
        if (claim.action() == RenewalAction.FAILED_WITHOUT_ATTEMPT) {
            notifyRenewalFailure(claim.user(), claim.orderID(), claim.graceEndsAt(), claim.finalFailure());
            return RenewalOutcome.failedWithoutAttempt();
        }
        if (claim.action() == RenewalAction.FINALIZE_ONLY) {
            paymentCommandTransactions.finalizeRenewal(claim.agreementID(), claim.orderID());
            return RenewalOutcome.success();
        }

        return chargeClaim(claim, today);
    }

    private RenewalOutcome chargeClaim(RenewalClaim claim, LocalDate today) {
        BillingChargeResult chargeResult;
        try {
            chargeResult = recurringProvider().charge(new BillingChargeCommand(
                    billingKeyCrypto.decrypt(claim.billingKeyCiphertext()),
                    claim.providerCustomerKey(),
                    claim.orderID(),
                    claim.orderName(),
                    claim.amount(),
                    claim.userEmail(),
                    claim.userNickname(),
                    claim.providerIdempotencyKey()));
        } catch (RuntimeException exception) {
            recordAmbiguousProviderFailure(claim, today, exception);
            return RenewalOutcome.failure();
        }

        if (chargeResult == null) {
            recordAmbiguousProviderFailure(
                    claim,
                    today,
                    "PROVIDER_NULL_RESULT",
                    "Provider returned null charge result.");
            return RenewalOutcome.failure();
        }
        if (chargeResult.success()) {
            if (isBlank(chargeResult.transactionId())) {
                recordAmbiguousProviderFailure(
                        claim,
                        today,
                        "PROVIDER_SUCCESS_MISSING_TRANSACTION_ID",
                        "Provider success result did not include a transaction ID.");
                return RenewalOutcome.failure();
            }
            paymentCommandTransactions.recordProviderSuccess(
                    claim.agreementID(),
                    claim.orderID(),
                    chargeResult.transactionId(),
                    chargeResult.providerPayload(),
                    null,
                    null);
            try {
                paymentCommandTransactions.finalizeRenewal(claim.agreementID(), claim.orderID());
                return RenewalOutcome.success();
            } catch (RuntimeException exception) {
                log.warn(
                        "Recurring renewal local finalization failed after provider success. "
                                + "agreementId={}, orderId={}, exception={}",
                        claim.agreementID(),
                        claim.orderID(),
                        exception.getClass().getSimpleName());
                return RenewalOutcome.failure();
            }
        }

        RenewalFailureResult failure = paymentCommandTransactions.recordRenewalProviderFailure(
                claim.agreementID(),
                claim.orderID(),
                chargeResult.failureCode(),
                chargeResult.failureMessage(),
                ProviderFailureDisposition.FAILED,
                today);
        notifyRenewalFailure(failure.user(), failure.orderID(), failure.graceEndsAt(), failure.finalFailure());
        return RenewalOutcome.failure();
    }

    private void recordAmbiguousProviderFailure(
            RenewalClaim claim,
            LocalDate today,
            RuntimeException exception) {
        recordAmbiguousProviderFailure(
                claim,
                today,
                "PROVIDER_EXCEPTION",
                exception.getClass().getSimpleName());
        log.warn(
                "Recurring renewal provider outcome is ambiguous. agreementId={}, orderId={}, exception={}",
                claim.agreementID(),
                claim.orderID(),
                exception.getClass().getSimpleName());
    }

    private void recordAmbiguousProviderFailure(
            RenewalClaim claim,
            LocalDate today,
            String code,
            String message) {
        try {
            paymentCommandTransactions.recordRenewalProviderFailure(
                    claim.agreementID(),
                    claim.orderID(),
                    code,
                    message,
                    ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION,
                    today);
        } catch (RuntimeException recordException) {
            log.warn(
                    "Failed to persist ambiguous renewal provider outcome. agreementId={}, orderId={}, exception={}",
                    claim.agreementID(),
                    claim.orderID(),
                    recordException.getClass().getSimpleName());
        }
    }

    private void notifyRenewalFailure(
            User user,
            String orderID,
            LocalDate graceEndsAt,
            boolean finalFailure) {
        if (user == null || orderID == null || graceEndsAt == null) {
            return;
        }
        String summary = finalFailure
                ? "Your subscription renewal payment has failed repeatedly and automatic renewal is suspended."
                : "Your subscription renewal payment could not be completed.";
        String retryGuide = finalFailure
                ? "Access remains available until the grace period ends on " + graceEndsAt
                    + ". Please contact support or register a valid payment method."
                : "We will retry automatically during the grace period until " + graceEndsAt
                    + ". Please check your registered payment method.";
        try {
            emailService.sendSubscriptionPaymentFailureEmail(
                    user,
                    summary + " Order: " + orderID,
                    retryGuide);
        } catch (RuntimeException exception) {
            log.warn("Failed to send renewal failure email. orderId={}", orderID, exception);
        }
    }

    private RecurringPaymentProvider recurringProvider() {
        RecurringPaymentProvider provider = recurringProviders.get(RECURRING_PROVIDER);
        if (provider == null) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_PROVIDER_NOT_CONFIGURED);
        }
        return provider;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record RenewalRunResult(int attempted, int succeeded, int failed, int skipped) {
    }

    private record RenewalOutcome(int attempted, int succeeded, int failed, int skipped) {
        private static RenewalOutcome success() {
            return new RenewalOutcome(1, 1, 0, 0);
        }

        private static RenewalOutcome failure() {
            return new RenewalOutcome(1, 0, 1, 0);
        }

        private static RenewalOutcome failedWithoutAttempt() {
            return new RenewalOutcome(0, 0, 1, 0);
        }

        private static RenewalOutcome skip() {
            return new RenewalOutcome(0, 0, 0, 1);
        }
    }
}
