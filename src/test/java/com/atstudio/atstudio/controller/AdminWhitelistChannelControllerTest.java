package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportFile;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportSummaryResponse;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.AdminWhitelistChannelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("AdminWhitelistChannelController contract tests")
class AdminWhitelistChannelControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AdminWhitelistChannelService adminWhitelistChannelService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("explicit export scope returns immutable batch identity")
    void exportReturnsBatchIdentity() throws Exception {
        given(adminWhitelistChannelService.exportChannels(
                nullable(com.atstudio.atstudio.security.CustomUserDetails.class),
                any()))
                .willReturn(new AdminWhitelistExportFile(
                        77L,
                        "whitelist-channels.csv",
                        "csv".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(post("/api/admin/whitelist-channels/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PENDING\",\"keyword\":\"shorts\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Whitelist-Export-Batch-Id", "77"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("stored export batch can be downloaded again")
    void downloadStoredBatch() throws Exception {
        given(adminWhitelistChannelService.downloadExportBatch(77L))
                .willReturn(new AdminWhitelistExportFile(
                        77L,
                        "whitelist-channels.csv",
                        "csv".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(get("/api/admin/whitelist-channels/exports/77"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Whitelist-Export-Batch-Id", "77"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("recent export lookup returns summary metadata only")
    void listRecentExportsReturnsSummaryOnly() throws Exception {
        given(adminWhitelistChannelService.listRecentExports(
                nullable(com.atstudio.atstudio.security.CustomUserDetails.class),
                eq(WhitelistChannelStatus.PENDING),
                eq("shorts")))
                .willReturn(List.of(new AdminWhitelistExportSummaryResponse(
                        77L,
                        "whitelist-channels.csv",
                        3,
                        WhitelistChannelStatus.PENDING,
                        "shorts",
                        LocalDateTime.of(2026, 8, 13, 12, 0))));

        mockMvc.perform(get("/api/admin/whitelist-channels/exports/recent")
                        .param("status", "PENDING")
                        .param("keyword", "shorts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList[0].batchId").value(77))
                .andExpect(jsonPath("$.dataList[0].fileName").value("whitelist-channels.csv"))
                .andExpect(jsonPath("$.dataList[0].itemCount").value(3))
                .andExpect(jsonPath("$.dataList[0].status").value("PENDING"))
                .andExpect(jsonPath("$.dataList[0].keyword").value("shorts"))
                .andExpect(jsonPath("$.dataList[0].createdAt").value("2026-08-13T12:00:00"))
                .andExpect(jsonPath("$.dataList[0].items").doesNotExist())
                .andExpect(jsonPath("$.dataList[0].content").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("recent export lookup rejects non-admin users")
    void listRecentExportsRejectsNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/whitelist-channels/exports/recent")
                        .param("status", "PENDING"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminWhitelistChannelService);
    }
}
