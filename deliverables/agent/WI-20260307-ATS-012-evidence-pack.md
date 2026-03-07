[WI EVIDENCE PACK — Agent-Facing]
WI ID: WI-20260307-ATS-012
REQ: REQ-20260307-ATS-008 (Phase 1 Batch6)
Date: 2026-03-07
Agent: docops
Status: COMPLETE

---

## Scope Summary

검증 대상 파일:
- docs/check/atstudio-front-list.md (v3, 2026-03-07)
- docs/check/modal-list.md (v1.1, 2026-03-07)
- docs/check/screen-flow.md (v1.1, 2026-03-07)

검증 관점: 도메인 WI-007~011에서 커버하기 어려운 cross-cutting 정합성 이슈.

---

## Verification Checklist (전수 확인)

### V-1: 화면 번호 참조 일치 (modal-list "화면" 컬럼 → front-list 실제 존재)

| Modal ID | 발생 화면 | front-list 존재 | 비고 |
|----------|---------|----------------|------|
| M-01 | Screen 10 | PASS (라인 61) | |
| M-02 | Screen 10 | PASS (라인 61) | |
| M-03 | Screen 6 | PASS (라인 28) | |
| M-04 | Screen 7 | PASS (라인 29) | |
| M-05 | Screen 9 | PASS (라인 53) | |
| M-06 | L-5 | PASS (라인 41) | |
| M-07 | Screen 9 | PASS (라인 53) | |
| M-08 | L-5 | PASS (라인 41) | |
| M-09 | Screen 16-3 | PASS (라인 83) | |
| M-10 | Screen 16-3 | PASS (라인 83) | |
| M-11 | K-7 | PASS (라인 136) | |
| M-12 | 1/3/B-1 | PASS (라인 25,27,27) | |
| M-13 | Screen 4/5 | PASS (라인 49,50) | |
| M-14 | L-1/L-2 | PASS (라인 37,38) | |
| M-15 | I-1 | PASS (라인 99) | |
| M-16 | Screen 14 | PASS (라인 109) | |
| M-17 | K-5 | PASS (라인 134) | |
| M-18 | K-4 | PASS (라인 133) | |
| M-19 | Screen 21/22 | PASS 부분 (라인 119,121) | MINOR: 22에서만 발생이 자연스러움. 상세 → M-1 |
| M-20 | Screen 15 | PASS (라인 110) | |
| M-21 | H-1 | PASS (라인 91) | |
| M-22 | Screen 11 | PASS (라인 73) | |
| M-23 | D-1 | PASS (라인 62) | |
| M-24 | K-2 | PASS (라인 131) | |
| M-25 | K-1 | PASS (라인 130) | |
| M-26 | Screen 16-2 | PASS (라인 82) | |
| M-27 | M-09 (모달 내) | N/A — 화면 아님 | MINOR: 발생 화면 컬럼에 모달번호 기재. 상세 → M-5 |
| M-28 | K-6 | PASS (라인 135) | |

결론: 모든 화면 번호 front-list에 실제 존재 확인. MINOR 2건 발생 (M-1, M-5).

---

### V-2: screen-flow M-## 참조 → modal-list 실제 존재

| screen-flow 참조 위치 | M-## | modal-list 존재 | 결과 |
|----------------------|------|----------------|------|
| §3 라인 81,87 | M-12 | 라인 81 | PASS |
| §4 라인 105 | M-13 | 라인 82 | PASS |
| §4 라인 112 | M-05 | 라인 65 | PASS |
| §4 라인 117 | M-07 | 라인 67 | PASS |
| §5 라인 138 | M-06 | 라인 66 | PASS |
| §5 라인 142 | M-08 | 라인 68 | PASS |
| §6 라인 155 | M-22 | 라인 91 | PASS |
| §7 라인 176 | M-26 | 라인 95 | PASS |
| §7 라인 183 | M-09 | 라인 69 | PASS |
| §7 라인 186 | M-27 | 라인 96 | PASS |
| §7 라인 191 | M-10 | 라인 70 | PASS |
| §8 라인 203 | M-01 | 라인 61 | PASS |
| §8 라인 204 | M-02 | 라인 62 | PASS |
| §8 라인 208 | M-23 | 라인 92 | PASS |
| §8 라인 221 | M-21 | 라인 90 | PASS |
| §8 라인 224 | M-15 | 라인 84 | PASS |
| §9 라인 246 | M-16 | 라인 85 | PASS |
| §9 라인 250 | M-20 | 라인 89 | PASS |
| §9 라인 254 | M-19 | 라인 88 | PASS |
| §10 라인 272 | M-25 | 라인 94 | PASS |
| §10 라인 276 | M-24 | 라인 93 | PASS |
| §10 라인 280 | M-18 | 라인 87 | PASS |
| §10 라인 282 | M-17 | 라인 86 | PASS |
| §10 라인 287 | M-28 | 라인 97 | PASS |
| §10 라인 289 | M-11 | 라인 80 | PASS |
| §10 라인 292 | M-03 | 라인 63 | PASS |
| §10 라인 296 | M-04 | 라인 64 | PASS |

