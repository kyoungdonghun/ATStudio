package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentReceipt;
import com.atstudio.atstudio.entity.enums.PaymentReceiptType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentReceiptRepository extends JpaRepository<PaymentReceipt, Long> {

    boolean existsByPaymentOrderAndType(PaymentOrder paymentOrder, PaymentReceiptType type);

    @EntityGraph(attributePaths = {"user", "paymentOrder", "subscriptionPayment"})
    Page<PaymentReceipt> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
