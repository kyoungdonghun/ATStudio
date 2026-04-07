package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.subscription.*;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.UserSubscriptionService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("UserSubscriptionController 권한 테스트")
class UserSubscriptionControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UserSubscriptionService userSubscriptionService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    private static final SubscriptionResponse MOCK_SUB_RESPONSE = new SubscriptionResponse(
            1L, "Basic", "Basic plan", "INDIVIDUAL",
            BigDecimal.valueOf(9900), BigDecimal.valueOf(99000),
            10, 3, 3, true);

    private static final UserSubscriptionResponse MOCK_RESPONSE = new UserSubscriptionResponse(
            100L, 1L, "user1", MOCK_SUB_RESPONSE,
            "MONTHLY", "ACTIVE",
            LocalDate.now(), LocalDate.now().plusMonths(1),
            null, null,
            LocalDateTime.now());

    private static final ChangeSubscriptionResponse MOCK_CHANGE_RESPONSE = new ChangeSubscriptionResponse(
            MOCK_SUB_RESPONSE, "MONTHLY", "ACTIVE", "UPGRADE",
            BigDecimal.valueOf(5000),
            LocalDate.now(), LocalDate.now().plusMonths(1));

    // -- 6.3 POST /api/user-subscriptions ------------------------------------

    @Test
    @DisplayName("POST /api/user-subscriptions - 비인증 -> 401")
    void subscribe_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/user-subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":1,\"billingCycle\":\"MONTHLY\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/user-subscriptions - 인증 유저 -> 201")
    void subscribe_success() throws Exception {
        given(userSubscriptionService.subscribe(any(), any())).willReturn(MOCK_RESPONSE);

        mockMvc.perform(post("/api/user-subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":1,\"billingCycle\":\"MONTHLY\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(100));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/user-subscriptions - BUSINESS 미인증 -> 403")
    void subscribe_certRequired() throws Exception {
        given(userSubscriptionService.subscribe(any(), any()))
                .willThrow(new BusinessException(BUSINESS_ERROR.COMPANY_CERTIFICATION_REQUIRED));

        mockMvc.perform(post("/api/user-subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":1,\"billingCycle\":\"MONTHLY\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/user-subscriptions - 중복 구독 -> 409")
    void subscribe_duplicate() throws Exception {
        given(userSubscriptionService.subscribe(any(), any()))
                .willThrow(new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_ALREADY_EXISTS));

        mockMvc.perform(post("/api/user-subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":1,\"billingCycle\":\"MONTHLY\"}"))
                .andExpect(status().isConflict());
    }

    // -- 6.4 GET /api/user-subscriptions/me ----------------------------------

    @Test
    @DisplayName("GET /api/user-subscriptions/me - 비인증 -> 401")
    void getMySubscription_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/user-subscriptions/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/user-subscriptions/me - 인증 유저 -> 200")
    void getMySubscription_success() throws Exception {
        given(userSubscriptionService.getMySubscription(any())).willReturn(MOCK_RESPONSE);

        mockMvc.perform(get("/api/user-subscriptions/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100));
    }

    // -- 6.5 GET /api/user-subscriptions (ADMIN) -----------------------------

    @Test
    @DisplayName("GET /api/user-subscriptions - 비인증 -> 401")
    void listAll_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/user-subscriptions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/user-subscriptions - 일반 유저 -> 403")
    void listAll_forbidden() throws Exception {
        mockMvc.perform(get("/api/user-subscriptions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/user-subscriptions - ADMIN -> 200")
    void listAll_success() throws Exception {
        given(userSubscriptionService.listAll(anyInt(), anyInt()))
                .willReturn(ResponseDTO.<UserSubscriptionResponse>builder()
                        .dataList(List.of()).build());

        mockMvc.perform(get("/api/user-subscriptions"))
                .andExpect(status().isOk());
    }

    // -- 6.6 GET /api/user-subscriptions/{id} (ADMIN) ------------------------

    @Test
    @DisplayName("GET /api/user-subscriptions/100 - 비인증 -> 401")
    void getDetail_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/user-subscriptions/100"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/user-subscriptions/100 - 일반 유저 -> 403")
    void getDetail_forbidden() throws Exception {
        mockMvc.perform(get("/api/user-subscriptions/100"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/user-subscriptions/100 - ADMIN -> 200")
    void getDetail_success() throws Exception {
        given(userSubscriptionService.getDetail(100L)).willReturn(MOCK_RESPONSE);

        mockMvc.perform(get("/api/user-subscriptions/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100));
    }

    // -- 6.7 PUT /api/user-subscriptions/me ----------------------------------

    @Test
    @DisplayName("PUT /api/user-subscriptions/me - 비인증 -> 401")
    void changeSubscription_unauthenticated() throws Exception {
        mockMvc.perform(put("/api/user-subscriptions/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":2,\"billingCycle\":\"MONTHLY\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/user-subscriptions/me - 인증 유저 -> 200")
    void changeSubscription_success() throws Exception {
        given(userSubscriptionService.changeSubscription(any(), any()))
                .willReturn(MOCK_CHANGE_RESPONSE);

        mockMvc.perform(put("/api/user-subscriptions/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subscriptionId\":2,\"billingCycle\":\"MONTHLY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.proratedAmount").value(5000));
    }

    // -- 6.8 PUT /api/user-subscriptions/{id} (ADMIN) ------------------------

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/user-subscriptions/100 - 일반 유저 -> 403")
    void adminUpdate_forbidden() throws Exception {
        mockMvc.perform(put("/api/user-subscriptions/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/user-subscriptions/100 - ADMIN -> 200")
    void adminUpdate_success() throws Exception {
        given(userSubscriptionService.adminUpdate(anyLong(), any())).willReturn(MOCK_RESPONSE);

        mockMvc.perform(put("/api/user-subscriptions/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk());
    }

    // -- 6.9 DELETE /api/user-subscriptions/{id} (ADMIN) ---------------------

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/user-subscriptions/100 - 일반 유저 -> 403")
    void adminCancel_forbidden() throws Exception {
        mockMvc.perform(delete("/api/user-subscriptions/100"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/user-subscriptions/100 - ADMIN -> 204")
    void adminCancel_success() throws Exception {
        doNothing().when(userSubscriptionService).adminCancel(100L);

        mockMvc.perform(delete("/api/user-subscriptions/100"))
                .andExpect(status().isNoContent());
    }

    // -- 6.10 DELETE /api/user-subscriptions/me ------------------------------

    @Test
    @DisplayName("DELETE /api/user-subscriptions/me - 비인증 -> 401")
    void selfCancel_unauthenticated() throws Exception {
        mockMvc.perform(delete("/api/user-subscriptions/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/user-subscriptions/me - 인증 유저 -> 204")
    void selfCancel_success() throws Exception {
        doNothing().when(userSubscriptionService).selfCancel(any());

        mockMvc.perform(delete("/api/user-subscriptions/me"))
                .andExpect(status().isNoContent());
    }
}