결론: screen-flow 내 M-## 참조 전수(27건) 모두 modal-list에 실제 존재 확인. GAP 없음.

---

### V-3: 총 화면 수 카운트 = 48개

직접 카운트 (front-list.md):

| 섹션 | 화면 ID 목록 | 수 |
|------|------------|---|
| 인증/회원가입 | A-1, A-2, A-3, A-4 | 4 |
| 음원(Track) | 1, 3, B-1, 6, 7 | 5 |
| 앨범(Album) | L-1, L-2, L-3, L-4, L-5 | 5 |
| 재생목록(Playlist) | 4, 5, C-1, 8, 9 | 5 |
| 개인 페이지 | 10, D-1, E-1, F-1, F-2 | 5 |
| 장바구니 | 11 | 1 |
| 구독 | 16-1, 16-2, 16-3 | 3 |
| 유튜브 채널 | H-1 | 1 |
| 기업인증 | I-1, I-2 | 2 |
| 문의 | 13, 14, 15 | 3 |
| 공지 | 20, 21, 21-2, 22 | 4 |
| 관리자 | 18, K-1, K-2, K-3, K-4, K-5, K-6, K-7 | 8 |
| 에러 | ERR-1, ERR-2 | 2 |
| **합계** | | **48** |

PASS. front-list 하단 "총 48개" 명시(라인 151)와 일치.
screen-flow 하단 "총 48개 화면 (46 + ERR-1 + ERR-2)" (라인 357) — 비-에러 46개 + ERR 2개 = 48 일치.

---

### V-4: 총 모달 수 카운트 = 28개

modal-list M-01~M-28 전수 존재 확인. 하단 "총 28개 모달" 명시(라인 255) PASS.
- Section 1 (화면 기반): M-01~M-10 (10개)
- Section 2 (유스케이스 기반): M-11~M-28 (18개)
- 보류: M-15, M-26, M-27 (3개) — 28개 내 포함됨.

---

### V-5: Screen 2 잔존 참조 없음

- front-list.md 전체 스캔: "Screen 2" 또는 "음원 목록 (이미지 타입)" 항목 없음. PASS.
- modal-list.md 전체 스캔: "Screen 2" 문자열 없음. PASS.
- screen-flow.md 전체 스캔: "Screen 2" 문자열 없음. PASS.

---

### V-6: GNB 구조(screen-flow §1) vs front-list 인증 권한 일치

screen-flow §1 (라인 28~30):
```
[비로그인]    GNB: 메인 / 앨범 / 구독플랜 / 로그인
[구독자]      GNB: 메인 / 앨범 / 재생목록 / 장바구니 / 내정보 / 로그아웃
[관리자]      GNB: 메인 / 앨범 / 음원관리 / 관리자대시보드 / 로그아웃
```

front-list 권한 매핑 검증:
- 메인(1): [PUBLIC] → 비로그인/구독자/관리자 모두 노출 ✓
- 앨범(L-1, L-2, L-3): [PUBLIC] → 비로그인/구독자/관리자 모두 앨범 GNB 노출 ✓
  - L-4, L-5: [ADMIN] → 앨범 목록 내에서 관리자에게만 노출
