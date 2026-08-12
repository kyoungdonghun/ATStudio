package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareRequest;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.service.payment.billing.BillingCustomerKeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class BillingAgreementPrepareTransactionService {

    private static final PaymentProviderType PROVIDER = PaymentProviderType.TOSS;
    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int PAYMENT_EXPIRY_MINUTES = 15;

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final CompanyCertificationRepository companyCertificationRepository;
    private final BillingCustomerKeyGenerator billingCustomerKeyGenerator;

    public BillingAgreementPrepareTransactionService(
            UserRepository userRepository,
            SubscriptionRepository subscriptionRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            PaymentOrderRepository paymentOrderRepository,
            BillingAgreementRepository billingAgreementRepository,
            CompanyCertificationRepository companyCertificationRepository,
            BillingCustomerKeyGenerator billingCustomerKeyGenerator) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.billingAgreementRepository = billingAgreementRepository;
        this.companyCertificationRepository = companyCertificationRepository;
        this.billingCustomerKeyGenerator = billingCustomerKeyGenerator;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long ensureAgreement(
            Long userID,
            BillingAgreementPrepareRequest request,
            LocalDateTime now) {
        User user = findUser(userID);
        Optional<BillingAgreement> existing =
                billingAgreementRepository.findByUserAndProvider(user, PROVIDER);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        Subscription subscription = findSubscription(request.subscriptionId());
        UserSubscription activeSubscription = userSubscriptionRepository
                .findActiveByUser(user, now.toLocalDate())
                .orElse(null);
        PaymentPurpose authoritativePurpose = activeSubscription == null
                ? PaymentPurpose.SUBSCRIBE
                : PaymentPurpose.BILLING_AGREEMENT;
        validateAuthoritativeIntent(
                user, subscription, activeSubscription, authoritativePurpose, request);

        BillingAgreement created = billingAgreementRepository.saveAndFlush(BillingAgreement.builder()
                .user(user)
                .provider(PROVIDER)
                .providerCustomerKey(billingCustomerKeyGenerator.generate())
                .build());
        return created.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PrepareClaim claim(
            Long userID,
            BillingAgreementPrepareRequest request,
            String commandKey,
            LocalDateTime now) {
        User user = findUser(userID);
        BillingAgreement agreement = billingAgreementRepository
                .findByUserIDAndProviderForUpdate(userID, PROVIDER)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND));
        Subscription subscription = findSubscription(request.subscriptionId());
        UserSubscription activeSubscription = userSubscriptionRepository
                .findActiveByUserForUpdate(user, now.toLocalDate())
                .orElse(null);
        PaymentPurpose authoritativePurpose = activeSubscription == null
                ? PaymentPurpose.SUBSCRIBE
                : PaymentPurpose.BILLING_AGREEMENT;

        Optional<PaymentOrder> existing = paymentOrderRepository.findByCommandKeyForUpdate(commandKey);
        if (existing.isPresent()) {
            PaymentOrder order = existing.get();
            validateReusableOrder(
                    order,
                    user,
                    subscription,
                    activeSubscription,
                    authoritativePurpose,
                    request,
                    agreement,
                    now);
            return toClaim(order, agreement);
        }

        validateAuthoritativeIntent(user, subscription, activeSubscription, authoritativePurpose, request);
        prepareAgreementForNewAttempt(agreement);
        BigDecimal amount = authoritativePurpose == PaymentPurpose.SUBSCRIBE
                ? priceFor(subscription, request.billingCycle())
                : BigDecimal.ZERO;
        PaymentOrder order = paymentOrderRepository.saveAndFlush(PaymentOrder.builder()
                .orderId(generateOrderId(now.toLocalDate()))
                .commandKey(commandKey)
                .user(user)
                .purpose(authoritativePurpose)
                .provider(PROVIDER)
                .subscription(subscription)
                .userSubscription(activeSubscription)
                .billingAgreement(agreement)
                .billingCycle(request.billingCycle())
                .amount(amount)
                .currency("KRW")
                .expiresAt(now.plusMinutes(PAYMENT_EXPIRY_MINUTES))
                .build());
        return toClaim(order, agreement);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PrepareClaim finalizeDescriptor(
            PrepareClaim expected,
            String providerPayload,
            LocalDateTime now) {
        User user = findUser(expected.userID());
        BillingAgreement agreement = billingAgreementRepository
                .findByUserIDAndProviderForUpdate(expected.userID(), PROVIDER)
                .orElseThrow(this::attemptConflict);
        UserSubscription activeSubscription = userSubscriptionRepository
                .findActiveByUserForUpdate(user, now.toLocalDate())
                .orElse(null);
        PaymentOrder order = paymentOrderRepository.findByCommandKeyForUpdate(expected.commandKey())
                .orElseThrow(this::attemptConflict);
        Subscription subscription = findSubscription(expected.subscriptionID());
        PaymentPurpose authoritativePurpose = activeSubscription == null
                ? PaymentPurpose.SUBSCRIBE
                : PaymentPurpose.BILLING_AGREEMENT;
        BillingAgreementPrepareRequest request = new BillingAgreementPrepareRequest(
                expected.subscriptionID(), expected.billingCycle(), expected.purpose());
        validateReusableOrder(
                order,
                user,
                subscription,
                activeSubscription,
                authoritativePurpose,
                request,
                agreement,
                now);
        if (!Objects.equals(agreement.getId(), expected.agreementID())
                || !Objects.equals(order.getOrderId(), expected.orderID())) {
            throw attemptConflict();
        }
        if (order.getStatus() == PaymentOrderStatus.READY) {
            order.markInProgress(providerPayload);
        } else if (!Objects.equals(order.getProviderPayload(), providerPayload)) {
            throw attemptConflict();
        }
        return toClaim(order, agreement);
    }

    private void validateReusableOrder(
            PaymentOrder order,
            User user,
            Subscription subscription,
            UserSubscription activeSubscription,
            PaymentPurpose authoritativePurpose,
            BillingAgreementPrepareRequest request,
            BillingAgreement agreement,
            LocalDateTime now) {
        if (!order.isOwnedBy(user)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }

        boolean exactTuple = order.getProvider() == PROVIDER
                && order.getPurpose() == request.purpose()
                && order.getPurpose() == authoritativePurpose
                && Objects.equals(order.getSubscription().getId(), subscription.getId())
                && order.getSubscription().getUserType() == subscription.getUserType()
                && order.getBillingCycle() == request.billingCycle()
                && Objects.equals(order.getBillingAgreement().getId(), agreement.getId())
                && Objects.equals(
                        order.getUserSubscription() == null ? null : order.getUserSubscription().getId(),
                        activeSubscription == null ? null : activeSubscription.getId());
        if (!exactTuple) {
            throw attemptConflict();
        }

        validateAuthoritativeIntent(user, subscription, activeSubscription, authoritativePurpose, request);

        if (order.getStatus() == PaymentOrderStatus.EXPIRED || order.isExpired(now)) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_EXPIRED);
        }

        switch (order.getStatus()) {
            case READY, IN_PROGRESS -> validateAgreementForReuse(agreement);
            case FAILED, CANCELLED -> {
                validateAgreementForNewAttempt(agreement);
                throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_TERMINAL);
            }
            default -> throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private void validateAuthoritativeIntent(
            User user,
            Subscription subscription,
            UserSubscription activeSubscription,
            PaymentPurpose authoritativePurpose,
            BillingAgreementPrepareRequest request) {
        if (subscription.getUserType() != user.getUserType()) {
            throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH);
        }
        if (request.purpose() != authoritativePurpose) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_PURPOSE_MISMATCH);
        }
        if (activeSubscription == null) {
            if (user.getUserType() == UserType.BUSINESS
                    && !companyCertificationRepository.existsByUserAndStatusIn(
                    user, List.of(CompanyCertificationStatus.APPROVED))) {
                throw new BusinessException(BUSINESS_ERROR.COMPANY_CERTIFICATION_REQUIRED);
            }
            return;
        }
        if (!Objects.equals(activeSubscription.getSubscription().getId(), subscription.getId())
                || activeSubscription.getSubscription().getUserType() != subscription.getUserType()
                || activeSubscription.getBillingCycle() != request.billingCycle()) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
    }

    private void prepareAgreementForNewAttempt(BillingAgreement agreement) {
        validateAgreementForNewAttempt(agreement);
        agreement.prepareRegistration(agreement.getProviderCustomerKey());
    }

    private void validateAgreementForReuse(BillingAgreement agreement) {
        if (agreement.getStatus() == BillingAgreementStatus.ACTIVE) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_ALREADY_ACTIVE);
        }
        if (agreement.getStatus() != BillingAgreementStatus.READY || hasIssuedBillingKey(agreement)) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
    }

    private void validateAgreementForNewAttempt(BillingAgreement agreement) {
        if (agreement.getStatus() == BillingAgreementStatus.ACTIVE) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_ALREADY_ACTIVE);
        }
        if (agreement.getStatus() == BillingAgreementStatus.READY && hasIssuedBillingKey(agreement)) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
    }

    private boolean hasIssuedBillingKey(BillingAgreement agreement) {
        return agreement.getBillingKeyCiphertext() != null
                && !agreement.getBillingKeyCiphertext().isBlank();
    }

    private PrepareClaim toClaim(PaymentOrder order, BillingAgreement agreement) {
        return new PrepareClaim(
                order.getUser().getId(),
                order.getSubscription().getId(),
                order.getBillingCycle(),
                order.getPurpose(),
                agreement.getId(),
                order.getOrderId(),
                order.getCommandKey(),
                agreement.getProviderCustomerKey(),
                order.getProvider(),
                agreement.getStatus(),
                order.getAmount(),
                order.getCurrency(),
                order.getExpiresAt());
    }

    private User findUser(Long userID) {
        return userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private Subscription findSubscription(Long subscriptionID) {
        return subscriptionRepository.findById(subscriptionID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
    }

    private BigDecimal priceFor(Subscription subscription, BillingCycle cycle) {
        return cycle == BillingCycle.MONTHLY
                ? subscription.getPriceMonthly()
                : subscription.getPriceYearly();
    }

    private String generateOrderId(LocalDate date) {
        String orderID;
        do {
            orderID = "ATS-BILL-" + date.format(ORDER_DATE) + "-"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (paymentOrderRepository.existsByOrderId(orderID));
        return orderID;
    }

    private BusinessException attemptConflict() {
        return new BusinessException(BUSINESS_ERROR.PAYMENT_PREPARE_ATTEMPT_CONFLICT);
    }

    public record PrepareClaim(
            Long userID,
            Long subscriptionID,
            BillingCycle billingCycle,
            PaymentPurpose purpose,
            Long agreementID,
            String orderID,
            String commandKey,
            String providerCustomerKey,
            PaymentProviderType provider,
            BillingAgreementStatus agreementStatus,
            BigDecimal amount,
            String currency,
            LocalDateTime expiresAt) {
    }
}
