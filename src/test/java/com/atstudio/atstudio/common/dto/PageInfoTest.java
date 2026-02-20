package com.atstudio.atstudio.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageInfo 페이지네이션 계산 테스트")
class PageInfoTest {

    @Test
    @DisplayName("첫 블록: page=1, total=25 → start=1, end=3, prev=false, next=false")
    void firstBlock_fewPages() {
        PageInfo pageInfo = PageInfo.of(1, 10, 25, 10);

        assertThat(pageInfo.getPage()).isEqualTo(1);
        assertThat(pageInfo.getSize()).isEqualTo(10);
        assertThat(pageInfo.getTotal()).isEqualTo(25);
        assertThat(pageInfo.getStart()).isEqualTo(1);
        assertThat(pageInfo.getEnd()).isEqualTo(3);
        assertThat(pageInfo.isPrev()).isFalse();
        assertThat(pageInfo.isNext()).isFalse();
    }

    @Test
    @DisplayName("첫 블록 중간: page=5, total=150 → start=1, end=10, prev=false, next=true")
    void firstBlock_manyPages() {
        PageInfo pageInfo = PageInfo.of(5, 10, 150, 10);

        assertThat(pageInfo.getStart()).isEqualTo(1);
        assertThat(pageInfo.getEnd()).isEqualTo(10);
        assertThat(pageInfo.isPrev()).isFalse();
        assertThat(pageInfo.isNext()).isTrue();
    }

    @Test
    @DisplayName("두 번째 블록: page=11, total=200 → start=11, end=20, prev=true, next=false")
    void secondBlock() {
        PageInfo pageInfo = PageInfo.of(11, 10, 200, 10);

        assertThat(pageInfo.getStart()).isEqualTo(11);
        assertThat(pageInfo.getEnd()).isEqualTo(20);
        assertThat(pageInfo.isPrev()).isTrue();
        assertThat(pageInfo.isNext()).isFalse();
    }

    @Test
    @DisplayName("엣지케이스: total=0 → start=1, end=0, prev=false, next=false")
    void edgeCase_totalZero() {
        PageInfo pageInfo = PageInfo.of(1, 10, 0, 10);

        assertThat(pageInfo.getTotal()).isEqualTo(0);
        assertThat(pageInfo.getStart()).isEqualTo(1);
        assertThat(pageInfo.getEnd()).isEqualTo(0);
        assertThat(pageInfo.isPrev()).isFalse();
        assertThat(pageInfo.isNext()).isFalse();
    }

    @Test
    @DisplayName("RequestDTO 오버로드: PageInfo.of(request, total) 사용 시 blockSize=10 적용")
    void ofWithRequestDTO() {
        RequestDTO request = new RequestDTO();
        // 기본값: page=1, size=10

        PageInfo pageInfo = PageInfo.of(request, 25);

        assertThat(pageInfo.getPage()).isEqualTo(1);
        assertThat(pageInfo.getSize()).isEqualTo(10);
        assertThat(pageInfo.getTotal()).isEqualTo(25);
        assertThat(pageInfo.getStart()).isEqualTo(1);
        assertThat(pageInfo.getEnd()).isEqualTo(3);
        assertThat(pageInfo.isPrev()).isFalse();
        assertThat(pageInfo.isNext()).isFalse();
    }
}
