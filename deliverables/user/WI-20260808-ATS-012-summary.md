---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: qa-integ
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260808-ATS-012-handoff.md
    reason: Approved independent validation scope and output contract
  - path: ../agent/WI-20260808-ATS-012-evidence-pack.md
    reason: Detailed commands, evidence pointers, findings, and rollback
  - path: REQ-20260808-ATS-003.md
    reason: Approved three-SR acceptance criteria
---
# WI-20260808-ATS-012 Work Summary

## 최종 결과

**PASS WITH MINOR — 완료.** SR-99~101의 핵심 현상, 원인, 영향 범위, 제안 경계는 공개 화면·API·스트림 크기·현재 코드·선행 Evidence Pack과 일치함. `BLOCKER 0`, `MAJOR 0`, `MINOR 2`이며 제품 코드·DB·공개 데이터·SR·인덱스는 수정하지 않았음.

## 독립 검증 결과

### SR-99

- Home은 Track 3을 `18:10`, Track 1·2를 각각 `3:49`로 표시함.
- 공개 API duration은 Track 1·2가 229초, Track 3이 1,090초이고, Range 전체 크기는 3,756,312 / 3,756,312 / 17,863,782 bytes임.
- 실제 플레이어 길이는 Track 1·2가 `1:33`, Track 3이 `7:26`으로 재현됨.
- 세 저장값 모두 현재 코드의 `floor(fileBytes / 16,384)` 결과와 일치하며, 오디오 교체 경로가 waveform만 재계산하고 duration은 갱신하지 않는 사실도 확인함.

### SR-100

- Home은 Genre와 Mood만 요청·표시함.
- 공개 Tag 수는 Genre 5, Mood 4, Instrument 4, Usage 1임.
- `instrument03`은 활성 Track 2·3에 연결되고 백엔드 검색도 2건을 반환하지만 Track 목록 화면에는 Instrument URL·상태·UI 연결이 없음.
- Usage Tag는 등록되어 있으나 활성 Track 연결·검색 결과가 0건임. Usage 우선 통합 탐색과 명시적 빈 상태 제안은 이 데이터 상태와 일치함.

### SR-101

- `waiting` 또는 `stalled` 이벤트는 경과 시간 검사 없이 즉시 `isStalled=true`를 설정하고, `timeupdate`·`canplay`·`playing` 등에서 해제됨.
- 앨범 2 API Track 항목은 ID·제목·아티스트·썸네일·순서만 전달하며 duration과 waveform이 없음. 반면 Track 2 상세에는 1,201자 waveform 데이터가 존재함.
- Album 상세의 컨텍스트·전체 재생·개별 재생은 모두 `duration: 0`, `waveformData: null`을 만들며, Playlist·Like·Download History·Drawer·History·queue/next/prev에도 같은 축약 데이터 영향이 확인됨.
- 이번 독립 브라우저 재실행에서는 이미 캐시된 Track 2가 즉시 재생되어 지연 문구가 다시 나타나지 않았음. 이는 이벤트 기반 조건과 모순되지 않으며, 약 1.8초 순간 노출은 WI-010의 기록된 재현으로 확인함.

## 발견 사항

1. **MINOR — SR-101 지연 안내 인용문 불일치**
   - `docs/SR/SR-101.md:14`는 `잠시 기다리거나 다시 시도해 주세요.`라고 인용함.
   - 실제 상수 `frontend/src/layouts/PlayerBar.tsx:16`은 `연결을 확인한 뒤 다시 시도해 주세요.`임.
   - 이벤트 조건·원인·개선 방향에는 영향이 없지만 구현 시 정확한 현재 문구로 정정하는 것이 좋음.

2. **MINOR — SR-99 비트레이트 단위 표기 혼재**
   - `docs/SR/SR-99.md:22,30,34`의 `128Ki-bps`와 같은 문서의 `128kbps` 표기가 혼재함.
   - 실제 식의 정확한 단위는 `128 Kibit/s (16,384 bytes/s)`임. 수치 결론에는 영향이 없지만 단위를 통일하는 것이 좋음.

## 문서·인덱스 검증

- 번호가 있는 SR 파일: 100개
- SR 인덱스 행: 100개
- 상태: 82 `DONE`, 15 `OPEN`, 2 `NOT CONFIRMED`, 1 `DROPPED`
- 전체 관리 문서 실제 합계·인덱스 합계: 202개
- 신규 SR 로컬 링크 오류: 0건
- H1·코드 펜스·EOF·후행 공백·문자 손상: 오류 0건
- `validate_docs.py`: PASS
- `git diff --check`: PASS
- 백엔드 대상 테스트: PASS
- 프론트 플레이어 테스트: 27/27 PASS
