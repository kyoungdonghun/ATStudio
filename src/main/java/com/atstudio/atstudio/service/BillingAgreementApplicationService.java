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
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.billing.BillingCustomerKeyGenerator;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BillingAgreementApplicationService {

    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS;
    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int PAYMENT_EXPIRY_MINUTES = 15;
    private static final String ALREADY_REMOVED_BILLING_KEY = "ALREADY_REMOVED_BILLING_KEY";
    private static final String BILLING_KEY_DELETE_EXCEPTION = "BILLING_KEY_DELETE_EXCEPTION";

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final CompanyCertificationRepository companyCertificationRepository;
    private final BillingCustomerKeyGenerator billingCustomerKeyGenerator;
    private final BillingKeyCrypto billingKeyCrypto;
    private final PaymentCommandTransactionService paymentCommandTransactionService;
    private final BillingAgreementCleanupTransactionService billingAgreementCleanupTransactionService;
    private final BillingAgreementCleanupProviderExecutor billingAgreementCleanupProviderExecutor;
    private final Map<PaymentProviderType, RecurringPaymentProvider> recurringProviders;

    public BillingAgreementApplicationService(
            UserRepository userRepository,
            SubscriptionRepository subscriptionRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            PaymentOrderRepository paymentOrderRepository,
            BillingAgreementRepository billingAgreementRepository,
            CompanyCertificationRepository companyCertificationRepository,
            BillingCustomerKeyGenerator billingCustomerKeyGenerator,
            BillingKeyCrypto billingKeyCrypto,
            PaymentCommandTransactionService paymentCommandTransactionService,
            BillingAgreementCleanupTransactionService billingAgreementCleanupTransactionService,
            BillingAgreementCleanupProviderExecutor billingAgreementCleanupProviderExecutor,
            List<RecurringPaymentProvider> recurringProviders) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.billingAgreementRepository = billingAgreementRepository;
        this.companyCertificationRepository = companyCertificationRepository;
        this.billingCustomerKeyGenerator = billingCustomerKeyGenerator;
        this.billingKeyCrypto = billingKeyCrypto;
        this.paymentCommandTransactionService = paymentCommandTransactionService;
        this.billingAgreementCleanupTransactionService = billingAgreementCleanupTransactionService;
        this.billingAgreementCleanupProviderExecutor = billingAgreementCleanupProviderExecutor;
        this.recurringProviders = recurringProviders.stream()
                .collect(Collectors.toUnmodifiableMap(RecurringPaymentProvider::getProviderType, Function.identity()));
    }

    @Transactional
    public BillingAgreementPrepareResponse prepareBillingAgreement(
            CustomUserDetails userDetails,
            BillingAgreementPrepareRequest request) {
        User user = findUser(userDetails);
        Subscription subscription = findSubscription(request.subscriptionId());
        validateSubscriptionUserType(user, subscription);
        UserSubscription activeSubscription = findActiveSubscriptionOrNull(user);
        PaymentPurpose purpose = activeSubscription == null
                ? PaymentPurpose.SUBSCRIBE
                : PaymentPurpose.BILLING_AGREEMENT;
        if (activeSubscription == null) {
            validateSubscriptionPreconditions(user);
        } else {
            validateBillingAgreementRegistrationRequest(activeSubscription, subscription, request.billingCycle());
        }

        BillingAgreement agreement = prepareAgreement(user);
        BigDecimal amount = purpose == PaymentPurpose.SUBSCRIBE
                ? priceFor(subscription, request.billingCycle())
                : BigDecimal.ZERO;
        PaymentOrder order = paymentOrderRepository.save(PaymentOrder.builder()
                .orderId(generateOrderId())
                .user(user)
                .purpose(purpose)
                .provider(RECURRING_PROVIDER)
                .subscription(subscription)
                .userSubscription(activeSubscription)
                .billingAgreement(agreement)
                .billingCycle(request.billingCycle())
                .amount(amount)
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusMinutes(PAYMENT_EXPIRY_MINUTES))
                .build());

        BillingAgreementPrepareResult providerResult = recurringProvider().prepareAgreement(
                new BillingAgreementPrepareCommand(agreement.getProviderCustomerKey()));
        order.markInProgress(providerResult.providerPayload());

        return new BillingAgreementPrepareResponse(
                order.getOrderId(),
                order.getProvider(),
                order.getPurpose(),
                agreement.getStatus(),
                subscription.getId(),
                order.getBillingCycle(),
                order.getAmount(),
                order.getCurrency(),
                order.getExpiresAt(),
                toCheckoutResponse(providerResult)
        );
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

    private BillingAgreement prepareAgreement(User user) {
        Optional<BillingAgreement> existing =
                billingAgreementRepository.findByUserAndProvider(user, RECURRING_PROVIDER);
        if (existing.isPresent()) {
            BillingAgreement agreement = existing.get();
            if (agreement.getStatus() == BillingAgreementStatus.ACTIVE) {
                throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_ALREADY_ACTIVE);
            }
            if (agreement.getStatus() == BillingAgreementStatus.READY
                    && !isBlank(agreement.getBillingKeyCiphertext())) {
                throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
            }
            agreement.prepareRegistration(agreement.getProviderCustomerKey());
            return agreement;
        }

        return billingAgreementRepository.save(BillingAgreement.builder()
                .user(user)
                .provider(RECURRING_PROVIDER)
                .providerCustomerKey(billingCustomerKeyGenerator.generate())
                .build());
    }

    private void validateBillingAgreementRegistrationRequest(
            UserSubscription activeSubscription,
            Subscription subscription,
            BillingCycle billingCycle) {
        if (!activeSubscription.getSubscription().getId().equals(subscription.getId())
                || activeSubscription.getBillingCycle() != billingCycle) {
            throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_ALREADY_EXISTS);
        }
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

    private Subscription findSubscription(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
    }

    private UserSubscription findActiveSubscriptionOrNull(User user) {
        return userSubscriptionRepository.findActiveByUser(user, LocalDate.now()).orElse(null);
    }

    private void validateSubscriptionPreconditions(User user) {
        if (user.getUserType() == UserType.BUSINESS) {
            boolean approved = companyCertificationRepository.existsByUserAndStatusIn(
                    user, List.of(CompanyCertificationStatus.APPROVED));
            if (!approved) {
                throw new BusinessException(BUSINESS_ERROR.COMPANY_CERTIFICATION_REQUIRED);
            }
        }

        userSubscriptionRepository.findActiveByUser(user, LocalDate.now())
                .ifPresent(us -> {
                    throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_ALREADY_EXISTS);
                });
    }

    private void validateSubscriptionUserType(User user, Subscription subscription) {
        if (subscription.getUserType() != user.getUserType()) {
            throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH);
        }
    }

    private BigDecimal priceFor(Subscription subscription, BillingCycle billingCycle) {
        return billingCycle == BillingCycle.MONTHLY
                ? subscription.getPriceMonthly()
                : subscription.getPriceYearly();
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

    private String generateOrderId() {
        String orderId;
        do {
            orderId = "ATS-BILL-" + LocalDate.now().format(ORDER_DATE) + "-"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (paymentOrderRepository.existsByOrderId(orderId));
        return orderId;
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
