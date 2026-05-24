package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.service.payment.provider.recurring.PaymentStatusLookupProvider;
import com.atstudio.atstudio.service.payment.provider.recurring.ProviderPaymentLookupResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReconciliationService {

    private static final EnumSet<PaymentPurpose> FINAL_PAYMENT_PURPOSES = EnumSet.of(
            PaymentPurpose.SUBSCRIBE,
            PaymentPurpose.UPGRADE,
            PaymentPurpose.RENEWAL
    );

    private final PaymentOrderRepository paymentOrderRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final List<PaymentStatusLookupProvider> paymentStatusLookupProviders;
    private final PaymentReconciliationIncidentService paymentReconciliationIncidentService;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void reconcilePaymentLedgersOnSchedule() {
        ReconciliationResult local = reconcileLocalLedger();
        ProviderReconciliationResult provider = reconcileProviderLedger();
        paymentReconciliationIncidentService.recordIssues(local, provider);
    }

    @Transactional(readOnly = true)
    public ReconciliationResult reconcileLocalLedger() {
        List<PaymentOrder> recentOrders = paymentOrderRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 100))
                .getContent();
        int doneOrdersWithoutPayment = 0;
        List<LocalReconciliationIssue> issues = new java.util.ArrayList<>();

        for (PaymentOrder order : recentOrders) {
            if (order.getStatus() == PaymentOrderStatus.DONE
                    && FINAL_PAYMENT_PURPOSES.contains(order.getPurpose())
                    && !subscriptionPaymentRepository.existsByPaymentOrder(order)) {
                doneOrdersWithoutPayment++;
                issues.add(localIssue(order, PaymentReconciliationIssueType.DONE_ORDER_WITHOUT_PAYMENT));
                log.warn("Payment ledger mismatch: DONE order has no subscription payment. orderId={}",
                        order.getOrderId());
            }
        }

        List<BillingAgreement> activeAgreements =
                billingAgreementRepository.findByStatus(BillingAgreementStatus.ACTIVE);
        int activeAgreementsWithoutSubscription = 0;
        LocalDate today = LocalDate.now();

        for (BillingAgreement agreement : activeAgreements) {
            boolean hasActiveSubscription = userSubscriptionRepository
                    .findActiveByUser(agreement.getUser(), today)
                    .map(UserSubscription::getId)
                    .isPresent();
            if (!hasActiveSubscription) {
                activeAgreementsWithoutSubscription++;
                issues.add(localIssue(agreement));
                log.warn("Payment ledger mismatch: ACTIVE billing agreement has no active subscription. agreementId={}",
                        agreement.getId());
            }
        }

        ReconciliationResult result = new ReconciliationResult(
                recentOrders.size(),
                activeAgreements.size(),
                doneOrdersWithoutPayment,
                activeAgreementsWithoutSubscription,
                List.copyOf(issues));
        if (result.hasMismatch()) {
            log.warn("Payment reconciliation completed with mismatches: {}", result);
        } else {
            log.info("Payment reconciliation completed: {}", result);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ProviderReconciliationResult reconcileProviderLedger() {
        List<PaymentOrder> recentOrders = paymentOrderRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 100))
                .getContent()
                .stream()
                .filter(order -> FINAL_PAYMENT_PURPOSES.contains(order.getPurpose()))
                .toList();

        int skipped = 0;
        int providerNotFound = 0;
        int lookupFailures = 0;
        int providerDoneWithoutLocalFinalization = 0;
        int localDoneButProviderNotDone = 0;
        int amountMismatches = 0;
        List<ProviderReconciliationIssue> issues = new java.util.ArrayList<>();

        for (PaymentOrder order : recentOrders) {
            Optional<PaymentStatusLookupProvider> lookupProvider = lookupProvider(order);
            if (lookupProvider.isEmpty()) {
                skipped++;
                continue;
            }

            ProviderPaymentLookupResult providerResult =
                    lookupProvider.get().findPaymentByOrderId(order.getOrderId());
            if (!providerResult.found()) {
                if (providerResult.lookupFailure()) {
                    lookupFailures++;
                    issues.add(issue(order, providerResult, PaymentReconciliationIssueType.PROVIDER_LOOKUP_FAILED));
                } else {
                    providerNotFound++;
                    if (order.getStatus() == PaymentOrderStatus.DONE) {
                        issues.add(issue(
                                order,
                                providerResult,
                                PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_FOUND));
                    }
                }
                continue;
            }

            if (amountMismatch(order, providerResult)) {
                amountMismatches++;
                issues.add(issue(order, providerResult, PaymentReconciliationIssueType.AMOUNT_MISMATCH));
            }

            if (providerResult.providerDone() && order.getStatus() != PaymentOrderStatus.DONE) {
                providerDoneWithoutLocalFinalization++;
                issues.add(issue(
                        order,
                        providerResult,
                        PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED));
                log.warn(
                        "Payment provider mismatch: provider DONE but local order is not finalized. orderId={}, localStatus={}, providerStatus={}",
                        order.getOrderId(),
                        order.getStatus(),
                        providerResult.status());
            }

            if (!providerResult.providerDone() && order.getStatus() == PaymentOrderStatus.DONE) {
                localDoneButProviderNotDone++;
                issues.add(issue(
                        order,
                        providerResult,
                        PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE));
                log.warn(
                        "Payment provider mismatch: local DONE but provider is not DONE. orderId={}, providerStatus={}",
                        order.getOrderId(),
                        providerResult.status());
            }
        }

        ProviderReconciliationResult result = new ProviderReconciliationResult(
                recentOrders.size(),
                skipped,
                providerNotFound,
                lookupFailures,
                providerDoneWithoutLocalFinalization,
                localDoneButProviderNotDone,
                amountMismatches,
                List.copyOf(issues));
        if (result.hasMismatch()) {
            log.warn("Payment provider reconciliation completed with mismatches: {}", result);
        } else {
            log.info("Payment provider reconciliation completed: {}", result);
        }
        return result;
    }

    private Optional<PaymentStatusLookupProvider> lookupProvider(PaymentOrder order) {
        return paymentStatusLookupProviders.stream()
                .filter(provider -> provider.getProviderType() == order.getProvider())
                .filter(PaymentStatusLookupProvider::isLookupConfigured)
                .findFirst();
    }

    private boolean amountMismatch(PaymentOrder order, ProviderPaymentLookupResult providerResult) {
        BigDecimal providerAmount = providerResult.totalAmount();
        return providerAmount != null && order.getAmount().compareTo(providerAmount) != 0;
    }

    private ProviderReconciliationIssue issue(
            PaymentOrder order,
            ProviderPaymentLookupResult providerResult,
            PaymentReconciliationIssueType issueType) {
        return new ProviderReconciliationIssue(
                issueType,
                order.getId(),
                order.getUser().getId(),
                order.getBillingAgreement() == null ? null : order.getBillingAgreement().getId(),
                order.getOrderId(),
                order.getProvider(),
                order.getPurpose(),
                order.getStatus().name(),
                providerResult.status(),
                order.getAmount(),
                providerResult.totalAmount(),
                providerResult.transactionId(),
                providerResult.failureCode(),
                providerResult.failureMessage());
    }

    private LocalReconciliationIssue localIssue(
            PaymentOrder order,
            PaymentReconciliationIssueType issueType) {
        return new LocalReconciliationIssue(
                issueType,
                order.getId(),
                order.getUser().getId(),
                order.getBillingAgreement() == null ? null : order.getBillingAgreement().getId(),
                order.getOrderId(),
                order.getProvider(),
                order.getPurpose(),
                order.getStatus().name(),
                order.getAmount());
    }

    private LocalReconciliationIssue localIssue(BillingAgreement agreement) {
        return new LocalReconciliationIssue(
                PaymentReconciliationIssueType.ACTIVE_AGREEMENT_WITHOUT_SUBSCRIPTION,
                null,
                agreement.getUser().getId(),
                agreement.getId(),
                null,
                agreement.getProvider(),
                null,
                agreement.getStatus().name(),
                null);
    }

    public record ReconciliationResult(
            int checkedOrders,
            int checkedBillingAgreements,
            int doneOrdersWithoutPayment,
            int activeAgreementsWithoutSubscription,
            List<LocalReconciliationIssue> issues) {
        public boolean hasMismatch() {
            return doneOrdersWithoutPayment > 0 || activeAgreementsWithoutSubscription > 0;
        }
    }

    public record ProviderReconciliationResult(
            int checkedOrders,
            int skippedOrders,
            int providerNotFound,
            int lookupFailures,
            int providerDoneWithoutLocalFinalization,
            int localDoneButProviderNotDone,
            int amountMismatches,
            List<ProviderReconciliationIssue> issues) {
        public boolean hasMismatch() {
            return !issues.isEmpty();
        }
    }

    public record LocalReconciliationIssue(
            PaymentReconciliationIssueType issueType,
            Long paymentOrderId,
            Long userId,
            Long billingAgreementId,
            String orderId,
            PaymentProviderType provider,
            PaymentPurpose purpose,
            String localStatus,
            BigDecimal localAmount) {
    }

    public record ProviderReconciliationIssue(
            PaymentReconciliationIssueType issueType,
            Long paymentOrderId,
            Long userId,
            Long billingAgreementId,
            String orderId,
            PaymentProviderType provider,
            PaymentPurpose purpose,
            String localStatus,
            String providerStatus,
            BigDecimal localAmount,
            BigDecimal providerAmount,
            String providerTransactionId,
            String failureCode,
            String failureMessage) {
    }
}
