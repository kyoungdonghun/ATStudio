---
wi_id: WI-20260307-ATS-008
req_id: REQ-20260307-ATS-008
date: 2026-03-07
author: docops
status: complete
batch: Phase1-Batch2 (Track/Tag)
---

# WI-20260307-ATS-008 — Evidence Pack: Track/Tag 교차 검증

> Phase 1 Batch2 상세 발견 목록. 각 항목에 파일:섹션/라인 포인터 포함.

---

## 검증 대상 파일 목록

| 파일 | 읽은 범위 |
|------|----------|
| `docs/design/api-spec.md` | §1 (1.1~1.7), §2 (2.1~2.4) |
| `docs/design/usecase/sound-track.md` | 전체 (SOUND-001, 005, 006, 010, 011, 012, 016) |
| `docs/design/usecase/sound-tag.md` | 전체 (SOUND-003, Tag List Query, SOUND-014, SOUND-018) |
| `docs/ui/atstudio-front-list.md` | Screen 1, 3, 6, 7, B-1, K-6, K-7 항목 |
| `docs/ui/modal-list.md` | M-03, M-04, M-11, M-12 항목 |
| `docs/ui/screen-flow.md` | §3 음원 탐색 흐름 |

---

## 발견 목록

### CRITICAL

---

**[CONFLICT] CRITICAL: C-01**
- 파일A: `docs/ui/atstudio-front-list.md` — K-7 행: `1.2 GET /api/tracks` (비활성 포함)
- 파일B: `docs/design/api-spec.md` §1.2 — "Returns only active (is_active=1) tracks"
- 설명: front-list K-7 (트랙 관리) 화면은 비활성 트랙까지 포함하여 조회해야 한다고 주석 기재하고 있음. 그러나 api-spec 1.2는 PUBLIC 공개 API로서 is_active=1 필터가 고정되어 있음. 어드민이 비활성 포함 목록을 볼 수 있는 별도 API가 §1에 정의되어 있지 않음.
- 영향: 구현 시 1.2 API를 어드민용으로 분기 처리하거나 신규 Admin-only API를 추가해야 함 — 방향 미정이면 K-7 구현 불가.
- 권고: Phase 2 컨펌 시 결정 필요. 옵션 A: api-spec 1.2에 ADMIN 권한 시 is_active 필터 생략 분기 추가. 옵션 B: 신규 `GET /api/admin/tracks` 엔드포인트 정의.

---

### MAJOR

---

**[CONFLICT] MAJOR: M-01**
- 파일A: `docs/design/api-spec.md` §1.1 Request 필드명 — `tonality: String`
- 파일A: `docs/design/api-spec.md` §1.2 Response 필드명 — `"tonality": "C"`
- 파일A: `docs/design/api-spec.md` §1.3 Response 필드명 — `"tonality": "C"`
- 파일B: `docs/design/usecase/sound-track.md` SOUND-001 Main Flow Step 1 — "Admin enters metadata (title, BPM, **key**, description)"
- 파일B: `docs/design/usecase/sound-track.md` SOUND-005 Postconditions — "title, BPM, **key**, thumbnail, playCount, tags"
- 파일B: `docs/design/usecase/sound-track.md` SOUND-006 Postconditions — "title, BPM, **key**, description, tags, playCount, audioFile path"
- 설명: API spec은 `tonality`를 필드명으로 사용하고, UC는 자연어 설명에서 "key"를 사용. 동일 개념에 두 용어 혼용.
- 영향: 프론트엔드 개발자가 UC를 보고 "key" 필드명을 기대하고 API를 보면 "tonality"를 발견하게 됨 — 혼동 유발.
- 권고: glossary.md에 `tonality` / `key` 관계 명확화 필요. UC 텍스트를 "tonality (key)" 형태로 통일하거나 UC에서도 `tonality`로 단일화 권장.

---

