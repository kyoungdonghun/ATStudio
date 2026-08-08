---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: qa-integ
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260808-ATS-008-handoff.md
    reason: Approved Work Item scope and runtime evidence
  - path: ../agent/WI-20260808-ATS-008-evidence-pack.md
    reason: Detailed cross-layer evidence and reproduction commands
  - path: REQ-20260808-ATS-003.md
    reason: Approved request and acceptance criteria
---
# WI-20260808-ATS-008 Work Summary

## 결론

- `Space Oddity - T0Ro, MELE - 경동훈 (2)`만의 파일 문제가 아니다. 공개된 세 트랙 모두 DB/API 길이가 실제 미디어 길이보다 약 2.45배 길다.
- 세 저장값은 MP3 파일 크기를 `128 * 1024 / 8` bytes/s로 나누고 소수점을 버리는 현재 코드의 결과와 정확히 일치한다.
- 실제 미디어 길이와 파일 크기로 계산한 평균 비트레이트는 약 320kbps다. 따라서 128kbps 고정 추정이 3:49를 1:33 대신, 18:10을 7:26 대신 표시하게 만든 직접 원인이다.
- 스트림 파일 자체는 정상이며 플레이어는 브라우저의 `loadedmetadata` 후 실제 미디어 길이로 교정한다. 반면 홈과 음원 목록은 저장된 `duration`을 그대로 표시하므로 서로 다른 시간이 보인다.
- 음원 파일 교체 경로는 waveform을 다시 만들지만 duration은 다시 계산하지 않는다. 최초 업로드 계산을 바로잡는 것과 별도로 교체 시 메타데이터 원자 갱신이 필요하다.

## 공개 런타임 교차검증

| Track | 제목 | API/홈 길이 | 브라우저 미디어 길이 | 오차 | 스트림 크기 | 현재 128Ki-bps 계산 | 실제 평균 비트레이트 추정 |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | 시골마을의 전학생 | 229초 (3:49) | 93초 (1:33) | +136초 | 3,756,312 bytes | 229초 | 약 323.1kbps |
| 2 | 시골마을의 전학생 | 229초 (3:49) | 93초 (1:33) | +136초 | 3,756,312 bytes | 229초 | 약 323.1kbps |
| 3 | Space Oddity - T0Ro, MELE - 경동훈 (2) | 1,090초 (18:10) | 446초 (7:26) | +644초 | 17,863,782 bytes | 1,090초 | 약 320.4kbps |

- API 값과 스트림 크기는 2026-08-08 공개 API 및 `Range: bytes=0-0` 응답으로 다시 확인했다.
- 실제 미디어 길이는 승인된 핸드오프에 기록된 브라우저 플레이어 메타데이터 관찰값이다.
- 약 320kbps는 `bytes * 8 / actualSeconds / 1000`으로 계산한 평균값이다. 이 증거만으로 CBR 또는 VBR 여부까지 확정하지는 않는다.

## 확인된 원인

1. 최초 업로드는 `TrackService.extractDuration()`에서 MP3 크기를 128kbps로 가정해 초 단위 길이를 만든다.
2. 계산된 정수는 `tracks.duration`에 저장되고 목록·상세·관리자·다운로드 기록 DTO로 그대로 전달된다.
3. 홈의 신규 음원과 음원 목록은 이 API 값을 분·초로 포맷하므로 각각 18:10, 3:49를 표시한다.
4. 실제 재생을 시작하면 `HTMLAudioElement.duration`이 로드되고 플레이어 상태가 실제 값으로 교체되어 7:26 또는 1:33으로 보인다.
5. 관리자 음원 수정에서 파일을 교체하면 오디오 파일과 waveform만 바뀌고 기존 duration은 남는다. 새 파일 길이와 무관한 이전 값이 유지될 수 있다.

## 영향 범위

### 잘못된 저장값을 직접 표시하거나 전달하는 영역

- `GET /api/tracks`: 홈 신규 음원 및 공개 음원 목록
- `GET /api/tracks/{id}`: 음원 상세와 플레이어 초기 fallback 값
- `GET /api/tracks/admin`, `GET /api/tracks/admin/{id}`, 음원 생성·수정 응답
- `GET /api/downloads/history`: 다운로드 기록에서 생성하는 플레이어 Track과 큐
- 홈 신규 음원 시간 표시
- 공개 음원 목록의 `TrackRow` 시간 표시
- 재생 메타데이터가 준비되기 전 PlayerBar 시간·seek 범위의 초기 fallback

### 직접 오출력되지 않는 영역

- 스트림 및 다운로드 응답은 저장된 `duration`으로 파일을 자르지 않고 실제 파일 bytes를 전달한다.
- 현재 앨범·플레이리스트 축약 Track DTO는 duration 자체를 포함하지 않는다. 해당 화면의 waveform/축약 계약 문제는 WI-010의 별도 조사 범위다.
- PlayerBar는 `loadedmetadata`와 `timeupdate` 이후 브라우저가 읽은 실제 길이를 우선 사용하므로 재생 중 최종 시간은 맞아진다.

