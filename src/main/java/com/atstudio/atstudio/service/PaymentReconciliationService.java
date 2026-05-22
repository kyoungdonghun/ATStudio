package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

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

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional(readOnly = true)
    public void reconcileLocalLedgerOnSchedule() {
        reconcileLocalLedger();
    }

    @Transactional(readOnly = true)
    public ReconciliationResult reconcileLocalLedger() {
        List<PaymentOrder> recentOrders = paymentOrderRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 100))
                .getContent();
        int doneOrdersWithoutPayment = 0;

        for (PaymentOrder order : recentOrders) {
            if (order.getStatus() == PaymentOrderStatus.DONE
                    && FINAL_PAYMENT_PURPOSES.contains(order.getPurpose())
                    && !subscriptionPaymentRepository.existsByPaymentOrder(order)) {
                doneOrdersWithoutPayment++;
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
                log.warn("Payment ledger mismatch: ACTIVE billing agreement has no active subscription. agreementId={}",
                        agreement.getId());
            }
        }

        ReconciliationResult result = new ReconciliationResult(
                recentOrders.size(),
                activeAgreements.size(),
                doneOrdersWithoutPayment,
                activeAgreementsWithoutSubscription);
        if (result.hasMismatch()) {
            log.warn("Payment reconciliation completed with mismatches: {}", result);
        } else {
            log.info("Payment reconciliation completed: {}", result);
        }
        return result;
    }

    public record ReconciliationResult(
            int checkedOrders,
            int checkedBillingAgreements,
            int doneOrdersWithoutPayment,
            int activeAgreementsWithoutSubscription) {
        public boolean hasMismatch() {
            return doneOrdersWithoutPayment > 0 || activeAgreementsWithoutSubscription > 0;
        }
    }
}
