package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.PaymentCancelRequest;
import com.atstudio.atstudio.dto.payment.PaymentCheckoutResponse;
import com.atstudio.atstudio.dto.payment.PaymentConfirmRequest;
import com.atstudio.atstudio.dto.payment.PaymentConfirmResponse;
import com.atstudio.atstudio.dto.payment.PaymentOrderResponse;
import com.atstudio.atstudio.dto.payment.PaymentPrepareRequest;
import com.atstudio.atstudio.dto.payment.PaymentPrepareResponse;
import com.atstudio.atstudio.dto.subscription.UserSubscriptionResponse;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.provider.PaymentProvider;
import com.atstudio.atstudio.service.payment.provider.PaymentProviderConfirmResult;
import com.atstudio.atstudio.service.payment.provider.PaymentProviderPrepareResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PaymentApplicationService {

    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int PAYMENT_EXPIRY_MINUTES = 15;

    private final PaymentOrderRepository paymentOrderRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final CompanyCertificationRepository companyCertificationRepository;
    private final PlaylistService playlistService;
    private final Map<PaymentProviderType, PaymentProvider> providers;

    public PaymentApplicationService(
            PaymentOrderRepository paymentOrderRepository,
            UserRepository userRepository,
            SubscriptionRepository subscriptionRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            SubscriptionPaymentRepository subscriptionPaymentRepository,
            CompanyCertificationRepository companyCertificationRepository,
            PlaylistService playlistService,
            List<PaymentProvider> providers) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionPaymentRepository = subscriptionPaymentRepository;
        this.companyCertificationRepository = companyCertificationRepository;
        this.playlistService = playlistService;
        this.providers = providers.stream()
                .collect(Collectors.toUnmodifiableMap(PaymentProvider::getProviderType, Function.identity()));
    }

    @Transactional
    public PaymentPrepareResponse prepareSubscriptionPayment(
            CustomUserDetails userDetails,
            PaymentPrepareRequest request) {
        User user = findUser(userDetails);
        Subscription subscription = findSubscription(request.subscriptionId());
        validateSubscriptionUserType(user, subscription);

        PaymentPurpose purpose = request.purpose();
        if (purpose != PaymentPurpose.SUBSCRIBE && purpose != PaymentPurpose.UPGRADE) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        UserSubscription current = null;
        BigDecimal amount;
        if (purpose == PaymentPurpose.SUBSCRIBE) {
            validateSubscriptionPreconditions(user);
            amount = priceFor(subscription, request.billingCycle());
        } else {
            current = findActiveSubscription(user);
            if (!isUpgrade(current, subscription)) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
            }
            amount = calculateProratedUpgradeAmount(current, subscription, request.billingCycle());
        }

        PaymentProviderType providerType = PaymentProviderType.MOCK;
        PaymentOrder order = paymentOrderRepository.save(PaymentOrder.builder()
                .orderId(generateOrderId())
                .user(user)
                .purpose(purpose)
                .provider(providerType)
                .subscription(subscription)
                .userSubscription(current)
                .billingCycle(request.billingCycle())
                .amount(amount)
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusMinutes(PAYMENT_EXPIRY_MINUTES))
                .build());

        PaymentProviderPrepareResult providerResult = provider(providerType).prepare(order);

        return new PaymentPrepareResponse(
                order.getOrderId(),
                order.getProvider(),
                order.getPurpose(),
                order.getAmount(),
                order.getCurrency(),
                order.getExpiresAt(),
                new PaymentCheckoutResponse(providerResult.checkoutType(), providerResult.confirmToken())
        );
    }

    @Transactional
    public PaymentConfirmResponse confirmPayment(
            CustomUserDetails userDetails,
            PaymentConfirmRequest request) {
        User user = findUser(userDetails);
        PaymentOrder order = findOwnedOrder(user, request.orderId());

        if (order.getStatus() == PaymentOrderStatus.DONE) {
            return toConfirmResponse(order);
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
        if (order.getProvider() != request.provider()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        if (order.getAmount().compareTo(request.amount()) != 0) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_AMOUNT_MISMATCH);
        }

        PaymentProviderConfirmResult providerResult = provider(order.getProvider()).confirm(order, request);
        if (!providerResult.success()) {
            order.markFailed(providerResult.failureCode(), providerResult.failureMessage());
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_CONFIRM_FAILED);
        }

        UserSubscription appliedSubscription = applySubscriptionAction(order);
        saveSubscriptionPayment(order, appliedSubscription, providerResult.transactionId());
        order.markDone(providerResult.transactionId(), appliedSubscription, providerResult.providerPayload());

        return toConfirmResponse(order);
    }

    @Transactional
    public PaymentOrderResponse cancelPayment(CustomUserDetails userDetails, PaymentCancelRequest request) {
        User user = findUser(userDetails);
        PaymentOrder order = findOwnedOrder(user, request.orderId());

        if (order.getStatus() == PaymentOrderStatus.DONE) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        if (request.status() == PaymentOrderStatus.CANCELLED) {
            order.markCancelled(defaultReason(request.reason(), "Payment cancelled by user."));
        } else if (request.status() == PaymentOrderStatus.FAILED) {
            order.markFailed("MOCK_FAILED", defaultReason(request.reason(), "Mock payment failed."));
        } else {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }

        return new PaymentOrderResponse(order.getOrderId(), order.getStatus(), order.getPurpose());
    }

    private UserSubscription applySubscriptionAction(PaymentOrder order) {
        if (order.getPurpose() == PaymentPurpose.SUBSCRIBE) {
            validateSubscriptionPreconditions(order.getUser());

            LocalDate startedAt = LocalDate.now();
            UserSubscription userSubscription = userSubscriptionRepository.save(
                    UserSubscription.builder()
                            .user(order.getUser())
                            .subscription(order.getSubscription())
                            .billingCycle(order.getBillingCycle())
                            .startedAt(startedAt)
                            .expiresAt(expiresAt(startedAt, order.getBillingCycle()))
                            .build());

            playlistService.createDefaultPlaylist(order.getUser());
            return userSubscription;
        }

        if (order.getPurpose() == PaymentPurpose.UPGRADE) {
            UserSubscription current = findActiveSubscription(order.getUser());
            if (!isUpgrade(current, order.getSubscription())) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
            }

            LocalDate today = LocalDate.now();
            current.upgrade(order.getSubscription(), order.getBillingCycle(),
                    expiresAt(today, order.getBillingCycle()));
            return current;
        }

        throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
    }

    private SubscriptionPayment saveSubscriptionPayment(
            PaymentOrder order,
            UserSubscription userSubscription,
            String pgTransactionId) {
        return subscriptionPaymentRepository.save(SubscriptionPayment.builder()
                .paymentOrder(order)
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

    private PaymentConfirmResponse toConfirmResponse(PaymentOrder order) {
        UserSubscriptionResponse subscription = order.getUserSubscription() == null
                ? null
                : UserSubscriptionResponse.from(order.getUserSubscription());
        return new PaymentConfirmResponse(order.getOrderId(), order.getStatus(), order.getPurpose(), subscription);
    }

    private User findUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private Subscription findSubscription(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
    }

    private UserSubscription findActiveSubscription(User user) {
        return userSubscriptionRepository.findActiveByUser(user, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
    }

    private PaymentOrder findOwnedOrder(User user, String orderId) {
        PaymentOrder order = paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_NOT_FOUND));
        if (!order.isOwnedBy(user)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
        return order;
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

    private boolean isUpgrade(UserSubscription current, Subscription newPlan) {
        return newPlan.getPriceMonthly().compareTo(current.getSubscription().getPriceMonthly()) > 0;
    }

    private BigDecimal calculateProratedUpgradeAmount(
            UserSubscription current,
            Subscription newPlan,
            BillingCycle newBillingCycle) {
        LocalDate today = LocalDate.now();
        long remainingDays = ChronoUnit.DAYS.between(today, current.getExpiresAt());
        long totalDays = ChronoUnit.DAYS.between(current.getStartedAt(), current.getExpiresAt());

        BigDecimal currentPrice = priceFor(current.getSubscription(), current.getBillingCycle());
        BigDecimal refundAmount = totalDays > 0
                ? currentPrice.multiply(BigDecimal.valueOf(Math.max(0, remainingDays)))
                    .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal amount = priceFor(newPlan, newBillingCycle).subtract(refundAmount);
        return amount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : amount;
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

    private PaymentProvider provider(PaymentProviderType providerType) {
        PaymentProvider paymentProvider = providers.get(providerType);
        if (paymentProvider == null) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        return paymentProvider;
    }

    private String generateOrderId() {
        String orderId;
        do {
            orderId = "ATS-" + LocalDate.now().format(ORDER_DATE) + "-"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (paymentOrderRepository.existsByOrderId(orderId));
        return orderId;
    }

    private String defaultReason(String reason, String fallback) {
        return reason == null || reason.isBlank() ? fallback : reason;
    }
}
