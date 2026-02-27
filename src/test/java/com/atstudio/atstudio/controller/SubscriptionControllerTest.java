package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.subscription.SubscriptionResponse;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("SubscriptionController 테스트")
class SubscriptionControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean SubscriptionService subscriptionService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    private static final SubscriptionResponse MOCK_SUB = new SubscriptionResponse(
            1L, "Basic", "Basic plan", "INDIVIDUAL",
            BigDecimal.valueOf(9900), BigDecimal.valueOf(99000),
            10, 3, true);

    // -- 6.1 GET /api/subscriptions (PUBLIC) ---------------------------------

    @Test
    @DisplayName("GET /api/subscriptions - 비인증도 허용 -> 200")
    void list_public_success() throws Exception {
        given(subscriptionService.getActiveSubscriptions(any())).willReturn(List.of(MOCK_SUB));

        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList").isArray())
                .andExpect(jsonPath("$.dataList[0].name").value("Basic"));
    }

    @Test
    @DisplayName("GET /api/subscriptions?userType=INDIVIDUAL - 필터 적용 200")
    void list_withFilter() throws Exception {
        given(subscriptionService.getActiveSubscriptions("INDIVIDUAL"))
                .willReturn(List.of(MOCK_SUB));

        mockMvc.perform(get("/api/subscriptions").param("userType", "INDIVIDUAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList").isArray());
    }

    // -- 6.2 GET /api/subscriptions/{subscriptionId} (PUBLIC) ----------------

    @Test
    @DisplayName("GET /api/subscriptions/1 - 비인증도 허용 -> 200")
    void detail_public_success() throws Exception {
        given(subscriptionService.getSubscription(1L)).willReturn(MOCK_SUB);

        mockMvc.perform(get("/api/subscriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Basic"));
    }

    @Test
    @DisplayName("GET /api/subscriptions/99 - 미존재 -> 404")
    void detail_notFound() throws Exception {
        given(subscriptionService.getSubscription(99L))
                .willThrow(new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        mockMvc.perform(get("/api/subscriptions/99"))
                .andExpect(status().isNotFound());
    }
}