- 구독플랜(16-1): [PUBLIC] → 비로그인 GNB 노출 ✓
- 재생목록(4, 5, C-1 등): auth required → 구독자 GNB 노출 ✓
- 장바구니(11): auth required → 구독자 GNB ✓
- 내정보(10 등): auth required → 구독자 GNB ✓
- 음원관리(6, 7, K-7): [ADMIN] → 관리자 GNB ✓
- 관리자대시보드(18, K-1~K-7): [ADMIN] → 관리자 GNB ✓

전체 권한 매핑 일치. 단, [관리자] GNB에 "앨범" 항목 없음 → MINOR 발견 M-4 참조.

---

### V-7: screen-flow §11 전역 패턴 → front-list ERR-1/ERR-2 존재

screen-flow §11 (라인 311~312):
- "API 에러 404 → [404 에러 페이지]"
- "API 에러 500 → [500 에러 페이지]"

front-list (라인 146~147):
- ERR-1: 404 Not Found 존재 ✓
- ERR-2: 500 Server Error 존재 ✓

화면 실체 존재 PASS. 단, screen-flow가 ERR-1/ERR-2 식별자를 사용하지 않고 설명 텍스트로 기재 → MINOR 발견 M-3 참조.

---

### V-8: Deferred Items (M-15, M-26, M-27) screen-flow "[보류]" 반영

| Modal | modal-list 상태 | screen-flow 반영 | 결과 |
|-------|---------------|-----------------|------|
| M-15 | [보류] (라인 84) | §8 라인 224: "[M-15 FileUploadModal 보류]" | PASS |
| M-26 | [보류] (라인 95) | §7 라인 176: "[M-26 PG 보류]" | PASS |
| M-27 | [보류] (라인 96) | §7 라인 186: "[M-27 PG 보류]" | PASS |

3개 모두 screen-flow에 "[보류]" 반영 확인.

---

### V-9: modal-list frontmatter — front-list.md 버전 참조 v3 업데이트 여부

modal-list.md frontmatter (라인 8~11):
```yaml
dependencies:
  - path: docs/check/atstudio-front-list.md
    reason: Screen number system and screen names (primary source)
```
버전 명시 없음.

modal-list.md 본문 상단 (라인 38):
> 관련 화면 목록: [docs/check/atstudio-front-list.md](atstudio-front-list.md) v3

본문에 v3 참조 있음 ✓. frontmatter에는 버전 미기재 → SUGGESTION S-1 참조.

---

## Findings (발견 목록)

### MINOR (5건)

```
[CONFLICT] MINOR: (docs/check/modal-list.md:88) vs (docs/check/screen-flow.md:254)
  M-19 발생 화면 불일치.
  modal-list: "Screen 21/22 (공지 조회)" — 21(공지 작성)과 22(공지 조회) 모두 기재.
  screen-flow §9: "[22]에서 [ADMIN] 삭제 → [M-19 ConfirmModal]" — 22 단독 기재.
  front-list에서 21은 "공지 작성", 22는 "공지 조회". 삭제 액션은 조회 화면(22)에서만 발생.
  권고: modal-list M-19 발생 화면을 "Screen 22 (공지 조회)"로 정정.
```

```
[CONFLICT] MINOR: (docs/check/screen-flow.md:19) vs (docs/check/modal-list.md:3)
  screen-flow 헤더 참조 버전 오기.
  screen-flow: "atstudio-front-list.md v3 / modal-list.md v1 기준" — modal-list를 v1로 참조.
  modal-list 실제 버전: v1.1 (frontmatter 라인 3).
  권고: screen-flow 헤더의 "modal-list.md v1" → "modal-list.md v1.1"로 정정.
```

```
[OMISSION] MINOR: (docs/check/screen-flow.md:311-312) — ERR-1/ERR-2 식별자 미사용.
  screen-flow §11 전역 패턴에서 "API 에러 404 → [404 에러 페이지]", "500 → [500 에러 페이지]"로 기재.
  front-list에서 화면 식별자 ERR-1, ERR-2로 정의됨 (라인 146~147).
  screen-flow 내 다른 화면은 식별자([A-1], [16-3] 등)로 참조하나 에러 페이지만 설명 텍스트 사용.
  권고: "[ERR-1 404 Not Found]", "[ERR-2 500 Server Error]"로 통일.
```