## SR-99 권고 요구사항

### 정확한 추출

1. 파일 크기와 고정 비트레이트로 duration을 추정하지 않는다.
2. MP3의 decoded PCM frame 수와 sample rate 또는 검증된 미디어 메타데이터 파서를 사용해 CBR, VBR, ID3가 포함된 파일 모두에서 실제 길이를 산출한다.
3. 현재 waveform 처리가 MP3를 PCM으로 끝까지 읽으므로, 한 번의 오디오 분석에서 `duration`과 200개 peak를 함께 반환하는 구조를 우선 검토한다.
4. 초 단위 정수 변환의 반올림 정책을 API 계약으로 고정하고 프론트 포맷과 일치시킨다.
5. 메타데이터 추출에 실패한 create/오디오 교체는 추정값이나 0을 저장하지 말고 명시적 업로드 오류로 거절한다.

### 생성과 파일 교체의 동일 계약

- 최초 생성과 오디오 파일 교체가 동일한 분석기를 사용해야 한다.
- 교체 시 새 파일의 duration과 waveform을 저장 교체 전에 모두 계산·검증하고, 파일 경로·duration·waveform이 함께 성공하거나 함께 실패하도록 구성한다.
- 제목·태그 등 오디오가 바뀌지 않는 수정은 기존 duration을 유지한다.

### 기존 데이터 audit와 backfill

1. active/inactive 전체 Track을 대상으로 저장 파일 존재 여부, 기존 duration, 정확히 추출한 duration, 차이, 추출 오류를 기록하는 dry-run 보고서를 먼저 만든다.
2. 허용 오차를 정한 뒤 차이가 큰 행만 명시적으로 승인받아 배치 갱신한다.
3. 갱신 전 값과 결과를 남겨 롤백할 수 있게 하고, 실패 항목은 건너뛰되 원인을 별도 보고한다.
4. backfill 후 목록·상세·관리자·다운로드 기록 API가 동일한 값을 반환하는지 표본 및 전체 건수로 검증한다.
5. 현재 세 곡은 모두 보정 후보이며, 이 WI에서는 DB를 변경하지 않았다.

## 필수 테스트

- 128kbps CBR, 320kbps CBR, VBR(Xing/VBRI 포함), ID3v2/앨범아트 포함 MP3의 실제 길이 추출
- 기존 WAV 길이·waveform 회귀
- 잘린 파일, 잘못된 확장자, 읽기 오류에서 명시적 실패와 DB/스토리지 무변경 확인
- create가 추출된 duration과 waveform을 함께 저장하는지 확인
- 오디오 교체가 두 값을 함께 갱신하고 메타데이터 전용 수정은 duration을 유지하는지 확인
- 목록·상세·관리자·다운로드 기록 API가 보정된 동일 duration을 반환하는지 확인
- 홈과 TrackRow가 93초를 1:33, 446초를 7:26으로 표시하는지 확인
- 브라우저 `loadedmetadata` 값과 서버 duration의 허용 오차 이내 일치 및 seek/진행률 정상 동작 확인
- dry-run, 부분 실패, 재실행 가능성, 롤백을 포함한 backfill 통합 테스트

## 현재 테스트의 의미

- `TrackServiceAudioProcessingTest`는 MP3 32,768 bytes가 128kbps 가정으로 2초가 되는 것을 정상으로 기대한다.
- 해당 테스트 클래스는 현재 상태에서 통과했지만, 이는 결함이 없다는 의미가 아니라 잘못된 고정 비트레이트 계약이 테스트로 고정되어 있음을 뜻한다.
- 후속 구현에서는 이 테스트를 실제 MP3 메타데이터 기반 CBR/VBR fixture 테스트로 교체해야 한다.

## 사실·추론·제안 구분

- **사실:** API duration, Range 응답 파일 크기, 코드의 128Ki-bps 식, 생성·교체 경로, DTO와 화면의 소비 방식, 현재 테스트 기대값.
- **관찰:** 브라우저 플레이어가 표시한 1:33, 1:33, 7:26.
- **추론:** 세 파일의 평균 비트레이트가 약 320kbps이며 고정 128kbps 추정이 공통 오차를 발생시켰다.
- **제안:** 통합 오디오 분석기, 실패 시 저장 거절, 기존 데이터 dry-run/backfill, 회귀 테스트와 런타임 정합성 검증.

## 변경 파일

- `deliverables/user/WI-20260808-ATS-008-summary.md`
- `deliverables/agent/WI-20260808-ATS-008-evidence-pack.md`

제품 코드, SR 문서, 인덱스, DB 및 공개 데이터는 변경하지 않았다.
