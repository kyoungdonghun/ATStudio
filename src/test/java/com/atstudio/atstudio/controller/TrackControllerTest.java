package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.track.PlayableTrackResponse;
import com.atstudio.atstudio.dto.track.TrackSearchRequest;
import com.atstudio.atstudio.dto.track.TrackUpdateRequest;
import com.atstudio.atstudio.dto.track.TrackResponse;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.DownloadService;
import com.atstudio.atstudio.service.PlayableTrackService;
import com.atstudio.atstudio.service.TrackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoInteractions;
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
    @MockitoBean PlayableTrackService playableTrackService;
    @MockitoBean DownloadService downloadService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/tracks/batch is public and excludes audio storage keys")
    void hydratePlayableTracks_publicReturnsCompleteContract() throws Exception {
        given(playableTrackService.hydrate(List.of(2L, 1L, 2L))).willReturn(List.of(
                new PlayableTrackResponse(
                        2L,
                        "Track 2",
                        "Artist",
                        182,
                        "tracks/thumbnails/2.jpg",
                        "[0.2,0.8]",
                        120,
                        "C",
                        List.of())
        ));

        mockMvc.perform(post("/api/tracks/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[2,1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList[0].id").value(2))
                .andExpect(jsonPath("$.dataList[0].duration").value(182))
                .andExpect(jsonPath("$.dataList[0].waveformData").value("[0.2,0.8]"))
                .andExpect(jsonPath("$.dataList[0].audioFile").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/tracks/batch rejects more than 100 IDs before querying")
    void hydratePlayableTracks_rejectsOversizedBatch() throws Exception {
        String ids = LongStream.rangeClosed(1, 101)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));

        mockMvc.perform(post("/api/tracks/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[" + ids + "]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(playableTrackService);
    }

    @Test
    @DisplayName("GET /api/tracks preserves repeated tag values for Instrument AND search")
    void getTracks_bindsRepeatedTagParametersWithoutCsvLoss() throws Exception {
        mockMvc.perform(get("/api/tracks")
                        .queryParam("genre", "K-Pop")
                        .queryParam("instrument", "Piano, Synth", "808 #Kit")
                        .queryParam("usage", "쇼츠 용"))
                .andExpect(status().isOk());

        ArgumentCaptor<TrackSearchRequest> request = ArgumentCaptor.forClass(TrackSearchRequest.class);
        org.mockito.Mockito.verify(trackService).getTracks(request.capture());
        assertEquals(List.of("K-Pop"), request.getValue().getGenre());
        assertEquals(List.of("Piano, Synth", "808 #Kit"), request.getValue().getInstrument());
        assertEquals(List.of("쇼츠 용"), request.getValue().getUsage());
    }

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

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/tracks - audio analysis failure uses stable 400 business contract")
    void createTrack_audioAnalysisFailureReturnsStable400() throws Exception {
        given(trackService.createTrack(any(), any(), any(), any()))
                .willThrow(new BusinessException(BUSINESS_ERROR.AUDIO_ANALYSIS_FAILED));

        mockMvc.perform(multipart("/api/tracks")
                        .file(new MockMultipartFile(
                                "audioFile", "broken.mp3", "audio/mpeg", new byte[] {1, 2, 3}))
                        .param("title", "Track")
                        .param("bpm", "120")
                        .param("tonality", "C"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("AUDIO_ANALYSIS_FAILED"))
                .andExpect(jsonPath("$.message").value(
                        "음원 파일을 분석할 수 없습니다. MP3 또는 WAV 파일을 확인해주세요."));
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

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/tracks/{id} binds explicit empty Tag replacement intent")
    void updateTrack_adminBindsExplicitEmptyTagReplacementIntent() throws Exception {
        given(trackService.updateTrack(eq(7L), any(), any(), any()))
                .willReturn(trackResponse("tracks/audio/original.mp3"));

        mockMvc.perform(multipart("/api/tracks/7")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("replaceTags", "true"))
                .andExpect(status().isOk());

        ArgumentCaptor<TrackUpdateRequest> request = ArgumentCaptor.forClass(TrackUpdateRequest.class);
        org.mockito.Mockito.verify(trackService).updateTrack(eq(7L), request.capture(), any(), any());
        assertEquals(true, request.getValue().getReplaceTags());
        assertEquals(null, request.getValue().getTagIds());
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

    @ParameterizedTest
    @CsvSource({
            "page, 0, 0, 20",
            "page, -1, -1, 20",
            "size, 0, 1, 0",
            "size, -1, 1, -1",
            "size, 101, 1, 101"
    })
    @DisplayName("GET /api/tracks returns the stable invalid-argument response for invalid pagination")
    void getTracks_invalidPaginationReturnsStableDomainError(
            String parameter,
            String value,
            int expectedPage,
            int expectedSize) throws Exception {
        given(trackService.getTracks(any()))
                .willThrow(new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT));

        mockMvc.perform(get("/api/tracks").queryParam(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"));

        ArgumentCaptor<TrackSearchRequest> request = ArgumentCaptor.forClass(TrackSearchRequest.class);
        org.mockito.Mockito.verify(trackService).getTracks(request.capture());
        assertEquals(expectedPage, request.getValue().getPage());
        assertEquals(expectedSize, request.getValue().getSize());
    }

    @Test
    @DisplayName("GET /api/tracks accepts size 100 at the controller boundary")
    void getTracks_maximumSizeDelegates() throws Exception {
        given(trackService.getTracks(any())).willReturn(
                com.atstudio.atstudio.common.dto.ResponseDTO
                        .<com.atstudio.atstudio.dto.track.TrackListItemResponse>builder()
                        .message("ok")
                        .dataList(List.of())
                        .pageInfo(com.atstudio.atstudio.common.dto.PageInfo.of(1, 100, 0, 10))
                        .build());

        mockMvc.perform(get("/api/tracks").queryParam("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(100));

        ArgumentCaptor<TrackSearchRequest> request = ArgumentCaptor.forClass(TrackSearchRequest.class);
        org.mockito.Mockito.verify(trackService).getTracks(request.capture());
        assertEquals(100, request.getValue().getSize());
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
    @DisplayName("GET stream - 시작/종료 Range를 전체 원본 길이로 해석")
    void streamTrack_startEndRangeUsesFullResourceLength() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 10L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=2-5"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 2-5/10"))
                .andExpect(result -> assertEquals(
                        List.of("bytes 2-5/10"),
                        result.getResponse().getHeaders(HttpHeaders.CONTENT_RANGE)))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 4L))
                .andExpect(content().bytes("2345".getBytes()));
    }

    @Test
    @DisplayName("GET stream - Range 미지정 시 원본 전체 반환")
    void streamTrack_withoutRangeReturnsFullResource() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 10L));

        mockMvc.perform(get("/api/tracks/1/stream"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 10L))
                .andExpect(content().bytes("0123456789".getBytes()));
    }

    @Test
    @DisplayName("GET stream - 전체 원본 길이에서 시작하는 Range는 416")
    void streamTrack_rangeStartingAtResourceLengthReturns416() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 10L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=10-"))
                .andExpect(status().isRequestedRangeNotSatisfiable())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */10"));
    }

    @Test
    @DisplayName("GET stream - 종료가 전체 길이를 넘는 Range는 원본 끝으로 제한")
    void streamTrack_rangeEndIsClampedToFullResourceLength() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 10L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=7-99"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 7-9/10"))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 3L))
                .andExpect(content().bytes("789".getBytes()));
    }

    @Test
    @DisplayName("GET stream - suffix Range를 전체 원본 길이로 해석")
    void streamTrack_suffixRangeUsesFullResourceLength() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 10L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=-2"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 8-9/10"))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 2L))
                .andExpect(content().bytes("89".getBytes()));
    }

    @Test
    @DisplayName("GET stream - malformed Range is rejected")
    void streamTrack_malformedRangeReturns416() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 10L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=invalid"))
                .andExpect(status().isRequestedRangeNotSatisfiable())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */10"));
    }

    @Test
    @DisplayName("GET stream - reversed, zero suffix, unsupported unit, and overflowing Ranges are rejected")
    void streamTrack_rangeEdgeCasesReturn416() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 10L));

        for (String rangeHeader : List.of(
                "bytes=3-1",
                "bytes=-0",
                "items=0-1",
                "bytes=9223372036854775808-")) {
            mockMvc.perform(get("/api/tracks/1/stream")
                            .header(HttpHeaders.RANGE, rangeHeader))
                    .andExpect(status().isRequestedRangeNotSatisfiable())
                    .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */10"));
        }
    }

    @Test
    @DisplayName("GET stream - multiple Ranges are rejected")
    void streamTrack_multipleRangesReturn416() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 10L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=0-0,2-2"))
                .andExpect(status().isRequestedRangeNotSatisfiable())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */10"));
    }

    @Test
    @DisplayName("GET stream - open-ended Range를 원본 끝까지 반환")
    void streamTrack_openEndedRangeReturnsThroughResourceEnd() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("0123456789".getBytes());
        given(trackService.getStreamResource(1L))
                .willReturn(new TrackService.StreamResource(resource, 10L));

        mockMvc.perform(get("/api/tracks/1/stream")
                        .header(HttpHeaders.RANGE, "bytes=4-"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 4-9/10"))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 6L))
                .andExpect(content().bytes("456789".getBytes()));
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
