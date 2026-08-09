package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionPreviewResponse;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionResponse;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.AdminSubscriptionCorrectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("Administrator subscription correction API")
class AdminUserSubscriptionCorrectionControllerTest {

    private static final String BASE_PATH = "/api/admin/user-subscription-corrections";
    private static final String VALID_REQUEST = """
            {
              "userSubscriptionId": 20,
              "targetSubscriptionId": 1,
              "targetBillingCycle": "MONTHLY",
              "targetStatus": "CANCELLED",
              "targetExpiresAt": "2026-08-08",
              "clearPendingChange": true,
              "cancelBillingAgreement": true,
              "reasonNote": "approved support case"
            }
            """;

    @Autowired MockMvc mockMvc;
    @MockitoBean AdminSubscriptionCorrectionService correctionService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("all correction endpoints are class-level ADMIN operations")
    void allEndpointsRequireAdmin() {
        PreAuthorize authorization = AdminUserSubscriptionCorrectionController.class
                .getAnnotation(PreAuthorize.class);

        assertThat(authorization).isNotNull();
        assertThat(authorization.value()).isEqualTo("hasRole('ADMIN')");
    }

    @Test
    @DisplayName("preview rejects unauthenticated and non-admin callers")
    void previewRequiresAdmin() throws Exception {
        mockMvc.perform(post(BASE_PATH + "/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post(BASE_PATH + "/preview")
                        .with(user(CustomUserDetails.builder()
                                .id(7L)
                                .email("user@test.com")
                                .role(UserRole.USER)
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("preview and request forward the authenticated administrator")
    void previewAndRequestForwardActor() throws Exception {
        given(correctionService.previewCorrection(any(), any()))
                .willReturn(ResponseDTO.<AdminSubscriptionCorrectionPreviewResponse>builder().build());
        given(correctionService.requestCorrection(any(), any()))
                .willReturn(ResponseDTO.<AdminSubscriptionCorrectionResponse>builder().build());

        mockMvc.perform(post(BASE_PATH + "/preview")
                        .with(user(admin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk());
        mockMvc.perform(post(BASE_PATH)
                        .with(user(admin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk());

        verify(correctionService).previewCorrection(
                org.mockito.ArgumentMatchers.argThat(actor -> actor.getId().equals(99L)), any());
        verify(correctionService).requestCorrection(
                org.mockito.ArgumentMatchers.argThat(actor -> actor.getId().equals(99L)), any());
    }

    @Test
    @DisplayName("list, detail, explicit approve, and execute expose the approved contract")
    void workflowContracts() throws Exception {
        given(correctionService.listCorrections(any(), anyInt(), anyInt()))
                .willReturn(ResponseDTO.<AdminSubscriptionCorrectionResponse>builder().build());
        given(correctionService.getCorrection(any(), anyLong()))
                .willReturn(ResponseDTO.<AdminSubscriptionCorrectionResponse>builder().build());
        given(correctionService.approveCorrection(anyLong(), any(), any()))
                .willReturn(ResponseDTO.<AdminSubscriptionCorrectionResponse>builder().build());
        given(correctionService.executeCorrection(anyLong(), any(), any()))
                .willReturn(ResponseDTO.<AdminSubscriptionCorrectionResponse>builder().build());

        mockMvc.perform(get(BASE_PATH).with(user(admin())))
                .andExpect(status().isOk());
        mockMvc.perform(get(BASE_PATH + "/88").with(user(admin())))
                .andExpect(status().isOk());
        mockMvc.perform(post(BASE_PATH + "/88/approve")
                        .with(user(admin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"explicit confirmation\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post(BASE_PATH + "/88/execute")
                        .with(user(admin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"apply\"}"))
                .andExpect(status().isOk());

        verify(correctionService).listCorrections(any(), org.mockito.ArgumentMatchers.eq(1),
                org.mockito.ArgumentMatchers.eq(20));
        verify(correctionService).getCorrection(any(), org.mockito.ArgumentMatchers.eq(88L));
        verify(correctionService).approveCorrection(
                org.mockito.ArgumentMatchers.eq(88L), any(), any());
        verify(correctionService).executeCorrection(
                org.mockito.ArgumentMatchers.eq(88L), any(), any());
    }

    @Test
    @DisplayName("open workflow lookup forwards the administrator and returns the detailed response")
    void openWorkflowReturnsDetailedResponse() throws Exception {
        given(correctionService.getOpenCorrection(any(), org.mockito.ArgumentMatchers.eq(20L)))
                .willReturn(Optional.of(
                        ResponseDTO.<AdminSubscriptionCorrectionResponse>builder().build()));

        mockMvc.perform(get(BASE_PATH + "/open")
                        .with(user(admin()))
                        .param("userSubscriptionId", "20"))
                .andExpect(status().isOk());

        verify(correctionService).getOpenCorrection(
                org.mockito.ArgumentMatchers.argThat(actor -> actor.getId().equals(99L)),
                org.mockito.ArgumentMatchers.eq(20L));
    }

    @Test
    @DisplayName("open workflow lookup returns no content when only terminal history exists")
    void openWorkflowReturnsNoContent() throws Exception {
        given(correctionService.getOpenCorrection(any(), org.mockito.ArgumentMatchers.eq(20L)))
                .willReturn(Optional.empty());

        mockMvc.perform(get(BASE_PATH + "/open")
                        .with(user(admin()))
                        .param("userSubscriptionId", "20"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("open workflow lookup preserves the stable unknown-subscription 404")
    void openWorkflowReturnsStableNotFound() throws Exception {
        given(correctionService.getOpenCorrection(any(), org.mockito.ArgumentMatchers.eq(404L)))
                .willThrow(new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        mockMvc.perform(get(BASE_PATH + "/open")
                        .with(user(admin()))
                        .param("userSubscriptionId", "404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("SUBSCRIPTION_NOT_FOUND"));
    }

    @Test
    @DisplayName("open workflow lookup rejects non-administrators")
    void openWorkflowRequiresAdmin() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/open")
                        .with(user(CustomUserDetails.builder()
                                .id(7L)
                                .email("user@test.com")
                                .role(UserRole.USER)
                                .build()))
                        .param("userSubscriptionId", "20"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("request validation rejects a blank reason")
    void requestRejectsBlankReason() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST.replace("approved support case", "   ")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("approval and execution notes retain the nullable 500-character contract")
    void workflowNotesRejectMoreThanFiveHundredCharacters() throws Exception {
        String oversizedNote = "{\"note\":\"" + "x".repeat(501) + "\"}";

        mockMvc.perform(post(BASE_PATH + "/88/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oversizedNote))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(BASE_PATH + "/88/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oversizedNote))
                .andExpect(status().isBadRequest());
    }

    private CustomUserDetails admin() {
        return CustomUserDetails.builder()
                .id(99L)
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .build();
    }
}
