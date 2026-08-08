---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: qa-fe
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260808-ATS-010-handoff.md
    reason: Approved Work Item scope and constraints
  - path: ../agent/WI-20260808-ATS-010-evidence-pack.md
    reason: Detailed runtime, code, and alternative-analysis evidence
  - path: REQ-20260808-ATS-003.md
    reason: Approved request and acceptance criteria
---
# WI-20260808-ATS-010 Work Summary

## 결론

두 현상은 원인이 다르며 모두 재생 신뢰성 SR에 포함할 가치가 있다.

1. `재생이 지연되고 있습니다...`는 현재 `HTMLAudioElement`의 `waiting` 또는 `stalled` 이벤트가 한 번만 발생해도 즉시 표시된다. 이는 재생 실패가 아니라 정상적인 최초 버퍼링에서도 발생할 수 있다. 앨범 2 전체 재생 인수 관찰에서도 문구가 먼저 나타났지만 약 1.8초 안에 재생으로 회복했다.
2. 앨범 전체 재생의 직선 waveform은 해당 음원의 실제 peak 데이터가 평평해서가 아니다. 앨범 API가 `duration`과 `waveformData`를 제공하지 않고, 화면이 강제로 `duration: 0`, `waveformData: null`인 플레이어 Track을 생성하기 때문이다. 현재 공개 Track 상세 API에서 같은 `trackId=2`의 waveform 문자열이 존재함을 확인했다.

## 재생 지연 문구의 현재 조건

- `waiting` 또는 `stalled`: 현재 곡이 있으면 즉시 `isStalled=true`
- `timeupdate`, `canplay`, `playing`: `isStalled=false`
- `play()` 성공: 재생 중 상태로 바꾸면서 지연 상태 해제
- 실제 미디어 `error`: 재생을 중단하고 별도 오류 문구 표시
- `play()` 거절: 재생 시작 실패 문구 표시

따라서 현재 문구는 "오류"가 아니라 "브라우저가 잠시 데이터를 기다리는 중"을 뜻한다. 다만 알림을 지연시키는 기준 시간이 없어 매우 짧은 정상 시작 지연도 사용자에게 장애처럼 보인다.

## waveform 누락 영향 범위

| 진입 화면 | duration | waveform | 결과 |
| --- | --- | --- | --- |
| 음원 목록·음원 상세 | API 값 전달 | API peak 전달 | 정상 경로 |
| 앨범 상세·전체 재생 | `0`으로 생성 | `null`로 생성 | 직선 fallback |
| 재생목록 상세·Drawer | `0` 또는 축약값 | `null`/미지정 | 직선 fallback |
| 좋아요 목록·Drawer | `0` | `null`/미지정 | 직선 fallback |
| 다운로드 기록 | API duration 전달 | `null` | 시간은 표시되나 직선 fallback |
| 로컬 재생 기록 Modal | 저장 정보 부족으로 `0` | 미지정 | 직선 fallback |

불완전한 Track 객체는 큐와 현재 화면 목록 컨텍스트에 그대로 들어간다. `다음 곡`/`이전 곡`도 별도 상세 조회 없이 그 객체를 다시 재생하므로 같은 결함이 이어지며, 로컬에 저장된 플레이어 상태로 재접속 후에도 남을 수 있다.

## 권고

### 1. 정상 시작 지연과 장애를 시간으로 구분

- `waiting`/`stalled` 직후에는 내부 버퍼링 시각만 기록하고 경고 문구는 바로 띄우지 않는다.
- 이번 약 1.8초 회복 관찰을 포함해 초기 기준값은 약 2초가 합리적이다. 기준값은 테스트 가능한 상수로 둔다.
- 기준 시간 안에 `timeupdate`/`canplay`/`playing`이 오면 아무 경고 없이 타이머를 취소한다.
- 기준 시간을 넘으면 비치명적 "버퍼링 중" 상태를 표시하고, 더 긴 지속 구간에서만 연결 확인과 다시 시도를 강조한다.
- 미디어 `error`와 `play()` 거절은 현재처럼 실제 오류 상태로 분리한다.
- 곡 변경, 일시정지, 오류, 재시도 시 이전 곡의 지연 타이머가 새 곡에 영향을 주지 않도록 generation/token으로 무효화한다.

### 2. 모든 재생 진입점에 하나의 데이터 계약 적용

- `PlayableTrack` 또는 동등한 공통 응답/매퍼를 정의하고 최소한 `id`, `title`, `artistName`, `duration`, `thumbnail`, `waveformData`를 필수 재생 메타데이터로 둔다.
- 앨범·재생목록·좋아요·다운로드 기록이 임의의 `0`/`null` Track을 만들지 않고 같은 계약을 사용하게 한다.
- 실제 peak가 없을 때만 직선 fallback을 사용하고, 존재하는 peak 대신 장식용 파형을 생성하지 않는다.
- 기존 로컬 `playerState`/재생 기록의 축약 데이터는 버전 마이그레이션하거나 재생 시 ID로 재수화한다.

### 3. N+1 없는 전달 방식

우선 권고는 컬렉션 API가 공통 재생 DTO를 함께 반환하는 방식이다. 앨범·재생목록·좋아요 저장소는 이미 Track을 `EntityGraph`로 함께 읽으므로 Track의 scalar인 duration/waveform을 추가해도 항목별 상세 API 호출이 필요 없다. 다운로드 기록은 현재 지연 연관 접근 가능성이 있으므로 Track fetch join/DTO projection 또는 ID 일괄 조회를 함께 적용해야 한다.

목록 payload가 우려되면 두 번째 선택지는 `trackIds` 한 번으로 재생 메타데이터를 일괄 조회하는 batch hydration이다. 클릭할 때마다 각 곡의 상세 API를 반복 호출하는 방식은 앨범 전체 재생과 큐 전환에서 N+1·재생 지연을 만들 수 있으므로 권고하지 않는다.

## SR-101에 포함할 검증 항목

- fake timer로 2초 미만 회복, 임계시간 초과, 장시간 버퍼링, 오류 전환을 각각 검증
- `timeupdate`/`canplay`/`playing`, pause, 곡 변경, retry가 타이머와 상태를 해제하는지 검증
- 앨범·재생목록·좋아요·다운로드·재생 기록에서 실제 waveform과 duration을 플레이어에 전달하는지 검증
- 전체 재생과 다음/이전 곡 전환에서도 각 곡의 waveform이 유지되는지 검증
- 빈 peak의 직선 fallback과 실제 peak 기반 bar 렌더링을 구분해 검증
- API 조회 수가 항목 수에 비례해 증가하지 않는지 query-count 또는 batch 호출 테스트로 검증

## 변경 범위

- 제품 코드, SR, 인덱스, DB 및 공개 사이트 데이터는 수정하지 않았다.
- 이 WI의 사용자 요약과 Evidence Pack만 생성했다.
