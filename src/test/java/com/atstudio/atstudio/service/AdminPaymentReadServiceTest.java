package com.atstudio.atstudio.service;

import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOperationAuditLogRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentReceiptRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPaymentReadService unit tests")
class AdminPaymentReadServiceTest {

    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock PaymentReceiptRepository paymentReceiptRepository;
    @Mock PaymentOperationAuditLogRepository paymentOperationAuditLogRepository;
    @Mock PaymentReconciliationService paymentReconciliationService;

    @InjectMocks AdminPaymentReadService service;

    @Test
    @DisplayName("ADMIN reconciliation GET data path uses observations only")
    void reconcilePayments_usesReadOnlyDiagnostics() {
        PaymentReconciliationService.ReconciliationResult local =
                new PaymentReconciliationService.ReconciliationResult(
                        0, 0, 0, 0, 0, false, List.of());
        PaymentReconciliationService.ProviderReconciliationResult provider =
                new PaymentReconciliationService.ProviderReconciliationResult(
                        1, 0, 0, 0, 1, 0, 0, 0, 1, false, List.of());
        given(paymentReconciliationService.reconcileLocalLedger()).willReturn(local);
        given(paymentReconciliationService.diagnoseProviderLedger()).willReturn(provider);

        var response = service.reconcilePayments();

        assertThat(response.getData().providerLedger().providerDoneWithoutLocalFinalization())
                .isEqualTo(1);
        verify(paymentReconciliationService).reconcileLocalLedger();
        verify(paymentReconciliationService).diagnoseProviderLedger();
        verify(paymentReconciliationService, never()).reconcileProviderLedger();
    }

    @Test
    @DisplayName("ADMIN reconciliation suspends the read transaction before Provider observation")
    void reconcilePayments_suspendsAmbientTransaction() throws Exception {
        Method method = AdminPaymentReadService.class.getMethod("reconcilePayments");
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional.readOnly()).isTrue();
        assertThat(transactional.propagation()).isEqualTo(Propagation.NOT_SUPPORTED);
    }
}
