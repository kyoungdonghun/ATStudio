package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentReconciliationResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundCreateRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementIgnoreRequest;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionService;
import com.atstudio.atstudio.service.AdminPaymentIncidentService;
import com.atstudio.atstudio.service.AdminPaymentReadService;
import com.atstudio.atstudio.service.AdminPaymentRefundService;
import com.atstudio.atstudio.service.AdminPaymentSettlementService;
import com.atstudio.atstudio.service.PaymentReconciliationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("AdminPaymentController HTTP contracts")
class AdminPaymentControllerTest {

    private static final String RAW_PROVIDER_TRANSACTION_SENTINEL =
            "RAW-PROVIDER-TRANSACTION-SENTINEL-CONTROLLER-4B92D1";

    @Autowired MockMvc mockMvc;
    @MockitoBean AdminPaymentReadService adminPaymentReadService;
    @MockitoBean AdminPaymentIncidentService adminPaymentIncidentService;
    @MockitoBean AdminPaymentRefundService adminPaymentRefundService;
    @MockitoBean AdminPaymentEntitlementCorrectionService adminPaymentEntitlementCorrectionService;
    @MockitoBean AdminPaymentSettlementService adminPaymentSettlementService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    void readEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/payments/orders"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminPaymentReadService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void readEndpointRejectsNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/payments/orders"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminPaymentReadService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void readEndpointForwardsPageContract() throws Exception {
        mockMvc.perform(get("/api/admin/payments/orders")
                        .param("page", "3")
                        .param("size", "40"))
                .andExpect(status().isOk());

        verify(adminPaymentReadService).listPaymentOrders(3, 40);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reconciliationSerializesSupportReferenceWithoutRawProviderIdentifier() throws Exception {
        PaymentReconciliationService.ReconciliationResult local =
                new PaymentReconciliationService.ReconciliationResult(
                        24, 12, 0, 0, 0, false, List.of());
        PaymentReconciliationService.ProviderReconciliationIssue rawIssue =
                new PaymentReconciliationService.ProviderReconciliationIssue(
                        PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED,
                        3001L,
                        12L,
                        10L,
                        "ATS-REN-20260716-CONTROLLER",
                        PaymentProviderType.TOSS,
                        PaymentPurpose.RENEWAL,
                        "IN_PROGRESS",
                        "DONE",
                        BigDecimal.valueOf(9900),
                        BigDecimal.valueOf(9900),
                        "KRW",
                        "KRW",
                        RAW_PROVIDER_TRANSACTION_SENTINEL,
                        null,
                        null);
        PaymentReconciliationService.ProviderReconciliationResult provider =
                new PaymentReconciliationService.ProviderReconciliationResult(
                        18, 1, 2, 3, 4, 5, 6, 7, 8, true, List.of(rawIssue));
        AdminPaymentReconciliationResponse response =
                AdminPaymentReconciliationResponse.from(local, provider);
        String expectedReference = response.providerLedger().issues().get(0).providerReference();
        given(adminPaymentReadService.reconcilePayments())
                .willReturn(ResponseDTO.<AdminPaymentReconciliationResponse>builder()
                        .data(response)
                        .build());

        String json = mockMvc.perform(get("/api/admin/payments/reconciliation"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.providerLedger.totalIssues").value(8))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.providerLedger.issueDetailsTruncated").value(true))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.providerLedger.issues[0].providerReference").value(expectedReference))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(expectedReference)
                .startsWith("REF-")
                .doesNotContain(RAW_PROVIDER_TRANSACTION_SENTINEL);
        assertThat(json)
                .doesNotContain("providerTransactionId")
                .doesNotContain(RAW_PROVIDER_TRANSACTION_SENTINEL);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void refundCreateForwardsLocalPaymentIdAndValidatedBody() throws Exception {
        mockMvc.perform(post("/api/admin/payments/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "subscriptionPaymentId":41,
                                  "amount":9900,
                                  "reasonCode":"CUSTOMER_REQUEST",
                                  "reasonNote":"ticket 123"
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<AdminPaymentRefundCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(AdminPaymentRefundCreateRequest.class);
        verify(adminPaymentRefundService).createRefund(org.mockito.ArgumentMatchers.isNull(), requestCaptor.capture());
        AdminPaymentRefundCreateRequest request = requestCaptor.getValue();
        assertThat(request.subscriptionPaymentId()).isEqualTo(41L);
        assertThat(request.amount()).isEqualByComparingTo(BigDecimal.valueOf(9900));
        assertThat(request.reasonNote()).isEqualTo("ticket 123");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void correctionPreviewForwardsRefundAndTargetSubscriptionIds() throws Exception {
        mockMvc.perform(post("/api/admin/payments/entitlement-correction-preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentRefundId":51,
                                  "targetSubscriptionId":20,
                                  "targetBillingCycle":"MONTHLY",
                                  "targetStatus":"EXPIRED",
                                  "targetExpiresAt":"2026-07-16",
                                  "clearPendingChange":true,
                                  "cancelBillingAgreement":true,
                                  "reasonNote":"refund incident 51"
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<AdminPaymentEntitlementCorrectionRequest> requestCaptor =
                ArgumentCaptor.forClass(AdminPaymentEntitlementCorrectionRequest.class);
        verify(adminPaymentEntitlementCorrectionService).previewCorrection(requestCaptor.capture());
        AdminPaymentEntitlementCorrectionRequest request = requestCaptor.getValue();
        assertThat(request.paymentRefundId()).isEqualTo(51L);
        assertThat(request.targetSubscriptionId()).isEqualTo(20L);
        assertThat(request.targetExpiresAt()).isEqualTo(LocalDate.of(2026, 7, 16));
        assertThat(request.clearPendingChange()).isTrue();
        assertThat(request.cancelBillingAgreement()).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void settlementIgnoreForwardsPathIdAndOperatorNote() throws Exception {
        mockMvc.perform(put("/api/admin/payments/settlements/71/ignore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"bank holiday evidence\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<AdminPaymentSettlementIgnoreRequest> requestCaptor =
                ArgumentCaptor.forClass(AdminPaymentSettlementIgnoreRequest.class);
        verify(adminPaymentSettlementService).ignoreSettlement(
                org.mockito.ArgumentMatchers.eq(71L),
                org.mockito.ArgumentMatchers.isNull(),
                requestCaptor.capture());
        assertThat(requestCaptor.getValue().note()).isEqualTo("bank holiday evidence");
    }
}
