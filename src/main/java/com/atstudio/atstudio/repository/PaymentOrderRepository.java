package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    Optional<PaymentOrder> findFirstByBillingAgreementAndPurposeAndStatusInOrderByCreatedAtDesc(
            BillingAgreement billingAgreement,
            PaymentPurpose purpose,
            Collection<PaymentOrderStatus> statuses);
}
