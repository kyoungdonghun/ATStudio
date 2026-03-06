[WI EVIDENCE PACK — Agent-facing]
WI ID: WI-20260306-ATS-005
REQ: REQ-20260306-ATS-005
Date: 2026-03-06
Agent: docops
Status: COMPLETE

---

## 1. Scope Summary

Target file: `docs/check/atstudio-front-list.md`
Change type: Content update (v1 → v2)
Backend changes: None
API Spec changes: None

---

## 2. Section-by-Section Change Detail

### 2.1 Album Section — NEW (① Album 섹션 신규 추가)

New section `## 💿 앨범 (Album)` inserted after `## 🎵 음원 (Track)`.
Position rationale: Track (supply-side public content) and Album (admin-curated public content) are symmetric discovery surfaces.

| No | 화면명 | 관련 API | 인증 | Status |
|----|--------|---------|------|--------|
| L-1 | 앨범 목록 (이미지 타입) | `15.2 GET /api/albums` | [PUBLIC] | NEW |
| L-2 | 앨범 목록 (리스트 타입) | `15.2 GET /api/albums` | [PUBLIC] | NEW |
| L-3 | 앨범 상세 | `15.3 GET /api/albums/{id}` | [PUBLIC] | NEW |
| L-4 | 앨범 생성 | `15.1 POST /api/albums` | [ADMIN] | NEW |
| L-5 | 앨범 수정 + 트랙 관리 | `15.4 PUT /api/albums/{id}` `15.6 POST (트랙 추가)` `15.7 DELETE (트랙 제거)` `15.8 PUT (순서 변경)` | [ADMIN] | NEW |

API source references verified in `docs/design/api-spec.md` Section 15 (lines 1621–1688).

### 2.2 Playlist Section — MODIFIED (② 재생목록 상세 추가 / ③ Screen 9 API 보완)

#### Added: C-1 재생목록 상세

| No | 화면명 | 관련 API | 인증 | Status |
|----|--------|---------|------|--------|
| C-1 | 재생목록 상세 | `3.3 GET /api/playlists/{id}` | auth required | NEW |

API source: `docs/design/api-spec.md` Section 3.3 (line 403).

#### Modified: Screen 9 (재생목록 수정) — API ref additions

Before:
```
3.5 PUT /api/playlists/{id}  3.6 PUT (트랙 순서)  3.7 DELETE (트랙 삭제)
```

After:
```
3.5 PUT /api/playlists/{id}  3.6 PUT (트랙 순서)  3.7 DELETE (트랙 삭제)  3.4 POST (트랙 추가)  3.8 DELETE /api/playlists/{id}
```

Added: `3.4 POST (트랙 추가)` (api-spec.md line 426) and `3.8 DELETE /api/playlists/{id}` (api-spec.md line 488).
Rationale: 재생목록 수정 화면에서 트랙 추가 및 재생목록 삭제(confirm() 처리)를 모두 수행하므로 참조 API에 포함.

### 2.3 개인 페이지 Section — MODIFIED (④ Screen 10 비밀번호 변경 모달)

#### Modified: Screen 10 (개인정보 페이지)

Before:
```
화면명: 개인정보 페이지
API: 5.4 GET /api/users/me  5.7 PUT /api/users/me  5.9 DELETE /api/users/me
```

After:
```
화면명: 개인정보 페이지 (비밀번호 변경 모달 포함)
API: 5.4 GET /api/users/me  5.7 PUT /api/users/me  5.9 DELETE /api/users/me  5.11 PUT /api/users/me/password
```

API source: `docs/design/api-spec.md` Section 5.11 (added in REQ-20260303-ATS-001 WI-002).

### 2.4 범례 — MODIFIED (⑤ 삭제 정책 명시)

Before: Legend had 4 items (`[PUBLIC]`, `auth required`, `[ADMIN]`, `⚠️`).

After: Added deletion policy line immediately after the legend line:
```
> `🗑️ 삭제` = 상세/목록 페이지에서 `confirm()` 처리 / 회원탈퇴만 비밀번호 재확인 모달
```

### 2.5 관리자 페이지 Section — MODIFIED (⑥ K-7 트랙 관리 추가)

| No | 화면명 | 관련 API | 인증 | Status |
|----|--------|---------|------|--------|
| K-7 | 트랙 관리 (전체 목록 + 활성화/삭제) | `1.2 GET /api/tracks` (비활성 포함) `1.6 PUT /api/tracks/{id}` `1.7 DELETE /api/tracks/{id}` | [ADMIN] | NEW |

API sources verified in `docs/design/api-spec.md` Section 1.

---

## 3. Total Screen Count — Breakdown (⑦)

| 섹션 | v1 | v2 | 변동 |
|------|----|----|------|
| 인증 / 회원가입 | 4 | 4 | 0 |
| 음원 (Track) | 6 | 6 | 0 |
| 앨범 (Album) | 0 | 5 | +5 |
| 재생목록 (Playlist) | 4 | 5 | +1 |
| 개인 페이지 (회원) | 5 | 5 | 0 |
| 장바구니 (다운로드 큐) | 1 | 1 | 0 |
| 구독 | 3 | 3 | 0 |
| 유튜브 채널 화이트리스트 | 1 | 1 | 0 |
| 기업 인증 | 2 | 2 | 0 |
| 문의하기 | 3 | 3 | 0 |
| 공지사항 | 4 | 4 | 0 |
| 관리자 페이지 | 7 | 8 | +1 |
| **합계** | **40** | **47** | **+7** |

---

## 4. Terminology Compliance Check

- "앨범 (Album)" used throughout Album section: PASS
- "재생목록 (Playlist)" used throughout Playlist section: PASS
- No "playlist" in Korean (플레이리스트) mixed with "재생목록": PASS
  - Exception: Section header `## 💿 재생목록 (Playlist)` — parenthetical English identifier retained for agent injection clarity
- No "album" in Korean (알범) used anywhere: PASS

Note: v1 section header was `## 💿 플레이리스트 (Playlist)`. This was corrected to `## 💿 재생목록 (Playlist)` per REQ-20260306-ATS-004 naming confirmation and the WI constraint "재생목록 표기 유지".

---

## 5. Acceptance Criteria Verification

| Criterion | Result |
|-----------|--------|
| `## 💿 앨범 (Album)` 섹션 신규 추가, 5개 화면 포함 | PASS |
| 재생목록 상세 화면 (C-1) 추가 | PASS |
| Screen 9 API 참조 보완 (3.4, 3.8) | PASS |
| Screen 10에 `5.11 PUT /api/users/me/password` + 모달 명시 | PASS |
| 삭제 정책 범례에 명시 | PASS |
| K-7 어드민 트랙 관리 추가 | PASS |
| 총 화면 수 정확히 업데이트 (47개) | PASS |
| "재생목록"/"앨범" 혼용 없음 | PASS |
| 모든 화면에 API 참조 기재 | PASS |
| 인증 컬럼 정확히 기재 | PASS |

---

## 6. Files Modified

| File | Change |
|------|--------|
| `docs/check/atstudio-front-list.md` | v1 → v2 (7 change items applied) |

---

## 7. Blocks

WI-20260306-ATS-006 (문서 정확성 리뷰 by cr) is now unblocked.
Input: `docs/check/atstudio-front-list.md` v2
