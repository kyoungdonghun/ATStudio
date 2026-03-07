---
wi_id: WI-20260307-ATS-008
req_id: REQ-20260307-ATS-008
date: 2026-03-07
author: docops
status: complete
---

# WI-20260307-ATS-008 — Track/Tag 도메인 교차 검증 요약

> Phase 1 Batch2 — api-spec §1/§2 · usecase · front-list · modal-list · screen-flow 교차 검증 결과

---

## 발견 건수 요약

| 심각도 | 건수 |
|--------|------|
| CRITICAL | 1 |
| MAJOR | 4 |
| MINOR | 5 |
| SUGGESTION | 2 |
| **합계** | **12** |

---

## 주요 이슈 목록

### CRITICAL (1건)

**C-01** K-7 화면이 "비활성 포함 전체 트랙 목록"을 요구하나, api-spec 1.2는 is_active=1 트랙만 반환하도록 명시되어 있음.
- front-list K-7이 `[ADMIN]` 전용 화면임에도 별도 Admin용 목록 API(비활성 포함)가 정의되어 있지 않음.
- 구현 시 공개 API를 어드민이 재사용하거나, 별도 Admin API 신설이 필요 — 방향 미확정이면 구현 불가.

### MAJOR (4건)

**M-01** api-spec 1.1/1.2 필드명 `tonality` vs usecase SOUND-001/SOUND-005 "key" — 동일 개념에 두 가지 용어 혼용.

**M-02** front-list B-1 (음원 상세) API 참조에 `4.1 POST /api/play-histories` 누락.
- SOUND-010 UC Step 6: "frontend simultaneously calls SOUND-004 (save play history)" 명시.
- front-list는 streaming API(`1.4`)만 기재하고 재생 기록 API를 나열하지 않음.

**M-03** front-list Screen 1, Screen 3 API 참조에 좋아요(`10.1 POST /api/likes/{trackId}`) 및 장바구니(`11.1 POST /api/download-queue`) 누락.
- screen-flow §3에서 Screen 1/3에 두 액션 모두 정의되어 있으나 front-list에는 기재되지 않음.

**M-04** modal-list M-12가 `SOUND-019` UC를 참조하나, sound-track.md에 SOUND-019 정의 없음.

### MINOR (5건)

**N-01** api-spec 1.7 (Delete Track) 설명에 track_tags 삭제 동작이 누락.
- SOUND-016 UC Step 6: "Backend deletes the tag mapping records for this track from track_tags" 명시.

**N-02** sound-tag.md "Tag List Query"에 UC 코드 미부여.
- 나머지 Tag UC(SOUND-003, SOUND-014, SOUND-018)와 달리 코드 없이 Sub UC로만 처리.

**N-03** api-spec 2.2 (List Tags) 응답 형식이 raw array `[...]` — V3 표준(`{"dataList": [...]}`) 미적용.
- (기존 SUGGESTION-002로 기록된 기지 이슈이나, 미해결 상태로 정합성 영향 있음)

**N-04** front-list Screen 6 (음원 업로드) 및 Screen 7 (음원 수정)에 SOUND-001/SOUND-012 관련 비동기 preview_file 생성 동작에 대한 UX 처리(로딩/실패 안내) 기재 없음.

**N-05** screen-flow §3 태그 필터("인라인 CHIP/SELECT 형태, 모달 전환 가능성 열어둠")에 대응하는 모달이 modal-list에 미정의 (M-03/M-04는 업로드/수정 전용).

### SUGGESTION (2건)

**S-01** api-spec 1.5 (Download Track) Error Cases에 `errorCode` 값만 있고 SOUND-011 UC가 기술한 "오늘의 다운로드 카운트 계산 방식(DATE(downloaded_at) = CURDATE())" 등 백엔드 로직 설명이 누락 — 추후 API spec에 간단한 Notes 섹션 추가 권장.

**S-02** SOUND-010 UC에서 SOUND-004를 "Related UC"로 기재하나, 이 배치 범위(§1 Tracks)에서 SOUND-004는 sound-track.md에 정의되지 않음 — 교차 참조 가시성 향상을 위해 UC 파일 상단 "Related Files" 섹션 추가 권장.

---

## 검토 의사결정 필요 항목

| 항목 | 설명 | 우선순위 |
|------|------|---------|
| C-01 | K-7 어드민 트랙 목록 — 비활성 포함 별도 API 신설 여부 | 높음 (Phase 3 착수 전 결정 필요) |
| M-01 | `tonality` vs `key` 통일 방향 결정 | 중간 |
| N-03 | api-spec 2.2 응답 형식 V3 표준 적용 여부 | 낮음 (기지 이슈) |

---

> 상세 발견 목록 (파일:섹션 포인터 포함): `deliverables/agent/WI-20260307-ATS-008-evidence-pack.md`
