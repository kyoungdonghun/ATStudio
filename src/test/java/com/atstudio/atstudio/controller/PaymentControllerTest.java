package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.dto.payment.BillingAgreementCheckoutResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementResponse;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.BillingAgreementApplicationService;
import com.atstudio.atstudio.service.PaymentApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("PaymentController billing agreement tests")
class PaymentControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean PaymentApplicationService paymentApplicationService;
    @MockitoBean BillingAgreementApplicationService billingAgreementApplicationService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/payments/billing-agreements/prepare - unauthenticated -> 401")
    void prepareBillingAgreement_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/payments/billing-agreements/prepare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":10,\"billingCycle\":\"MONTHLY\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/payments/billing-agreements/prepare - authenticated -> 201")
    void prepareBillingAgreement_success() throws Exception {
        given(billingAgreementApplicationService.prepareBillingAgreement(any(), any()))
                .willReturn(new BillingAgreementPrepareResponse(
                        "ORDER-1",
                        PaymentProviderType.TOSS_BILLING,
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":10,\"billingCycle\":\"MONTHLY\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.provider").value("TOSS_BILLING"))
                .andExpect(jsonPath("$.data.purpose").value("SUBSCRIBE"))
                .andExpect(jsonPath("$.data.checkout.customerKey").value("ats_billing_customer_1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/payments/billing-agreements/confirm - authenticated -> 200")
    void confirmBillingAgreement_success() throws Exception {
        given(billingAgreementApplicationService.confirmBillingAgreement(any(), any()))
                .willReturn(new BillingAgreementConfirmResponse(
                        "ORDER-1",
                        PaymentOrderStatus.DONE,
                        PaymentProviderType.TOSS_BILLING,
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
                        PaymentProviderType.TOSS_BILLING,
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
                        PaymentProviderType.TOSS_BILLING,
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
}
