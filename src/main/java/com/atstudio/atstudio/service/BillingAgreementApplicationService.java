package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.BillingAgreementCheckoutResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementResponse;
import com.atstudio.atstudio.dto.subscription.UserSubscriptionResponse;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementConfirmCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementConfirmResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementPrepareCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementPrepareResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BillingAgreementApplicationService {

    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS;
    private static final int PREPARE_CLAIM_ATTEMPTS = 3;
    private static final String ALREADY_REMOVED_BILLING_KEY = "ALREADY_REMOVED_BILLING_KEY";
    private static final String BILLING_KEY_DELETE_EXCEPTION = "BILLING_KEY_DELETE_EXCEPTION";

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final BillingKeyCrypto billingKeyCrypto;
    private final PaymentCommandTransactionService paymentCommandTransactionService;
    private final BillingAgreementCleanupTransactionService billingAgreementCleanupTransactionService;
    private final BillingAgreementCleanupProviderExecutor billingAgreementCleanupProviderExecutor;
    private final BillingAgreementPrepareTransactionService billingAgreementPrepareTransactionService;
    private final PaymentCommandKeyFactory paymentCommandKeyFactory;
    private final Map<PaymentProviderType, RecurringPaymentProvider> recurringProviders;

    public BillingAgreementApplicationService(
            UserRepository userRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            PaymentOrderRepository paymentOrderRepository,
            BillingAgreementRepository billingAgreementRepository,
            BillingKeyCrypto billingKeyCrypto,
            PaymentCommandTransactionService paymentCommandTransactionService,
            BillingAgreementCleanupTransactionService billingAgreementCleanupTransactionService,
            BillingAgreementCleanupProviderExecutor billingAgreementCleanupProviderExecutor,
            BillingAgreementPrepareTransactionService billingAgreementPrepareTransactionService,
            PaymentCommandKeyFactory paymentCommandKeyFactory,
            List<RecurringPaymentProvider> recurringProviders) {
        this.userRepository = userRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.billingAgreementRepository = billingAgreementRepository;
        this.billingKeyCrypto = billingKeyCrypto;
        this.paymentCommandTransactionService = paymentCommandTransactionService;
        this.billingAgreementCleanupTransactionService = billingAgreementCleanupTransactionService;
        this.billingAgreementCleanupProviderExecutor = billingAgreementCleanupProviderExecutor;
        this.billingAgreementPrepareTransactionService = billingAgreementPrepareTransactionService;
        this.paymentCommandKeyFactory = paymentCommandKeyFactory;
        this.recurringProviders = recurringProviders.stream()
                .collect(Collectors.toUnmodifiableMap(RecurringPaymentProvider::getProviderType, Function.identity()));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BillingAgreementPrepareResponse prepareBillingAgreement(
            CustomUserDetails userDetails,
            BillingAgreementPrepareRequest request,
            String rawIdempotencyKey) {
        String commandKey;
        try {
            commandKey = paymentCommandKeyFactory.billingAgreementPrepare(
                    userDetails.getId(), rawIdempotencyKey);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID);
        }

        RecurringPaymentProvider provider = recurringProvider();
        if (!provider.supportsPureDeterministicPrepare()) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_PROVIDER_NOT_CONFIGURED);
        }

        ensureAgreementWithBoundedRetry(userDetails.getId(), request);
        BillingAgreementPrepareTransactionService.PrepareClaim claim =
                claimPrepareWithBoundedRetry(userDetails.getId(), request, commandKey);
        BillingAgreementPrepareResult providerResult = provider.prepareAgreement(
                new BillingAgreementPrepareCommand(claim.providerCustomerKey()));
        if (providerResult == null || providerResult.provider() != claim.provider()) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_PROVIDER_NOT_CONFIGURED);
        }
        BillingAgreementPrepareTransactionService.PrepareClaim finalized =
                billingAgreementPrepareTransactionService.finalizeDescriptor(
                        claim, providerResult.providerPayload(), LocalDateTime.now());

        return new BillingAgreementPrepareResponse(
                finalized.orderID(),
                finalized.provider(),
                finalized.purpose(),
                finalized.agreementStatus(),
                finalized.subscriptionID(),
                finalized.billingCycle(),
                finalized.amount(),
                finalized.currency(),
                finalized.expiresAt(),
                toCheckoutResponse(providerResult)
        );
    }

    private void ensureAgreementWithBoundedRetry(
            Long userID,
            BillingAgreementPrepareRequest request) {
        for (int attempt = 1; attempt <= PREPARE_CLAIM_ATTEMPTS; attempt++) {
            try {
                billingAgreementPrepareTransactionService.ensureAgreement(
                        userID, request, LocalDateTime.now());
                return;
            } catch (DataIntegrityViolationException exception) {
                if (!isNamedConstraintViolation(
                        exception, "uq_billing_agreements_user_provider")) {
                    throw exception;
                }
                if (attempt == PREPARE_CLAIM_ATTEMPTS) {
                    throw new BusinessException(BUSINESS_ERROR.PAYMENT_PREPARE_ATTEMPT_CONFLICT);
                }
            }
        }
    }

    private BillingAgreementPrepareTransactionService.PrepareClaim claimPrepareWithBoundedRetry(
            Long userID,
            BillingAgreementPrepareRequest request,
            String commandKey) {
        for (int attempt = 1; attempt <= PREPARE_CLAIM_ATTEMPTS; attempt++) {
            try {
                return billingAgreementPrepareTransactionService.claim(
                        userID, request, commandKey, LocalDateTime.now());
            } catch (DataIntegrityViolationException exception) {
                if (!isNamedConstraintViolation(exception, "uq_payment_orders_command_key")) {
                    throw exception;
                }
                if (attempt == PREPARE_CLAIM_ATTEMPTS) {
                    throw new BusinessException(BUSINESS_ERROR.PAYMENT_PREPARE_ATTEMPT_CONFLICT);
                }
            }
        }
        throw new BusinessException(BUSINESS_ERROR.PAYMENT_PREPARE_ATTEMPT_CONFLICT);
    }

    private boolean isNamedConstraintViolation(
            DataIntegrityViolationException exception,
            String constraintName) {
        String message = exception.getMostSpecificCause().getMessage();
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains(constraintName);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BillingAgreementConfirmResponse confirmBillingAgreement(
            CustomUserDetails userDetails,
            BillingAgreementConfirmRequest request) {
        PaymentCommandTransactionService.BillingConfirmClaim claim =
                paymentCommandTransactionService.claimBillingConfirm(
                        userDetails.getId(),
                        request.orderId(),
                        request.customerKey(),
                        request.amount(),
                        LocalDateTime.now());
        if (claim.action() == PaymentCommandTransactionService.BillingConfirmAction.COMPLETED) {
            return claim.response();
        }
        if (claim.action() == PaymentCommandTransactionService.BillingConfirmAction.FINALIZE_ONLY) {
            return paymentCommandTransactionService.finalizeInitialCharge(
                    userDetails.getId(),
                    claim.agreementID(),
                    claim.orderID());
        }

        BillingAgreementConfirmResult issueResult;
        try {
            issueResult = recurringProvider().confirmAgreement(
                    new BillingAgreementConfirmCommand(request.authKey(), claim.providerCustomerKey()));
        } catch (RuntimeException exception) {
            recordProviderFailure(
                    claim,
                    "BILLING_KEY_ISSUE_EXCEPTION",
                    exception.getClass().getSimpleName(),
                    PaymentCommandTransactionService.ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION,
                    false);
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED);
        }
        if (issueResult == null) {
            recordProviderFailure(
                    claim,
                    "BILLING_KEY_ISSUE_EMPTY_RESULT",
                    "Provider returned no billing-key issue result.",
                    PaymentCommandTransactionService.ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION,
                    false);
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED);
        }
        if (!issueResult.success()) {
            recordProviderFailure(
                    claim,
                    issueResult.failureCode(),
                    issueResult.failureMessage(),
                    PaymentCommandTransactionService.ProviderFailureDisposition.FAILED,
                    false);
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED);
        }
        if (isBlank(issueResult.billingKey())) {
            recordProviderFailure(
                    claim,
                    "BILLING_KEY_MISSING",
                    "Billing key was not returned by provider.",
                    PaymentCommandTransactionService.ProviderFailureDisposition.FAILED,
                    false);
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED);
        }

        try {
            BillingKeyCrypto.ProtectedBillingKey protectedBillingKey =
                    billingKeyCrypto.encrypt(issueResult.billingKey());
            paymentCommandTransactionService.storeIssuedBillingKey(
                    claim.agreementID(),
                    claim.orderID(),
                    protectedBillingKey.ciphertext(),
                    protectedBillingKey.fingerprint(),
                    issueResult.payMethod(),
                    issueResult.maskedMethod());
        } catch (RuntimeException exception) {
            recordProviderFailure(
                    claim,
                    "BILLING_KEY_STORAGE_FAILED",
                    exception.getClass().getSimpleName(),
                    PaymentCommandTransactionService.ProviderFailureDisposition.FAILED,
                    false);
            cleanupIssuedBillingKey(issueResult.billingKey(), claim.agreementID());
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED);
        }

        if (claim.purpose() == PaymentPurpose.BILLING_AGREEMENT) {
            paymentCommandTransactionService.recordProviderSuccess(
                    claim.agreementID(),
                    claim.orderID(),
                    "billing-agreement-" + claim.orderID(),
                    issueResult.providerPayload(),
                    issueResult.payMethod(),
                    issueResult.maskedMethod());
            return paymentCommandTransactionService.finalizeInitialCharge(
                    userDetails.getId(),
                    claim.agreementID(),
                    claim.orderID());
        }

        BillingChargeResult chargeResult;
        try {
            chargeResult = recurringProvider().charge(new BillingChargeCommand(
                    issueResult.billingKey(),
                    claim.providerCustomerKey(),
                    claim.orderID(),
                    claim.orderName(),
                    claim.amount(),
                    claim.userEmail(),
                    claim.userNickname(),
                    claim.providerIdempotencyKey()));
        } catch (RuntimeException exception) {
            recordProviderFailure(
                    claim,
                    "BILLING_CHARGE_EXCEPTION",
                    exception.getClass().getSimpleName(),
                    PaymentCommandTransactionService.ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION,
                    true);
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED);
        }
        if (chargeResult == null) {
            recordProviderFailure(
                    claim,
                    "BILLING_CHARGE_EMPTY_RESULT",
                    "Provider returned no initial-charge result.",
                    PaymentCommandTransactionService.ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION,
                    true);
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED);
        }
        if (!chargeResult.success()) {
            recordProviderFailure(
                    claim,
                    chargeResult.failureCode(),
                    chargeResult.failureMessage(),
                    PaymentCommandTransactionService.ProviderFailureDisposition.FAILED,
                    true);
            cleanupIssuedBillingKey(issueResult.billingKey(), claim.agreementID());
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED);
        }
        if (isBlank(chargeResult.transactionId())) {
            recordProviderFailure(
                    claim,
                    "PROVIDER_TRANSACTION_MISSING",
                    "Provider success did not include a transaction ID.",
                    PaymentCommandTransactionService.ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION,
                    true);
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED);
        }

        paymentCommandTransactionService.recordProviderSuccess(
                claim.agreementID(),
                claim.orderID(),
                chargeResult.transactionId(),
                chargeResult.providerPayload(),
                chargeResult.payMethod(),
                chargeResult.maskedMethod());
        return paymentCommandTransactionService.finalizeInitialCharge(
                userDetails.getId(),
                claim.agreementID(),
                claim.orderID());
    }

    public BillingAgreementResponse getMyBillingAgreement(CustomUserDetails userDetails) {
        User user = findUser(userDetails);
        BillingAgreement agreement = billingAgreementRepository.findByUserAndProvider(user, RECURRING_PROVIDER)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND));
        return toAgreementResponse(agreement, findActiveSubscriptionOrNull(user));
    }

    @Transactional(propagation = Propagation.NEVER)
    public BillingAgreementResponse cancelMyBillingAgreement(CustomUserDetails userDetails) {
        BillingAgreementCleanupTransactionService.UserCancellationClaim claim =
                billingAgreementCleanupTransactionService.claimUserCancellation(
                        userDetails.getId(),
                        LocalDateTime.now());
        if (claim.action() == BillingAgreementCleanupTransactionService.CleanupAction.COMPLETED) {
            return claim.response();
        }
        if (claim.action() != BillingAgreementCleanupTransactionService.CleanupAction.CALL_PROVIDER) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CANCEL_FAILED);
        }

        BillingAgreementCleanupProviderExecutor.CleanupProviderResult providerResult =
                billingAgreementCleanupProviderExecutor.deleteBillingKey(
                        claim.provider(),
                        claim.billingKeyCiphertext());
        BillingAgreementResponse response =
                billingAgreementCleanupTransactionService.recordUserCancellationResult(
                        userDetails.getId(),
                        claim,
                        providerResult);
        if (providerResult.disposition()
                != BillingAgreementCleanupProviderExecutor.CleanupDisposition.SUCCEEDED) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CANCEL_FAILED);
        }
        return response;
    }

    private PaymentOrder findOwnedBillingOrder(User user, String orderId) {
        PaymentOrder order = paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_NOT_FOUND));
        if (!order.isOwnedBy(user)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
        if (order.getProvider() != RECURRING_PROVIDER
                || (order.getPurpose() != PaymentPurpose.SUBSCRIBE
                && order.getPurpose() != PaymentPurpose.BILLING_AGREEMENT)) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        return order;
    }

    private User findUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private UserSubscription findActiveSubscriptionOrNull(User user) {
        return userSubscriptionRepository.findActiveByUser(user, LocalDate.now()).orElse(null);
    }

    private RecurringPaymentProvider recurringProvider() {
        RecurringPaymentProvider provider = recurringProviders.get(RECURRING_PROVIDER);
        if (provider == null) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_PROVIDER_NOT_CONFIGURED);
        }
        return provider;
    }

    private BillingAgreementCheckoutResponse toCheckoutResponse(BillingAgreementPrepareResult providerResult) {
        Map<String, String> metadata = providerResult.checkoutMetadata();
        return new BillingAgreementCheckoutResponse(
                providerResult.checkoutType(),
                metadata.get("clientKey"),
                metadata.get("customerKey"),
                metadata.get("successUrl"),
                metadata.get("failUrl"),
                metadata.get("method")
        );
    }

    private BillingAgreementResponse toAgreementResponse(
            BillingAgreement agreement,
            UserSubscription subscription) {
        return new BillingAgreementResponse(
                agreement.getProvider(),
                agreement.getStatus(),
                agreement.getPayMethod(),
                agreement.getMaskedMethod(),
                agreement.getNextBillingAt(),
                agreement.getLastChargedAt(),
                agreement.getCancelledAt(),
                subscription == null ? null : UserSubscriptionResponse.from(subscription)
        );
    }

    private void recordProviderFailure(
            PaymentCommandTransactionService.BillingConfirmClaim claim,
            String failureCode,
            String failureMessage,
            PaymentCommandTransactionService.ProviderFailureDisposition disposition,
            boolean recordFailedCharge) {
        paymentCommandTransactionService.recordProviderFailure(
                claim.agreementID(),
                claim.orderID(),
                failureCode,
                failureMessage,
                disposition,
                recordFailedCharge);
    }

    private void cleanupIssuedBillingKey(String billingKey, Long agreementID) {
        BillingAgreementCancelResult cancelResult;
        try {
            cancelResult = recurringProvider().cancelAgreement(new BillingAgreementCancelCommand(billingKey));
        } catch (RuntimeException exception) {
            paymentCommandTransactionService.recordBillingCleanupFailure(
                    agreementID,
                    BILLING_KEY_DELETE_EXCEPTION,
                    exception.getClass().getSimpleName());
            return;
        }

        if (cancelResult != null
                && (cancelResult.success()
                || ALREADY_REMOVED_BILLING_KEY.equals(cancelResult.failureCode()))) {
            paymentCommandTransactionService.clearIssuedBillingKeyAfterCleanup(agreementID);
            return;
        }

        paymentCommandTransactionService.recordBillingCleanupFailure(
                agreementID,
                cancelResult == null ? BILLING_KEY_DELETE_EXCEPTION : cancelResult.failureCode(),
                cancelResult == null ? "EmptyProviderResult" : cancelResult.failureMessage());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
