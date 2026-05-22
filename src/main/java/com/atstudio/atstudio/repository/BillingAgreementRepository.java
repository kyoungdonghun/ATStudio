package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillingAgreementRepository extends JpaRepository<BillingAgreement, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<BillingAgreement> findByUserAndProvider(User user, PaymentProviderType provider);

    @EntityGraph(attributePaths = {"user"})
    Optional<BillingAgreement> findByProviderAndProviderCustomerKey(
            PaymentProviderType provider,
            String providerCustomerKey);

    @EntityGraph(attributePaths = {"user"})
    List<BillingAgreement> findByStatusAndNextBillingAtLessThanEqual(
            BillingAgreementStatus status,
            LocalDate nextBillingAt);

    @EntityGraph(attributePaths = {"user"})
    Page<BillingAgreement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<BillingAgreement> findByStatus(BillingAgreementStatus status);
}