**[GAP] MAJOR: M-02**
- 파일A: `docs/design/usecase/sound-track.md` SOUND-010 Main Flow Step 6 — "If member: frontend simultaneously calls SOUND-004 (save play history) when QueBar playback starts."
- 파일B: `docs/ui/atstudio-front-list.md` B-1 행 관련 API — `1.3 GET /api/tracks/{trackId}` `1.4 GET /api/tracks/{trackId}/stream` 만 기재
- 설명: SOUND-010 UC는 재생 시작 시 프론트엔드가 `4.1 POST /api/play-histories`를 호출해야 함을 명시. front-list B-1 화면의 관련 API 목록에 이 API가 누락되어 있음.
- 영향: 프론트 구현 시 B-1 화면 스펙만 보면 재생 기록 API 호출 누락 위험. QA 시 재생 기록 미저장 버그로 이어질 수 있음.
- 권고: front-list B-1 관련 API에 `4.1 POST /api/play-histories` (재생 시 호출, 회원만) 추가 필요.

---

**[GAP] MAJOR: M-03**
- 파일A: `docs/ui/screen-flow.md` §3 음원 탐색 흐름 — Screen 1/3 인라인 버튼: "좋아요 클릭 → 10.1 POST /api/likes/{trackId}", "장바구니 담기 클릭 → 11.1 POST /api/download-queue"
- 파일B: `docs/ui/atstudio-front-list.md` Screen 1 행 — `1.2 GET /api/tracks` `2.2 GET /api/tags` 만 기재
- 파일B: `docs/ui/atstudio-front-list.md` Screen 3 행 — `1.2 GET /api/tracks` 만 기재
- 설명: screen-flow는 Screen 1과 Screen 3에서 좋아요 및 장바구니 인라인 액션이 동작함을 정의. 그러나 front-list의 Screen 1, 3 API 참조 컬럼에 두 API 모두 미기재.
- 영향: front-list 기준으로 화면 구현 시 좋아요/장바구니 기능 누락 위험.
- 권고: front-list Screen 1 관련 API에 `10.1~10.3 /api/likes`, `11.1 POST /api/download-queue` 추가. Screen 3에도 동일 액션 API 기재 필요.

---

**[GAP] MAJOR: M-04**
- 파일A: `docs/ui/modal-list.md` M-12 UC 컬럼 — `SOUND-019`
- 파일B: `docs/design/usecase/sound-track.md` — SOUND-019 정의 없음 (파일 내 정의된 UC: SOUND-001, 005, 006, 010, 011, 012, 016)
- 설명: modal-list M-12가 발생 근거 UC로 SOUND-019를 참조하나, sound-track.md에 해당 코드가 없음. SOUND-019가 다른 usecase 파일에 정의되어 있을 가능성 있으나 이 배치 범위 내에서는 확인 불가.
- 영향: M-12의 UC 추적 불가. SOUND-019가 미정의 또는 잘못된 파일 참조일 경우 감사 추적 단절.
- 권고: SOUND-019가 sound-track.md에 있어야 한다면 파일에 UC 추가. 다른 파일(예: sound-playlist.md)에 있다면 modal-list M-12의 UC 출처 주석 또는 링크 명시 필요.

---

### MINOR

---

**[GAP] MINOR: N-01**
- 파일A: `docs/design/usecase/sound-track.md` SOUND-016 Main Flow Step 6 — "Backend deletes the tag mapping records for this track from track_tags."
- 파일B: `docs/design/api-spec.md` §1.7 (Delete Track) Description — "Soft delete (deactivate with is_active=0)" 만 기재, track_tags 삭제 동작 미언급
- 설명: SOUND-016 UC는 track_tags 레코드 삭제를 명시하나 API spec 1.7에는 이 동작이 설명되지 않음.
- 영향: API spec만 보는 프론트 또는 API 통합 테스트 작성자가 이 부수 효과(side effect)를 놓칠 수 있음. 경미한 문서 불완전성.
- 권고: api-spec 1.7 Description 또는 별도 Notes 항목에 "Associated track_tags records are deleted" 추가 권장.

