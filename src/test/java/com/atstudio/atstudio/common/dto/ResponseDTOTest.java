package com.atstudio.atstudio.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResponseDTO 빌더 및 직렬화 테스트")
class ResponseDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── withMessage ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("withMessage: message만 설정되고 나머지는 null이다")
    void withMessage_onlyMessageSet() {
        ResponseDTO<String> dto = ResponseDTO.<String>withMessage()
                .message("success")
                .build();

        assertThat(dto.getMessage()).isEqualTo("success");
        assertThat(dto.getData()).isNull();
        assertThat(dto.getDataList()).isNull();
        assertThat(dto.getPageInfo()).isNull();
    }

    @Test
    @DisplayName("withMessage: JSON 직렬화 시 null 필드는 포함되지 않는다 (NON_NULL)")
    void withMessage_nullFieldsOmittedInJson() throws Exception {
        ResponseDTO<String> dto = ResponseDTO.<String>withMessage()
                .message("success")
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"message\":\"success\"");
        assertThat(json).doesNotContain("\"data\"");
        assertThat(json).doesNotContain("\"dataList\"");
        assertThat(json).doesNotContain("\"pageInfo\"");
    }

    // ── withSingleData ────────────────────────────────────────────────────────

    @Test
    @DisplayName("withSingleData: message와 data가 설정되고 나머지는 null이다")
    void withSingleData_messageAndDataSet() {
        ResponseDTO<String> dto = ResponseDTO.<String>withSingleData()
                .message("조회 성공")
                .data("track-data")
                .build();

        assertThat(dto.getMessage()).isEqualTo("조회 성공");
        assertThat(dto.getData()).isEqualTo("track-data");
        assertThat(dto.getDataList()).isNull();
        assertThat(dto.getPageInfo()).isNull();
    }

    @Test
    @DisplayName("withSingleData: JSON 직렬화 시 null 필드는 포함되지 않는다 (NON_NULL)")
    void withSingleData_nullFieldsOmittedInJson() throws Exception {
        ResponseDTO<String> dto = ResponseDTO.<String>withSingleData()
                .message("조회 성공")
                .data("track-data")
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"message\"");
        assertThat(json).contains("\"data\"");
        assertThat(json).doesNotContain("\"dataList\"");
        assertThat(json).doesNotContain("\"pageInfo\"");
    }

    // ── withAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("withAll: dataList와 pageInfo가 설정되고 나머지는 null이다")
    void withAll_dataListAndPageInfoSet() {
        List<String> list = List.of("track1", "track2");
        PageInfo pageInfo = PageInfo.of(1, 10, 25, 10);

        ResponseDTO<String> dto = ResponseDTO.<String>withAll()
                .dataList(list)
                .pageInfo(pageInfo)
                .build();

        assertThat(dto.getDataList()).containsExactly("track1", "track2");
        assertThat(dto.getPageInfo()).isNotNull();
        assertThat(dto.getMessage()).isNull();
        assertThat(dto.getData()).isNull();
    }

    @Test
    @DisplayName("withAll: JSON 직렬화 시 null 필드는 포함되지 않는다 (NON_NULL)")
    void withAll_nullFieldsOmittedInJson() throws Exception {
        List<String> list = List.of("track1");
        PageInfo pageInfo = PageInfo.of(1, 10, 10, 10);

        ResponseDTO<String> dto = ResponseDTO.<String>withAll()
                .dataList(list)
                .pageInfo(pageInfo)
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"dataList\"");
        assertThat(json).contains("\"pageInfo\"");
        assertThat(json).doesNotContain("\"message\"");
        assertThat(json).doesNotContain("\"data\":");
    }
}
