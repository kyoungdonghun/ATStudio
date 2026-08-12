package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.dto.payment.BillingAgreementCheckoutResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementResponse;
import com.atstudio.atstudio.dto.payment.PaymentCommandOutcomeResponse;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.BillingAgreementApplicationService;
import com.atstudio.atstudio.service.PaymentRecoveryReadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("PaymentController billing agreement tests")
class PaymentControllerTest {

    private static final String PREPARE_KEY = "123e4567-e89b-42d3-a456-426614174000";

    @Autowired MockMvc mockMvc;
    @MockitoBean BillingAgreementApplicationService billingAgreementApplicationService;
    @MockitoBean PaymentRecoveryReadService paymentRecoveryReadService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/payments/billing-agreements/prepare - unauthenticated -> 401")
    void prepareBillingAgreement_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/payments/billing-agreements/prepare")
                        .header("Idempotency-Key", PREPARE_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":10,\"billingCycle\":\"MONTHLY\",\"purpose\":\"SUBSCRIBE\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/payments/billing-agreements/prepare - authenticated -> 201")
    void prepareBillingAgreement_success() throws Exception {
        given(billingAgreementApplicationService.prepareBillingAgreement(any(), any(), eq(PREPARE_KEY)))
                .willReturn(new BillingAgreementPrepareResponse(
                        "ORDER-1",
                        PaymentProviderType.TOSS,
                        PaymentPurpose.SUBSCRIBE,
                        BillingAgreementStatus.READY,
                        10L,
                        BillingCycle.MONTHLY,
                        BigDecimal.valueOf(9900),
                        "KRW",
                        LocalDateTime.now().plusMinutes(15),
                        new BillingAgreementCheckoutResponse(
                                "TOSS_BILLING_AUTH",
                                "test_ck",
                                "ats_billing_customer_1",
                                "http://localhost/success",
                                "http://localhost/fail",
                                "CARD")));

        mockMvc.perform(post("/api/payments/billing-agreements/prepare")
                        .header("Idempotency-Key", PREPARE_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":10,\"billingCycle\":\"MONTHLY\",\"purpose\":\"SUBSCRIBE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.provider").value("TOSS"))
                .andExpect(jsonPath("$.data.purpose").value("SUBSCRIBE"))
                .andExpect(jsonPath("$.data.checkout.customerKey").value("ats_billing_customer_1"));
    }

