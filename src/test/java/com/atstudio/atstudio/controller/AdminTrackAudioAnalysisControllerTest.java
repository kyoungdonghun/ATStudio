package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.track.AdminTrackAudioAnalysisDryRunItemResponse;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.AdminTrackAudioAnalysisService;
import com.atstudio.atstudio.service.audio.AudioAnalysisFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("AdminTrackAudioAnalysisController")
class AdminTrackAudioAnalysisControllerTest {

    private static final String RAW_STORAGE_KEY = "tracks/audio/secret-storage-key.mp3";

    @Autowired MockMvc mockMvc;

    @MockitoBean AdminTrackAudioAnalysisService audioAnalysisService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("dry-run requires authentication")
    void dryRunUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/admin/tracks/audio-analysis/dry-run"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("dry-run rejects non-admin users")
    void dryRunUserReturns403() throws Exception {
        mockMvc.perform(get("/api/admin/tracks/audio-analysis/dry-run"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("dry-run exposes analysis evidence but no raw storage key field or value")
    void dryRunAdminResponseDoesNotExposeRawStorageKey() throws Exception {
        AdminTrackAudioAnalysisDryRunItemResponse row =
                new AdminTrackAudioAnalysisDryRunItemResponse(
                        7L,
                        "Track title",
                        false,
                        true,
                        120,
                        90,
                        -30,
                        true,
                        AudioAnalysisFormat.MP3,
                        AdminTrackAudioAnalysisDryRunItemResponse.Status.METADATA_MISMATCH,
                        AdminTrackAudioAnalysisDryRunItemResponse.Recommendation.BACKFILL_ANALYSIS_METADATA,
                        3_969_000L,
                        44_100,
                        2);
        given(audioAnalysisService.dryRun(1, 20)).willReturn(ResponseDTO
                .<AdminTrackAudioAnalysisDryRunItemResponse>builder()
                .message("Audio analysis dry-run completed")
                .dataList(List.of(row))
                .pageInfo(PageInfo.of(1, 20, 1, 10))
                .build());

        mockMvc.perform(get("/api/admin/tracks/audio-analysis/dry-run")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList[0].trackId").value(7))
                .andExpect(jsonPath("$.dataList[0].isActive").value(false))
                .andExpect(jsonPath("$.dataList[0].storedDurationSeconds").value(120))
                .andExpect(jsonPath("$.dataList[0].analyzedDurationSeconds").value(90))
                .andExpect(jsonPath("$.dataList[0].format").value("MP3"))
                .andExpect(jsonPath("$.dataList[0].status").value("METADATA_MISMATCH"))
                .andExpect(jsonPath("$.dataList[0].audioFile").doesNotExist())
                .andExpect(jsonPath("$.dataList[0].storageKey").doesNotExist())
                .andExpect(content().string(not(containsString(RAW_STORAGE_KEY))));
    }
}