---

**[GAP] MINOR: N-02**
- 파일A: `docs/design/usecase/sound-tag.md` — "Tag List Query" 섹션: "Sub UC (no separate code)"
- 파일B: `docs/design/api-spec.md` §2.2 (List Tags) — 정식 API 정의
- 설명: SOUND-003, SOUND-014, SOUND-018 모두 `SOUND-XXX` 코드를 가지나, 태그 목록 조회 UC는 코드 없이 Sub UC로만 처리됨. API 2.2는 존재하나 추적 가능한 UC 코드가 없음.
- 영향: 태그 목록 조회 관련 버그/이슈 추적 시 UC 기준 참조 불가. 경미.
- 권고: `SOUND-002` 등 적절한 코드 부여 권장 (Sub UC 패턴 유지 여부는 팀 결정).

---

**[CONFLICT] MINOR: N-03**
- 파일A: `docs/design/api-spec.md` §2.2 (List Tags) Response — raw array `[{"id":1,"name":"Happy","type":"MOOD"}, ...]`
- 파일B: `docs/design/api-spec.md` Common Rules v3 표준 — `{"dataList": [...]}` 래핑 형식
- 설명: v3 변경 이력에서 "List field name: `content` → `dataList`"를 적용했으나 2.2는 여전히 raw array 반환. 기지 이슈(SUGGESTION-002)이나 미해결 상태.
- 영향: 프론트엔드 응답 처리 로직에서 Track 목록(dataList)과 Tag 목록(raw array)을 다르게 처리해야 함 — 일관성 파손.
- 권고: api-spec 2.2 응답을 `{"dataList": [...]}` 형태로 정정하고 백엔드 TagController 반환 타입도 일치시킴 (별도 WI 처리 권장).

---

**[OMISSION] MINOR: N-04**
- API/UC: `docs/design/api-spec.md` §1.1, §1.6 — 비동기 preview_file 생성 / 재생성 동작 정의
- 화면: `docs/ui/atstudio-front-list.md` Screen 6 (음원 업로드), Screen 7 (음원 수정)
- 설명: api-spec 1.1/1.6은 업로드/수정 후 비동기로 preview_file이 생성됨을 명시. 생성 실패 시 preview_file=NULL 상태로 유지됨. 그러나 front-list Screen 6/7에는 이 비동기 처리에 대한 UI 고려사항(로딩 표시, 실패 안내, 재시도 등)이 기재되어 있지 않음.
- 영향: 프론트 구현 시 업로드 완료 후 preview_file 미생성 상태에 대한 UX 처리 누락 위험. 경미하나 품질 영향 있음.
- 권고: front-list Screen 6/7 비고란에 "비동기 preview 생성 — 완료 전 재생 시 audio_file fallback" 안내 문구 추가 권장.

---

**[OMISSION] MINOR: N-05**
- 파일A: `docs/ui/screen-flow.md` §3 — "태그 필터: 인라인 CHIP/SELECT 형태 (모달 전환 가능성 열어둠)"
- 파일B: `docs/ui/modal-list.md` — M-03 (Screen 6 업로드 태그 선택), M-04 (Screen 7 수정 태그 선택) 만 정의. 메인화면/목록화면 태그 필터용 모달은 미정의.
- 설명: screen-flow는 메인(Screen 1) 및 음원 목록(Screen 3)의 태그 필터가 향후 모달로 전환될 수 있음을 열어두나, 해당 경우에 대한 모달 항목이 modal-list에 정의되어 있지 않음.
- 영향: 모달 전환 결정 시 modal-list 업데이트 필요함을 추적할 포인터 없음. 경미한 문서 불완전성.
- 권고: modal-list Deferred Items 또는 screen-flow §3에 "모달 전환 시 M-XX 추가 필요" 주석 추가 권장.

---

### SUGGESTION

---

