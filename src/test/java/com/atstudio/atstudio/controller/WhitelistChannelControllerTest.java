package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.whitelist.WhitelistChannelResponse;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.WhitelistChannelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("WhitelistChannelController 권한 테스트")
class WhitelistChannelControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean WhitelistChannelService whitelistChannelService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    private static final WhitelistChannelResponse MOCK_RESPONSE = new WhitelistChannelResponse(
            1L, "https://youtube.com/@channel1", "채널1", LocalDateTime.now()
    );

    private static final String VALID_REQUEST = """
            {"channelUrl":"https://youtube.com/@channel1","channelName":"채널1"}""";

    // ── 12.1 POST /api/whitelist-channels ────────────────────────────────────

    @Test
    @DisplayName("POST /api/whitelist-channels - 비인증 → 401")
    void registerChannel_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/whitelist-channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/whitelist-channels - 인증됨 → 201")
    void registerChannel_authenticated_returns201() throws Exception {
        given(whitelistChannelService.registerChannel(any(), any())).willReturn(MOCK_RESPONSE);

        mockMvc.perform(post("/api/whitelist-channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/whitelist-channels - 유효하지 않은 URL → 400")
    void registerChannel_invalidUrl_returns400() throws Exception {
        given(whitelistChannelService.registerChannel(any(), any()))
                .willThrow(new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT));

        mockMvc.perform(post("/api/whitelist-channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"channelUrl":"https://vimeo.com/@ch","channelName":"채널"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/whitelist-channels - 구독 없음 → 403")
    void registerChannel_noSubscription_returns403() throws Exception {
        given(whitelistChannelService.registerChannel(any(), any()))
                .willThrow(new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));

        mockMvc.perform(post("/api/whitelist-channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isForbidden());
    }

    // ── 12.2 GET /api/whitelist-channels ─────────────────────────────────────

    @Test
    @DisplayName("GET /api/whitelist-channels - 비인증 → 401")
    void getMyChannels_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/whitelist-channels"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/whitelist-channels - 인증됨 → 200")
    void getMyChannels_authenticated_returns200() throws Exception {
        given(whitelistChannelService.getMyChannels(any())).willReturn(List.of(MOCK_RESPONSE));

        mockMvc.perform(get("/api/whitelist-channels"))
                .andExpect(status().isOk());
    }

    // ── 12.3 PUT /api/whitelist-channels/{channelId} ─────────────────────────

    @Test
    @DisplayName("PUT /api/whitelist-channels/{id} - 비인증 → 401")
    void updateChannel_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/whitelist-channels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/whitelist-channels/{id} - 인증됨 → 200")
    void updateChannel_authenticated_returns200() throws Exception {
        given(whitelistChannelService.updateChannel(any(), anyLong(), any())).willReturn(MOCK_RESPONSE);

        mockMvc.perform(put("/api/whitelist-channels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/whitelist-channels/{id} - 타인 채널 → 403")
    void updateChannel_forbidden_returns403() throws Exception {
        given(whitelistChannelService.updateChannel(any(), anyLong(), any()))
                .willThrow(new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));

        mockMvc.perform(put("/api/whitelist-channels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isForbidden());
    }

    // ── 12.4 DELETE /api/whitelist-channels/{channelId} ──────────────────────

    @Test
    @DisplayName("DELETE /api/whitelist-channels/{id} - 비인증 → 401")
    void deleteChannel_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/whitelist-channels/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/whitelist-channels/{id} - 인증됨 → 204")
    void deleteChannel_authenticated_returns204() throws Exception {
        doNothing().when(whitelistChannelService).deleteChannel(any(), anyLong());

        mockMvc.perform(delete("/api/whitelist-channels/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/whitelist-channels/{id} - 타인 채널 → 403")
    void deleteChannel_forbidden_returns403() throws Exception {
        doThrow(new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS))
                .when(whitelistChannelService).deleteChannel(any(), anyLong());

        mockMvc.perform(delete("/api/whitelist-channels/1"))
                .andExpect(status().isForbidden());
    }
}
