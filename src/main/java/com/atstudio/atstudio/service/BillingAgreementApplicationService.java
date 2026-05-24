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
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
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

    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS_BILLING;
    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int PAYMENT_EXPIRY_MINUTES = 15;

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final CompanyCertificationRepository companyCertificationRepository;
    private final PlaylistService playlistService;
    private final BillingCustomerKeyGenerator billingCustomerKeyGenerator;
    private final BillingKeyCrypto billingKeyCrypto;
    private final PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    private final Map<PaymentProviderType, RecurringPaymentProvider> recurringProviders;

    public BillingAgreementApplicationService(
            UserRepository userRepository,
            SubscriptionRepository subscriptionRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            PaymentOrderRepository paymentOrderRepository,
            SubscriptionPaymentRepository subscriptionPaymentRepository,
            BillingAgreementRepository billingAgreementRepository,
            CompanyCertificationRepository companyCertificationRepository,
            PlaylistService playlistService,
            BillingCustomerKeyGenerator billingCustomerKeyGenerator,
            BillingKeyCrypto billingKeyCrypto,
            PaymentReceiptEvidenceService paymentReceiptEvidenceService,
            List<RecurringPaymentProvider> recurringProviders) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.subscriptionPaymentRepository = subscriptionPaymentRepository;
        this.billingAgreementRepository = billingAgreementRepository;
        this.companyCertificationRepository = companyCertificationRepository;
        this.playlistService = playlistService;
        this.billingCustomerKeyGenerator = billingCustomerKeyGenerator;
        this.billingKeyCrypto = billingKeyCrypto;
        this.paymentReceiptEvidenceService = paymentReceiptEvidenceService;
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

    @Transactional
    public BillingAgreementConfirmResponse confirmBillingAgreement(
            CustomUserDetails userDetails,
            BillingAgreementConfirmRequest request) {
        User user = findUser(userDetails);
        PaymentOrder order = findOwnedBillingOrder(user, request.orderId());
        BillingAgreement agreement = order.getBillingAgreement();

        if (order.getStatus() == PaymentOrderStatus.DONE) {
            return toConfirmResponse(order, agreement);
        }
        validateConfirmable(order, agreement, request);

        BillingAgreementConfirmResult issueResult = recurringProvider().confirmAgreement(
                new BillingAgreementConfirmCommand(request.authKey(), request.customerKey()));
        if (!issueResult.success()) {
            order.markFailed(issueResult.failureCode(), issueResult.failureMessage());
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED);
        }
        if (isBlank(issueResult.billingKey())) {
            order.markFailed("BILLING_KEY_MISSING", "Billing key was not returned by provider.");
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED);
        }

        BillingKeyCrypto.ProtectedBillingKey protectedBillingKey =
                billingKeyCrypto.encrypt(issueResult.billingKey());
        agreement.storeIssuedKey(
                protectedBillingKey.ciphertext(),
                protectedBillingKey.fingerprint(),
                issueResult.payMethod(),
                issueResult.maskedMethod());

        if (order.getPurpose() == PaymentPurpose.BILLING_AGREEMENT) {
            UserSubscription activeSubscription = findActiveSubscriptionOrNull(user);
            if (activeSubscription == null) {
                deleteIssuedKeyAfterFailedInitialCharge(issueResult.billingKey(), agreement);
                throw new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION);
            }
            agreement.activate(
                    protectedBillingKey.ciphertext(),
                    protectedBillingKey.fingerprint(),
                    issueResult.payMethod(),
                    issueResult.maskedMethod(),
                    activeSubscription.getExpiresAt());
            order.markDone(
                    "billing-agreement-" + order.getOrderId(),
                    activeSubscription,
                    issueResult.providerPayload());
            return toConfirmResponse(order, agreement);
        }

        BillingChargeResult chargeResult = recurringProvider().charge(new BillingChargeCommand(
                issueResult.billingKey(),
                agreement.getProviderCustomerKey(),
                order.getOrderId(),
                orderName(order),
                order.getAmount(),
                user.getEmail(),
                user.getNickname(),
                "billing-initial-" + order.getOrderId()
        ));
        if (!chargeResult.success()) {
            order.markFailed(chargeResult.failureCode(), chargeResult.failureMessage());
            agreement.recordFailedCharge();
            deleteIssuedKeyAfterFailedInitialCharge(issueResult.billingKey(), agreement);
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CONFIRM_FAILED);
        }

        UserSubscription subscription = applySubscriptionAction(order);
        SubscriptionPayment subscriptionPayment =
                saveSubscriptionPayment(order, subscription, chargeResult.transactionId(), agreement);
        order.markDone(chargeResult.transactionId(), subscription, chargeResult.providerPayload());
        paymentReceiptEvidenceService.publishSuccessfulChargeEvidence(
                order,
                subscriptionPayment,
                chargeResult.providerPayload());
        agreement.activate(
                protectedBillingKey.ciphertext(),
                protectedBillingKey.fingerprint(),
                firstPresent(chargeResult.payMethod(), issueResult.payMethod()),
                firstPresent(chargeResult.maskedMethod(), issueResult.maskedMethod()),
                subscription.getExpiresAt());
        agreement.recordSuccessfulCharge(subscription.getExpiresAt());

        return toConfirmResponse(order, agreement);
    }

    public BillingAgreementResponse getMyBillingAgreement(CustomUserDetails userDetails) {
        User user = findUser(userDetails);
        BillingAgreement agreement = billingAgreementRepository.findByUserAndProvider(user, RECURRING_PROVIDER)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND));
        return toAgreementResponse(agreement, findActiveSubscriptionOrNull(user));
    }

    @Transactional
    public BillingAgreementResponse cancelMyBillingAgreement(CustomUserDetails userDetails) {
        User user = findUser(userDetails);
        BillingAgreement agreement = billingAgreementRepository.findByUserAndProvider(user, RECURRING_PROVIDER)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND));
        if (agreement.getStatus() == BillingAgreementStatus.CANCELLED) {
            return toAgreementResponse(agreement, findActiveSubscriptionOrNull(user));
        }
        if (agreement.getStatus() == BillingAgreementStatus.EXPIRED) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }

        if (!isBlank(agreement.getBillingKeyCiphertext())) {
            String billingKey = billingKeyCrypto.decrypt(agreement.getBillingKeyCiphertext());
            BillingAgreementCancelResult cancelResult = recurringProvider().cancelAgreement(
                    new BillingAgreementCancelCommand(billingKey));
            if (!cancelResult.success()) {
                throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_CANCEL_FAILED);
            }
            agreement.clearIssuedKey();
        }

        agreement.cancel();
        UserSubscription subscription = findActiveSubscriptionOrNull(user);
        if (subscription != null) {
            subscription.cancel();
        }
        return toAgreementResponse(agreement, subscription);
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

    private void validateConfirmable(
            PaymentOrder order,
            BillingAgreement agreement,
            BillingAgreementConfirmRequest request) {
        if (agreement == null) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND);
        }
        if (agreement.getStatus() != BillingAgreementStatus.READY) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
        if (order.getStatus() == PaymentOrderStatus.FAILED
                || order.getStatus() == PaymentOrderStatus.CANCELLED
                || order.getStatus() == PaymentOrderStatus.EXPIRED) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        if (order.isExpired(LocalDateTime.now())) {
            order.markExpired();
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_EXPIRED);
        }
        if (!agreement.isOwnedBy(order.getUser())) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
        if (!agreement.getProviderCustomerKey().equals(request.customerKey())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        if (order.getAmount().compareTo(request.amount()) != 0) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_AMOUNT_MISMATCH);
        }
        validateSubscriptionUserType(order.getUser(), order.getSubscription());
        if (order.getPurpose() == PaymentPurpose.SUBSCRIBE) {
            validateSubscriptionPreconditions(order.getUser());
        } else {
            UserSubscription activeSubscription = findActiveSubscriptionOrNull(order.getUser());
            if (activeSubscription == null) {
                throw new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION);
            }
            validateBillingAgreementRegistrationRequest(
                    activeSubscription,
                    order.getSubscription(),
                    order.getBillingCycle());
        }
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

    private void deleteIssuedKeyAfterFailedInitialCharge(String billingKey, BillingAgreement agreement) {
        BillingAgreementCancelResult cancelResult = recurringProvider().cancelAgreement(
                new BillingAgreementCancelCommand(billingKey));
        if (cancelResult.success()) {
            agreement.clearIssuedKey();
        }
    }

    private UserSubscription applySubscriptionAction(PaymentOrder order) {
        LocalDate startedAt = LocalDate.now();
        LocalDate expiresAt = expiresAt(startedAt, order.getBillingCycle());
        UserSubscription userSubscription = userSubscriptionRepository.findByUser(order.getUser())
                .map(existing -> {
                    existing.startNewSubscription(
                            order.getSubscription(),
                            order.getBillingCycle(),
                            startedAt,
                            expiresAt);
                    return existing;
                })
                .orElseGet(() -> userSubscriptionRepository.save(
                        UserSubscription.builder()
                                .user(order.getUser())
                                .subscription(order.getSubscription())
                                .billingCycle(order.getBillingCycle())
                                .startedAt(startedAt)
                                .expiresAt(expiresAt)
                                .build()));

        playlistService.createDefaultPlaylist(order.getUser());
        return userSubscription;
    }

    private SubscriptionPayment saveSubscriptionPayment(
            PaymentOrder order,
            UserSubscription userSubscription,
            String pgTransactionId,
            BillingAgreement agreement) {
        return subscriptionPaymentRepository.save(SubscriptionPayment.builder()
                .paymentOrder(order)
                .billingAgreement(agreement)
                .provider(order.getProvider())
                .user(order.getUser())
                .userSubscription(userSubscription)
                .subscription(order.getSubscription())
                .billingCycle(order.getBillingCycle())
                .amount(order.getAmount())
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId(pgTransactionId)
                .build());
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

    private LocalDate expiresAt(LocalDate startedAt, BillingCycle billingCycle) {
        return billingCycle == BillingCycle.MONTHLY
                ? startedAt.plusMonths(1)
                : startedAt.plusYears(1);
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

    private BillingAgreementConfirmResponse toConfirmResponse(
            PaymentOrder order,
            BillingAgreement agreement) {
        UserSubscriptionResponse subscription = order.getUserSubscription() == null
                ? null
                : UserSubscriptionResponse.from(order.getUserSubscription());
        return new BillingAgreementConfirmResponse(
                order.getOrderId(),
                order.getStatus(),
                agreement.getProvider(),
                agreement.getStatus(),
                agreement.getNextBillingAt(),
                subscription);
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

    private String orderName(PaymentOrder order) {
        return order.getSubscription().getName() + " recurring subscription";
    }

    private String firstPresent(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
