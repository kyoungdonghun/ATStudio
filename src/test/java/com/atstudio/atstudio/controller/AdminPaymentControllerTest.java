package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentReconciliationResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundCreateRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementIgnoreRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportAttemptResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportErrorResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportResponse;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionService;
import com.atstudio.atstudio.service.AdminPaymentIncidentService;
import com.atstudio.atstudio.service.AdminPaymentReadService;
import com.atstudio.atstudio.service.AdminPaymentRefundService;
import com.atstudio.atstudio.service.AdminPaymentSettlementService;
import com.atstudio.atstudio.service.PaymentReconciliationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
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
    void settlementImportAttemptEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/payments/settlement-import-attempts"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/payments/settlement-import-attempts/41"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/payments/settlement-import-attempts/recovery")
                        .header("Idempotency-Key", "11111111-1111-4111-8111-111111111111"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminPaymentSettlementService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void settlementImportAttemptEndpointsRejectNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/payments/settlement-import-attempts"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/payments/settlement-import-attempts/41"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/payments/settlement-import-attempts/recovery")
                        .header("Idempotency-Key", "11111111-1111-4111-8111-111111111111"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminPaymentSettlementService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void settlementImportAttemptListAndNumericDetailForwardExactContract() throws Exception {
        given(adminPaymentSettlementService.listImportAttempts(2, 10))
                .willReturn(ResponseDTO.<AdminPaymentSettlementImportAttemptResponse>builder().build());
        given(adminPaymentSettlementService.getImportAttempt(41L))
                .willReturn(ResponseDTO.<AdminPaymentSettlementImportAttemptResponse>builder().build());

        mockMvc.perform(get("/api/admin/payments/settlement-import-attempts")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/payments/settlement-import-attempts/41"))
                .andExpect(status().isOk());

        verify(adminPaymentSettlementService).listImportAttempts(2, 10);
        verify(adminPaymentSettlementService).getImportAttempt(41L);
    }

    @Test
    void settlementImportAndRecoveryForwardTheSameHeaderWithoutPuttingItInTheUrl(CapturedOutput output)
            throws Exception {
        CustomUserDetails admin = adminActor();
        String operationKey = "11111111-1111-4111-8111-111111111111";
        String operatorNote = "OPERATOR-NOTE-SENTINEL-CONTROLLER-WI-056";
        String recoveryPath = "/api/admin/payments/settlement-import-attempts/recovery";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "settlements.csv",
                "text/csv",
                "provider,order_id,gross_amount,net_settlement_amount,settlement_base_date\n"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        MockPart notePart = new MockPart(
                "note",
                operatorNote.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        notePart.getHeaders().setContentType(MediaType.TEXT_PLAIN);
        given(adminPaymentSettlementService.importSettlements(admin, file, operatorNote, operationKey))
                .willReturn(ResponseDTO.<AdminPaymentSettlementImportResponse>builder().build());
        given(adminPaymentSettlementService.recoverImportAttempt(admin, operationKey))
                .willReturn(ResponseDTO.<AdminPaymentSettlementImportAttemptResponse>builder().build());

        MvcResult importResult = mockMvc.perform(multipart("/api/admin/payments/settlements/import")
                        .file(file)
                        .part(notePart)
                        .header("Idempotency-Key", operationKey)
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult recoveryResult = mockMvc.perform(get(recoveryPath)
                        .header("Idempotency-Key", operationKey)
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(importResult.getRequest().getRequestURI())
                .isEqualTo("/api/admin/payments/settlements/import")
                .doesNotContain(operatorNote);
        assertThat(importResult.getRequest().getQueryString()).isNull();
        assertThat(recoveryResult.getRequest().getRequestURI())
                .isEqualTo(recoveryPath)
                .doesNotContain(operationKey);
        assertThat(recoveryResult.getRequest().getPathInfo())
                .isEqualTo(recoveryPath)
                .doesNotContain(operationKey);
        assertThat(recoveryResult.getRequest().getQueryString()).isNull();
        assertThat(output.getAll()).doesNotContain(operationKey, operatorNote);
        verify(adminPaymentSettlementService).importSettlements(admin, file, operatorNote, operationKey);
        verify(adminPaymentSettlementService).recoverImportAttempt(admin, operationKey);
    }

    @Test
    void settlementImportDoesNotBindTheNoteFromAQueryParameter() throws Exception {
        CustomUserDetails admin = adminActor();
        String operationKey = "11111111-1111-4111-8111-111111111111";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "settlements.csv",
                "text/csv",
                "provider,order_id,gross_amount,net_settlement_amount,settlement_base_date\n"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        given(adminPaymentSettlementService.importSettlements(admin, file, null, operationKey))
                .willReturn(ResponseDTO.<AdminPaymentSettlementImportResponse>builder().build());

        MvcResult result = mockMvc.perform(multipart("/api/admin/payments/settlements/import")
                        .file(file)
                        .queryParam("note", "query-note-must-not-bind")
                        .header("Idempotency-Key", operationKey)
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getRequest().getQueryString()).contains("note=query-note-must-not-bind");
        verify(adminPaymentSettlementService).importSettlements(admin, file, null, operationKey);
    }

    @Test
    void settlementReconciliationSerializesAggregatesErrorsAndOmittedErrorCount() throws Exception {
        CustomUserDetails admin = adminActor();
        AdminPaymentSettlementImportResponse response = new AdminPaymentSettlementImportResponse(
                "ATS-SETTLEMENT-RECONCILE-CONTRACT",
                3,
                1,
                0,
                2,
                Map.of("MATCHED", 1),
                List.of(new AdminPaymentSettlementImportErrorResponse(2, "bounded row error")),
                1);
        given(adminPaymentSettlementService.reconcileMissingProviderSettlements(admin, null))
                .willReturn(ResponseDTO.<AdminPaymentSettlementImportResponse>builder()
                        .data(response)
                        .build());

        mockMvc.perform(post("/api/admin/payments/settlements/reconcile")
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.importBatchKey").value("ATS-SETTLEMENT-RECONCILE-CONTRACT"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.totalRows").value(3))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.importedRows").value(1))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.skippedDuplicateRows").value(0))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.failedRows").value(2))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.statusCounts.MATCHED").value(1))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.errors[0].rowNumber").value(2))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.errors[0].message").value("bounded row error"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.data.omittedErrorCount").value(1));

        verify(adminPaymentSettlementService).reconcileMissingProviderSettlements(admin, null);
    }

    @Test
    void recoveryDetailEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/payments/refunds/51"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/payments/entitlement-corrections/61"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminPaymentRefundService, adminPaymentEntitlementCorrectionService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void recoveryDetailEndpointsRejectNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/payments/refunds/51"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/payments/entitlement-corrections/61"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminPaymentRefundService, adminPaymentEntitlementCorrectionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void recoveryDetailEndpointsForwardExactLocalIds() throws Exception {
        mockMvc.perform(get("/api/admin/payments/refunds/51"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/payments/entitlement-corrections/61"))
                .andExpect(status().isOk());

        verify(adminPaymentRefundService).getRefund(51L);
        verify(adminPaymentEntitlementCorrectionService).getCorrection(61L);
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
    void settlementIgnoreForwardsPathIdAndNormalizedOperatorNoteOnce() throws Exception {
        CustomUserDetails actor = CustomUserDetails.builder()
                .id(99L)
                .email("settlement-admin@test.com")
                .role(UserRole.ADMIN)
                .build();

        mockMvc.perform(put("/api/admin/payments/settlements/71/ignore")
                        .with(user(actor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"  bank holiday evidence  \"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<AdminPaymentSettlementIgnoreRequest> requestCaptor =
                ArgumentCaptor.forClass(AdminPaymentSettlementIgnoreRequest.class);
        verify(adminPaymentSettlementService, times(1)).ignoreSettlement(
                org.mockito.ArgumentMatchers.eq(71L),
                org.mockito.ArgumentMatchers.same(actor),
                requestCaptor.capture());
        assertThat(requestCaptor.getValue().note()).isEqualTo("bank holiday evidence");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void settlementIgnoreRejectsMissingNullBlankAndTrimmedOverLimitNotes() throws Exception {
        List<String> invalidBodies = List.of(
                "{}",
                "{\"note\":null}",
                "{\"note\":\"\"}",
                "{\"note\":\"   \"}",
                "{\"note\":\" " + "a".repeat(501) + " \"}");

        for (String body : invalidBodies) {
            mockMvc.perform(put("/api/admin/payments/settlements/71/ignore")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        verifyNoInteractions(adminPaymentSettlementService);
    }

    @Test
    void settlementIgnoreRequiresAuthentication() throws Exception {
        mockMvc.perform(put("/api/admin/payments/settlements/71/ignore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"reviewed\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminPaymentSettlementService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void settlementIgnoreRejectsNonAdmin() throws Exception {
        mockMvc.perform(put("/api/admin/payments/settlements/71/ignore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"reviewed\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminPaymentSettlementService);
    }

    private CustomUserDetails adminActor() {
        return CustomUserDetails.builder()
                .id(99L)
                .email("settlement-import-admin@test.com")
                .role(UserRole.ADMIN)
                .build();
    }
}
