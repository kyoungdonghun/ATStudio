package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.dto.track.TrackResponse;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.DownloadService;
import com.atstudio.atstudio.service.TrackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("TrackController 권한 테스트")
class TrackControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TrackService trackService;
    @MockitoBean DownloadService downloadService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    // ── POST /api/tracks (ADMIN 전용) ─────────────────────────────────────────

    @Test
    @DisplayName("POST /api/tracks - 비인증 → 401")
    void createTrack_unauthenticated_returns401() throws Exception {
        mockMvc.perform(multipart("/api/tracks")
                        .file(new MockMultipartFile("audioFile", "test.mp3", "audio/mpeg", "audio".getBytes()))
                        .param("title", "Track").param("bpm", "120").param("tonality", "C"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/tracks - 일반 유저 → 403")
    void createTrack_userRole_returns403() throws Exception {
        mockMvc.perform(multipart("/api/tracks")
                        .file(new MockMultipartFile("audioFile", "test.mp3", "audio/mpeg", "audio".getBytes()))
                        .param("title", "Track").param("bpm", "120").param("tonality", "C"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/tracks - ADMIN → 보안 통과 (201)")
    void createTrack_adminRole_returns201() throws Exception {
        TrackResponse mockResponse = new TrackResponse(1L, "Track", "Artist", 0, 120, "C", null,
                "tracks/audio/test.mp3", null, false, 0L, 0L, 0L, null, List.of(), null, null);
        given(trackService.createTrack(any(), any(), any(), any())).willReturn(mockResponse);

        mockMvc.perform(multipart("/api/tracks")
                        .file(new MockMultipartFile("audioFile", "test.mp3", "audio/mpeg", "audio".getBytes()))
                        .param("title", "Track").param("bpm", "120").param("tonality", "C"))
                .andExpect(status().isCreated());
    }

    // ── PUT /api/tracks/{id} (ADMIN 전용) ────────────────────────────────────

    @Test
    @DisplayName("PUT /api/tracks/{id} - 비인증 → 401")
    void updateTrack_unauthenticated_returns401() throws Exception {
        mockMvc.perform(multipart("/api/tracks/1")
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .param("title", "Updated"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/tracks/{id} - 일반 유저 → 403")
    void updateTrack_userRole_returns403() throws Exception {
        mockMvc.perform(multipart("/api/tracks/1")
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .param("title", "Updated"))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/tracks/{id} (ADMIN 전용) ──────────────────────────────────

    @Test
    @DisplayName("DELETE /api/tracks/{id} - 비인증 → 401")
    void deleteTrack_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/tracks/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/tracks/{id} - 일반 유저 → 403")
    void deleteTrack_userRole_returns403() throws Exception {
        mockMvc.perform(delete("/api/tracks/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/tracks/{id} - ADMIN → 204")
    void deleteTrack_adminRole_returns204() throws Exception {
        doNothing().when(trackService).deleteTrack(anyLong());

        mockMvc.perform(delete("/api/tracks/1"))
                .andExpect(status().isNoContent());
    }

    // ── GET /api/tracks (PUBLIC) ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/tracks - 비인증도 접근 가능 (PUBLIC)")
    void getTracks_unauthenticated_returns200() throws Exception {
        given(trackService.getTracks(any())).willReturn(
                com.atstudio.atstudio.common.dto.ResponseDTO.<com.atstudio.atstudio.dto.track.TrackListItemResponse>builder()
                        .message("ok").dataList(List.of())
                        .pageInfo(com.atstudio.atstudio.common.dto.PageInfo.of(1, 20, 0, 10))
                        .build());

        mockMvc.perform(get("/api/tracks"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET public track - original audio key is represented as null")
    void getTrack_publicResponseDoesNotExposeOriginalAudioKey() throws Exception {
        given(trackService.getTrack(1L)).willReturn(trackResponse(null));

        mockMvc.perform(get("/api/tracks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.audioFile").hasJsonPath())
                .andExpect(jsonPath("$.data.audioFile").value(nullValue()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET admin track - original audio key remains available")
    void getTrackForAdmin_responseRetainsOriginalAudioKey() throws Exception {
        given(trackService.getTrackForAdmin(1L)).willReturn(trackResponse("tracks/audio/original.mp3"));

        mockMvc.perform(get("/api/tracks/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.audioFile").value("tracks/audio/original.mp3"));
    }

    // ── GET /api/tracks/{id}/stream (PUBLIC) ─────────────────────────────────

    @Test
    @DisplayName("GET stream - 전용 previewFile의 Range 응답 유지")
    void streamTrack_previewFile_preservesRangeResponse() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 10L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=2-5"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 2-5/10"))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 4L))
                .andExpect(content().bytes("2345".getBytes()));
    }

    @Test
    @DisplayName("GET stream - Range 미지정 원본 폴백은 공개 경계까지만 반환")
    void streamTrack_originalFallback_withoutRangeReturnsBoundedPrefix() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 4L));

        mockMvc.perform(get("/api/tracks/1/stream"))
                .andExpect(status().isOk())
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 4L))
                .andExpect(content().bytes("0123".getBytes()));
    }

    @Test
    @DisplayName("GET stream - 공개 경계 이후에서 시작하는 Range는 416")
    void streamTrack_originalFallback_outOfBoundsRangeReturns416() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 4L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=4-"))
                .andExpect(status().isRequestedRangeNotSatisfiable())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */4"));
    }

    @Test
    @DisplayName("GET stream - 경계 안에서 시작한 Range의 끝은 공개 경계로 제한")
    void streamTrack_originalFallback_clampsRangeEndToBoundary() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 4L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=2-9"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 2-3/4"))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 2L))
                .andExpect(content().bytes("23".getBytes()));
    }

    @Test
    @DisplayName("GET stream - suffix Range is resolved against the public boundary")
    void streamTrack_originalFallback_suffixRangeUsesPublicBoundary() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 4L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=-2"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 2-3/4"))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 2L))
                .andExpect(content().bytes("23".getBytes()));
    }

    @Test
    @DisplayName("GET stream - malformed Range is rejected")
    void streamTrack_malformedRangeReturns416() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 4L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=invalid"))
                .andExpect(status().isRequestedRangeNotSatisfiable())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */4"));
    }

    @Test
    @DisplayName("GET stream - reversed, zero suffix, unsupported unit, and overflowing Ranges are rejected")
    void streamTrack_rangeEdgeCasesReturn416() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 4L));

        for (String rangeHeader : List.of(
                "bytes=3-1",
                "bytes=-0",
                "items=0-1",
                "bytes=9223372036854775808-")) {
            mockMvc.perform(get("/api/tracks/1/stream")
                            .header(HttpHeaders.RANGE, rangeHeader))
                    .andExpect(status().isRequestedRangeNotSatisfiable())
                    .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */4"));
        }
    }

    @Test
    @DisplayName("GET stream - multiple Ranges are rejected")
    void streamTrack_multipleRangesReturn416() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 4L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=0-0,2-2"))
                .andExpect(status().isRequestedRangeNotSatisfiable())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */4"));
    }

    @Test
    @DisplayName("GET stream - repeated requests cannot retrieve bytes beyond the public boundary")
    void streamTrack_repeatedOutOfBoundsRangesReturn416() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 4L));

        for (int start : List.of(4, 5, 9)) {
            mockMvc.perform(get("/api/tracks/1/stream")
                            .header(HttpHeaders.RANGE, "bytes=" + start + "-"))
                    .andExpect(status().isRequestedRangeNotSatisfiable())
                    .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */4"));
        }
    }

    // ── GET /api/tracks/{id}/download (인증 필요) ─────────────────────────────

    @Test
    @DisplayName("GET /api/tracks/{id}/download - 비인증 → 401")
    void downloadTrack_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/tracks/1/download"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/tracks/{id}/download - 인증 사용자 → 보안 통과")
    void downloadTrack_authenticated_securityPasses() throws Exception {
        given(downloadService.download(anyLong(), any())).willReturn(
                new ByteArrayResource("audio".getBytes()));

        mockMvc.perform(get("/api/tracks/1/download"))
                .andExpect(status().isOk());
    }

    private TrackResponse trackResponse(String audioFile) {
        return new TrackResponse(1L, "Track", "Artist", 120, 120, "C", null,
                audioFile, null, true, 0L, 0L, 0L, null, List.of(), null, null);
    }
}