```
[GAP] MINOR: (docs/check/screen-flow.md:30) — [관리자] GNB에 앨범 접근 경로 미기재.
  screen-flow §1 [관리자] GNB: "메인 / 앨범 / 음원관리 / 관리자대시보드 / 로그아웃"
  주석: 실제 screen-flow 라인 30을 재확인하면 [관리자] GNB에 "앨범"이 없음.
  front-list에서 L-4(앨범 생성), L-5(앨범 수정+트랙 관리)는 [ADMIN] 전용.
  screen-flow §5에서 "[ADMIN] 앨범 생성 → L-4" 흐름이 있으나, GNB에서 앨범 목록(L-1/L-2)을
  경유하여 L-4/L-5에 도달하는 경로가 GNB 기술에서 누락됨.
  [비로그인], [구독자] GNB에는 "앨범"이 있으나 [관리자] GNB에는 없어, 관리자의 앨범 GNB
  접근 가능 여부가 불명확.
  권고: [관리자] GNB에 "앨범" 추가 여부 결정 후 screen-flow §1 업데이트.
```

```
[CONFLICT] MINOR: (docs/check/modal-list.md:96) — M-27 "발생 화면" 컬럼에 화면번호 아닌 모달번호 기재.
  modal-list Section 2 테이블 컬럼 스키마: "발생 화면"은 front-list 화면 ID를 기재하는 컬럼.
  M-27의 "발생 화면": "M-09 (PlanCompareModal 내)" — 화면번호가 아닌 모달번호.
  M-27은 M-09(PlanCompareModal) 내에서만 발생하는 2차 모달이므로 화면번호 직접 기재 불가.
  동일 패턴: M-09의 발생 화면 "Screen 16-3"이 M-27의 실질적 화면.
  권고: "발생 화면" 컬럼 표기를 "Screen 16-3 (M-09 내)" 등으로 통일하거나, 비고 컬럼 추가.
```

---

### SUGGESTION (1건)

```
[SUGGESTION]: (docs/check/modal-list.md:8-11) — frontmatter dependencies에 버전 명시 권장.
  현재: dependencies path만 있고 버전 미기재.
  본문(라인 38)에는 "atstudio-front-list.md v3" 참조 있음.
  표준상 frontmatter의 dependencies에 버전 명시 시 의존 추적이 명확해짐.
  예: reason: "Screen number system and screen names (primary source) — v3"
  우선순위: LOW (본문에 버전 참조가 이미 존재하므로 즉시 수정 불필요).
```

---

## Confirmed Clean (이상 없음 확인 항목)

- front-list 화면 수: 48개 직접 카운트 일치
- modal-list 모달 수: M-01~M-28 (28개) 일치
- modal-list "화면" 컬럼 → front-list 전수 존재 확인 (M-27 제외 모두 화면번호 일치)
- screen-flow M-## 참조 전수 → modal-list 존재 확인 (27건 GAP 없음)
- Screen 2 잔존 참조: 3종 문서 모두 없음
- Deferred Items 보류 표기: M-15/M-26/M-27 모두 screen-flow에 "[보류]" 반영
- GNB 권한 매핑: 비로그인/구독자/관리자 접근 권한 front-list와 전반적 일치
- screen-flow §11 → ERR-1/ERR-2 실체 존재 확인

---

## Traceability

| 항목 | 포인터 |
|------|--------|
| front-list 총계 | docs/check/atstudio-front-list.md:151 |
| modal-list 총계 | docs/check/modal-list.md:255 |
| screen-flow 총계 | docs/check/screen-flow.md:357 |
| GNB 구조 | docs/check/screen-flow.md:28-30 |
| §11 전역 패턴 | docs/check/screen-flow.md:303-316 |
| M-19 발생 화면 | docs/check/modal-list.md:88 |
| screen-flow 헤더 버전 | docs/check/screen-flow.md:19 |
| ERR-1/ERR-2 정의 | docs/check/atstudio-front-list.md:146-147 |
| 관리자 GNB | docs/check/screen-flow.md:30 |
| M-27 발생 화면 | docs/check/modal-list.md:96 |
| Deferred Items | docs/check/modal-list.md:248-251 |