    @ParameterizedTest(name = "subscriptionId={0}")
    @ValueSource(longs = {0, -1})
    @WithMockUser(roles = "USER")
    @DisplayName("prepare rejects non-positive subscription ID before service invocation")
    void prepareBillingAgreement_nonPositiveSubscriptionId(long subscriptionId) throws Exception {
        mockMvc.perform(post("/api/payments/billing-agreements/prepare")
                        .header("Idempotency-Key", PREPARE_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"subscriptionId\":%d,\"billingCycle\":\"MONTHLY\","
                                + "\"purpose\":\"SUBSCRIBE\"}").formatted(subscriptionId)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(billingAgreementApplicationService);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("prepare requires Idempotency-Key before service invocation")
    void prepareBillingAgreement_missingIdempotencyKey() throws Exception {
        mockMvc.perform(post("/api/payments/billing-agreements/prepare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":10,\"billingCycle\":\"MONTHLY\",\"purpose\":\"SUBSCRIBE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID"));

        verifyNoInteractions(billingAgreementApplicationService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "not-a-uuid",
            "123E4567-E89B-42D3-A456-426614174000",
            "123e4567-e89b-12d3-a456-426614174000",
            "123e4567-e89b-42d3-c456-426614174000",
            "123e4567-e89b-42d3-a456-4266141740000",
            "123e4567-e89b-42d3-a456-426614174000\t"
    })
    @WithMockUser(roles = "USER")
    @DisplayName("prepare rejects blank, malformed, oversized, and control-character keys")
    void prepareBillingAgreement_invalidIdempotencyKey(String key) throws Exception {
        mockMvc.perform(post("/api/payments/billing-agreements/prepare")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":10,\"billingCycle\":\"MONTHLY\",\"purpose\":\"SUBSCRIBE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID"));

        verifyNoInteractions(billingAgreementApplicationService);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("prepare rejects missing purpose before service invocation")
    void prepareBillingAgreement_missingPurpose() throws Exception {
        mockMvc.perform(post("/api/payments/billing-agreements/prepare")
                        .header("Idempotency-Key", PREPARE_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":10,\"billingCycle\":\"MONTHLY\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(billingAgreementApplicationService);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("prepare rejects malformed purpose before service invocation")
    void prepareBillingAgreement_malformedPurpose() throws Exception {
        mockMvc.perform(post("/api/payments/billing-agreements/prepare")
                        .header("Idempotency-Key", PREPARE_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":10,\"billingCycle\":\"MONTHLY\",\"purpose\":\"NOT_A_PURPOSE\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(billingAgreementApplicationService);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("prepare rejects UPGRADE purpose before service invocation")
    void prepareBillingAgreement_upgradePurpose() throws Exception {
        mockMvc.perform(post("/api/payments/billing-agreements/prepare")
                        .header("Idempotency-Key", PREPARE_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":10,\"billingCycle\":\"MONTHLY\",\"purpose\":\"UPGRADE\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(billingAgreementApplicationService);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/payments/billing-agreements/confirm - authenticated -> 200")
    void confirmBillingAgreement_success() throws Exception {
        given(billingAgreementApplicationService.confirmBillingAgreement(any(), any()))
                .willReturn(new BillingAgreementConfirmResponse(
                        "ORDER-1",
                        PaymentOrderStatus.DONE,
                        PaymentProviderType.TOSS,
                        BillingAgreementStatus.ACTIVE,
                        LocalDate.now().plusMonths(1),
                        null));

        mockMvc.perform(post("/api/payments/billing-agreements/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId":"ORDER-1",
                                  "authKey":"auth_key",
                                  "customerKey":"ats_billing_customer_1",
                                  "amount":9900
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderStatus").value("DONE"))
                .andExpect(jsonPath("$.data.billingKey").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/payments/billing-agreements/me - authenticated -> 200")
    void getMyBillingAgreement_success() throws Exception {
        given(billingAgreementApplicationService.getMyBillingAgreement(any()))
                .willReturn(new BillingAgreementResponse(
                        PaymentProviderType.TOSS,
                        BillingAgreementStatus.ACTIVE,
                        "CARD",
                        "1234",
                        LocalDate.now().plusMonths(1),
                        LocalDateTime.now(),
                        null,
                        null));

        mockMvc.perform(get("/api/payments/billing-agreements/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.customerKey").doesNotExist())
                .andExpect(jsonPath("$.data.billingKey").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/payments/billing-agreements/me - authenticated -> 200")
    void cancelMyBillingAgreement_success() throws Exception {
        given(billingAgreementApplicationService.cancelMyBillingAgreement(any()))
                .willReturn(new BillingAgreementResponse(
                        PaymentProviderType.TOSS,
                        BillingAgreementStatus.CANCELLED,
                        "CARD",
                        "1234",
                        LocalDate.now().plusMonths(1),
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        null));

        mockMvc.perform(delete("/api/payments/billing-agreements/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void callbackOutcome_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/payments/orders/ORDER-1/outcome"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(paymentRecoveryReadService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void callbackOutcome_returnsOnlyRecoveryIntentFields() throws Exception {
        given(paymentRecoveryReadService.getCallbackOutcome(any(), eq("ORDER-1")))
                .willReturn(new PaymentCommandOutcomeResponse(
                        PaymentPurpose.SUBSCRIBE,
                        PaymentOrderStatus.DONE,
                        100L,
                        10L,
                        BillingCycle.MONTHLY));

        mockMvc.perform(get("/api/payments/orders/ORDER-1/outcome"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.purpose").value("SUBSCRIBE"))
                .andExpect(jsonPath("$.data.orderStatus").value("DONE"))
                .andExpect(jsonPath("$.data.userSubscriptionId").value(100))
                .andExpect(jsonPath("$.data.targetSubscriptionId").value(10))
                .andExpect(jsonPath("$.data.targetBillingCycle").value("MONTHLY"))
                .andExpect(jsonPath("$.data.failureCode").doesNotExist())
                .andExpect(jsonPath("$.data.providerPayload").doesNotExist())
                .andExpect(jsonPath("$.data.commandKey").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "USER")
    void upgradeOutcome_requiresExactTargetIntent() throws Exception {
        given(paymentRecoveryReadService.getUpgradeOutcome(any(), eq(20L), eq(BillingCycle.YEARLY)))
                .willReturn(new PaymentCommandOutcomeResponse(
                        PaymentPurpose.UPGRADE,
                        PaymentOrderStatus.PROCESSING,
                        null,
                        20L,
                        BillingCycle.YEARLY));

        mockMvc.perform(get("/api/payments/subscription-upgrades/outcome")
                        .param("subscriptionId", "20")
                        .param("billingCycle", "YEARLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.purpose").value("UPGRADE"))
                .andExpect(jsonPath("$.data.orderStatus").value("PROCESSING"))
                .andExpect(jsonPath("$.data.userSubscriptionId").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void userPaymentOutcomeReads_adminRole_areForbidden() throws Exception {
        mockMvc.perform(get("/api/payments/orders/ORDER-1/outcome"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/payments/subscription-upgrades/outcome")
                        .param("subscriptionId", "20")
                        .param("billingCycle", "YEARLY"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(paymentRecoveryReadService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN cannot invoke any user payment mutation")
    void userPaymentMutations_adminRole_areForbidden() throws Exception {
        List<MockHttpServletRequestBuilder> requests = List.of(
                post("/api/payments/billing-agreements/prepare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":10,\"billingCycle\":\"MONTHLY\",\"purpose\":\"SUBSCRIBE\"}"),
                post("/api/payments/billing-agreements/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"ORDER-1\",\"authKey\":\"auth\",\"customerKey\":\"customer\",\"amount\":9900}"),
                delete("/api/payments/billing-agreements/me")
        );

        for (MockHttpServletRequestBuilder request : requests) {
            mockMvc.perform(request).andExpect(status().isForbidden());
        }

        verifyNoInteractions(billingAgreementApplicationService);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    @DisplayName("ADMIN denial wins for a mixed-authority payment principal")
    void userPaymentMutation_mixedRoles_isForbidden() throws Exception {
        mockMvc.perform(post("/api/payments/billing-agreements/prepare")
                        .header("Idempotency-Key", PREPARE_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":10,\"billingCycle\":\"MONTHLY\",\"purpose\":\"SUBSCRIBE\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(billingAgreementApplicationService);
    }
}