**[SUGGESTION] S-01**
- 관련 파일: `docs/design/api-spec.md` §1.5 (Download Track) Error Cases
- 관련 UC: `docs/design/usecase/sound-track.md` SOUND-011 Main Flow Step 4
- 내용: SOUND-011 UC Step 4는 "Backend calculates today's download count via COUNT query on track_downloads (DATE(downloaded_at) = CURDATE())" 등 다운로드 카운트 산출 방법을 설명하나 api-spec 1.5에는 이런 로직 설명이 없음. api-spec에 Notes 섹션 또는 관련 util API(`UTIL-006`) 참조 포인터 추가 권장.

---

**[SUGGESTION] S-02**
- 관련 파일: `docs/design/usecase/sound-track.md` SOUND-010 Related UC 컬럼 — `SOUND-004`
- 내용: SOUND-010 Related UC에 `SOUND-004`가 나오나, sound-track.md에는 SOUND-004가 없음. SOUND-004는 다른 파일(play history 도메인)에 있을 것으로 추정되나 이 배치 내에서 확인 불가. UC 파일 상단에 "External UC References" 섹션을 추가하여 타 도메인 UC 참조 시 파일 경로를 명시하는 패턴 도입 권장.

---

## 검증 완료 항목 (이상 없음)

| 검증 항목 | 결과 |
|----------|------|
| api-spec §1 vs front-list Screen 6 태그 API 참조 (M-03) | 일치 — 2.2 사용 ✓ |
| api-spec §1 vs front-list Screen 7 태그 API 참조 (M-04) | 일치 — 2.2 사용 ✓ |
| api-spec §1.7 vs front-list K-7 delete API 참조 | 일치 — 1.7 사용 ✓ |
| modal-list M-03/M-04 컴포넌트 타입 (SelectModal) vs 사용 API (2.2) | 일치 ✓ |
| modal-list M-11 (K-7 트랙 삭제) vs api-spec 1.7 | 일치 ✓ |
| modal-list M-12 발생 화면 (1/3/B-1) vs front-list | 일치 ✓ |
| api-spec §2.1 (Create Tag) vs SOUND-003 UC 필드 (name, type) | 일치 ✓ |
| api-spec §2.3 (Update Tag) vs SOUND-014 UC 필드 (name, type) | 일치 ✓ |
| api-spec §2.4 (Delete Tag) vs SOUND-018 UC 흐름 (204 No Content) | 일치 ✓ |
| screen-flow §3 B-1 진입 경로 (곡명 클릭) vs front-list B-1 | 일치 ✓ |
| screen-flow §3 M-12 SelectModal 흐름 vs modal-list M-12 | 일치 ✓ |
| api-spec §1.4 streaming AUTH ([PUBLIC]) vs front-list B-1 인증 표시 | 일치 ✓ |
| front-list K-6 (태그 관리) API 참조 (2.1, 2.3, 2.4) vs api-spec §2 | 일치 ✓ |
| screen-flow §3 태그 필터 → api-spec 2.2 사용 | 일치 ✓ |

---

## Acceptance Criteria 체크

| 기준 | 결과 |
|------|------|
| api-spec §1/§2 vs usecase 2종 교차 검증 완료 | ✓ |
| usecase vs front-list 화면(Screen 1, 3, 6, 7, B-1) 검증 완료 | ✓ |
| front-list vs modal-list (M-03, M-04, M-11, M-12) 검증 완료 | ✓ |
| screen-flow §3 vs 위 문서들 정합 검증 완료 | ✓ |
| 각 발견 항목에 파일:섹션 포인터 포함 | ✓ |
| 심각도 분류 명시 | ✓ |
| 발견 없는 영역도 "검증 완료" 명시 | ✓ |

---

## Related Documents

- `deliverables/user/WI-20260307-ATS-008-summary.md` — 사용자 향 요약
- `deliverables/agent/WI-20260307-ATS-008-handoff.md` — 핸드오프 패킷 (추적용)
- `deliverables/user/REQ-20260307-ATS-008.md` — REQ (Phase 1 Batch2)
